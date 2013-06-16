/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.google.youtube.upload;

import com.google.common.base.Charsets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.inject.Inject;
import com.google.inject.Injector;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import org.chaosfisch.exceptions.ErrorCode;
import org.chaosfisch.exceptions.SystemException;
import org.chaosfisch.google.auth.AuthCode;
import org.chaosfisch.google.auth.GDataRequestSigner;
import org.chaosfisch.google.auth.IGoogleLogin;
import org.chaosfisch.google.youtube.MetadataService;
import org.chaosfisch.google.youtube.PlaylistService;
import org.chaosfisch.google.youtube.ResumeableManager;
import org.chaosfisch.google.youtube.impl.MetadataCode;
import org.chaosfisch.google.youtube.impl.ResumeInfo;
import org.chaosfisch.google.youtube.upload.events.UploadAbortEvent;
import org.chaosfisch.google.youtube.upload.events.UploadProgressEvent;
import org.chaosfisch.http.RequestUtil;
import org.chaosfisch.services.EnddirService;
import org.chaosfisch.util.EventBusUtil;
import org.chaosfisch.util.ExtendedPlaceholders;
import org.chaosfisch.util.GsonHelper;
import org.chaosfisch.util.io.Throttle;
import org.chaosfisch.util.io.ThrottledOutputStream;
import org.chaosfisch.youtubeuploader.ApplicationData;
import org.chaosfisch.youtubeuploader.command.RefreshPlaylistsCommand;
import org.chaosfisch.youtubeuploader.db.dao.AccountDao;
import org.chaosfisch.youtubeuploader.db.dao.PlaylistDao;
import org.chaosfisch.youtubeuploader.db.dao.UploadDao;
import org.chaosfisch.youtubeuploader.db.events.ModelUpdatedEvent;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Account;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;
import org.chaosfisch.youtubeuploader.guice.ICommandProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class UploadWorker extends Task<Void> {

	/** Status enum for handling control flow */
	protected enum STATUS {
		ABORTED, DONE, FAILED, INITIALIZE, METADATA, POSTPROCESS, RESUMEINFO, UPLOAD, POSTPROCESS_BROWSER;
		private ErrorCode errorCode;

		private ErrorCode getErrorCode() {
			return errorCode;
		}

		private STATUS setErrorCode(final ErrorCode errorCode) {
			this.errorCode = errorCode;
			return this;
		}
	}

	private static final int    DEFAULT_BUFFER_SIZE = 65536;
	private              STATUS currentStatus       = STATUS.INITIALIZE;

	private long start;
	private long totalBytesUploaded;
	private long bytesToUpload;
	private long fileSize;

	/** File that is uploaded */
	private File   fileToUpload;
	private Upload upload;

	private final Logger logger = LoggerFactory.getLogger(getClass() + " -> " + Thread.currentThread().getName());
	@Inject
	private PlaylistService      playlistService;
	@Inject
	private GDataRequestSigner   requestSigner;
	@Inject
	private MetadataService      metadataService;
	@Inject
	private EnddirService        enddirService;
	@Inject
	private Injector             injector;
	@Inject
	private IGoogleLogin         authTokenHelper;
	@Inject
	private Throttle             throttle;
	@Inject
	private ResumeableManager    resumeableManager;
	@Inject
	private ExtendedPlaceholders extendedPlacerholders;
	@Inject
	private EventBus             eventBus;
	@Inject
	private PlaylistDao          playlistDao;
	@Inject
	private UploadDao            uploadDao;
	@Inject
	private AccountDao           accountDao;
	@Inject
	private ICommandProvider     commandProvider;

	private UploadProgressEvent uploadProgress;

	public UploadWorker() {
		EventBusUtil.getInstance().register(this);
	}

	@Override
	protected Void call() throws Exception {
		// Einstiegspunkt in diesen Thread.
		/*
		 * Abzuarbeiten sind mehrere Teilschritte, jeder Schritt kann jedoch
		 * fehlschlagen und muss wiederholbar sein.
		 */
		while (STATUS.ABORTED != currentStatus && STATUS.DONE != currentStatus && STATUS.FAILED != currentStatus && resumeableManager
				.canContinue() && !isCancelled() && !Thread.currentThread().isInterrupted()) {
			try {

				switch (currentStatus) {
					case INITIALIZE:
						initialize();
						break;
					case METADATA:
						// Schritt 1: MetadataUpload + UrlFetch
						metadata();
						break;
					case UPLOAD:
						// Schritt 2: Chunkupload
						upload();
						break;
					case RESUMEINFO:
						// Schritt 3: Fetchen des Resumeinfo
						resumeinfo();
						break;
					case POSTPROCESS:
						// Schritt 4: Postprocessing
						postprocess();
						break;
					case POSTPROCESS_BROWSER:
						postprocessBrowser();
						break;
					default:
						break;
				}
				resumeableManager.setRetries(0);
			} catch (final SystemException e) {
				if (e.getErrorCode() instanceof MetadataCode) {
					if (e.getErrorCode().equals(MetadataCode.BAD_REQUEST)) {
						logger.warn("Bad request", e);
						currentStatus = STATUS.FAILED.setErrorCode(MetadataCode.BAD_REQUEST);
					} else {
						logger.warn("Metadata IO error", e);
						resumeableManager.setRetries(resumeableManager.getRetries() + 1);
						resumeableManager.delay();
					}
				} else if (e.getErrorCode() instanceof AuthCode) {
					logger.warn("AuthException", e);
					resumeableManager.setRetries(resumeableManager.getRetries() + 1);
					resumeableManager.delay();
				} else if (e.getErrorCode() instanceof UploadCode) {
					if (e.getErrorCode().equals(UploadCode.FILE_NOT_FOUND)) {
						logger.warn("File not found - upload failed", e);
						currentStatus = STATUS.FAILED.setErrorCode(UploadCode.FILE_NOT_FOUND);
					} else if (e.getErrorCode().equals(UploadCode.LOGFILE_IO_ERROR)) {
						logger.warn("Logfile IO Error", e);
					} else if (e.getErrorCode().equals(UploadCode.PLAYLIST_IO_ERROR)) {
						logger.warn("Playlist IO Error", e);
					} else if (e.getErrorCode().equals(UploadCode.UPDATE_METADATA_IO_ERROR)) {
						logger.warn("Update Metadata IO Error", e);
					} else {
						logger.warn("UploadException", e);
						currentStatus = STATUS.RESUMEINFO;
					}
				}
			}
		}
		return null;
	}

	@Override
	protected void done() {
		super.done();
		try {
			get();
		} catch (final InterruptedException e) { // $codepro.audit.disable
			// logExceptions
			Thread.currentThread().interrupt();
		} catch (final Throwable t) {
			logger.debug("ERROR", t);
		}

		upload.setInprogress(false);
		switch (currentStatus) {
			case DONE:
				upload.setArchived(true);
				uploadDao.update(upload);
				uploadProgress.done = true;
				break;
			case FAILED:
				setFailedStatus(currentStatus.getErrorCode());
				break;
			case ABORTED:
				setFailedStatus(UploadCode.USER_ABORT);
				break;
			default:
				logger.info("Unknow error: {} ::::::: {}", currentStatus.getErrorCode(), upload.getStatus());
				setFailedStatus(UploadCode.UNKNOWN_ERROR);
				break;
		}
		eventBus.post(uploadProgress);
		updatePlaylists();
	}

	private void resumeinfo() throws SystemException {
		final ResumeInfo resumeInfo = resumeableManager.fetchResumeInfo(upload);
		if (null == resumeInfo) {
			currentStatus = STATUS.FAILED;
			throw new SystemException(UploadCode.MAX_RETRIES_REACHED).set("url", upload.getUploadurl())
					.set("time", Calendar.getInstance().getTime().toString());
		}
		logger.info("Resuming stalled upload to: {}", upload.getUploadurl());
		if (null != resumeInfo.videoId) { // upload actually completed despite
			// the exception
			final String videoId = resumeInfo.videoId;
			logger.info("No need to resume video ID {}", videoId);
			currentStatus = STATUS.POSTPROCESS;
		} else {
			totalBytesUploaded = resumeInfo.nextByteToUpload;
			// possibly rolling back the previously saved value
			fileSize = fileToUpload.length();
			bytesToUpload = fileSize - resumeInfo.nextByteToUpload;
			start = resumeInfo.nextByteToUpload;
			logger.info("Next byte to upload is {].", resumeInfo.nextByteToUpload);
			currentStatus = STATUS.UPLOAD;
		}
	}

	private void flowChunk(final InputStream inputStream, final OutputStream outputStream, final long startByte, final long endByte) throws IOException {

		// Write Chunk
		final byte[] buffer = new byte[UploadWorker.DEFAULT_BUFFER_SIZE];
		long totalRead = 0;

		while (!isCancelled() && STATUS.UPLOAD == currentStatus && totalRead != endByte - startByte + 1) {
			// Upload bytes in buffer
			final int bytesRead = RequestUtil.flowChunk(inputStream, outputStream, buffer, 0, UploadWorker.DEFAULT_BUFFER_SIZE);
			// Calculate all uploadinformation
			totalRead += bytesRead;
			totalBytesUploaded += bytesRead;

			// PropertyChangeEvent
			final long diffTime = Calendar.getInstance().getTimeInMillis() - uploadProgress.getTime();
			if (1000 < diffTime || totalRead == endByte - startByte + 1) {
				uploadProgress.setBytes(totalBytesUploaded);
				uploadProgress.setTime(diffTime);
				eventBus.post(uploadProgress);
			}
		}
	}

	private long generateEndBytes(final long start, final double bytesToUpload) {
		final long end;
		if (0 < bytesToUpload - throttle.chunkSize.get()) {
			end = start + throttle.chunkSize.get() - 1;
		} else {
			end = start + (int) bytesToUpload - 1;
		}
		return end;
	}

	private void initialize() throws FileNotFoundException {
		// Set the time uploaded started
		final GregorianCalendar calendar = new GregorianCalendar();
		upload.setDateOfStart(calendar);
		uploadDao.update(upload);

		// Get File and Check if existing
		fileToUpload = upload.getFile();

		if (!fileToUpload.exists()) {
			throw new FileNotFoundException("Datei existiert nicht.");
		}

		updatePlaylists();
	}

	private void updatePlaylists() {
		final Object monitor = new Object();

		final EventHandler<WorkerStateEvent> eventHandler = new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent workerStateEvent) {
				currentStatus = STATUS.METADATA;
				synchronized (monitor) {
					monitor.notifyAll();
				}
			}
		};

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				final RefreshPlaylistsCommand command = commandProvider.get(RefreshPlaylistsCommand.class);
				command.accounts = new Account[] {accountDao.findById(upload.getAccountId())};
				command.setOnFailed(eventHandler);
				command.setOnSucceeded(eventHandler);
				command.start();
			}
		});

		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized (monitor) {
			try {
				monitor.wait();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private void metadata() throws SystemException {

		if (null != upload.getUploadurl() && !upload.getUploadurl().isEmpty()) {
			logger.info("Uploadurl existing: {}", upload.getUploadurl());
			currentStatus = STATUS.RESUMEINFO;
			return;
		}

		replacePlaceholders();
		final String atomData = metadataService.atomBuilder(upload);
		upload.setUploadurl(metadataService.createMetaData(atomData, fileToUpload, accountDao.findById(upload.getAccountId())));
		uploadDao.update(upload);

		// Log operation
		logger.info("Uploadurl received: {}", upload.getUploadurl());
		// INIT Vars
		fileSize = fileToUpload.length();
		totalBytesUploaded = 0;
		start = 0;
		bytesToUpload = fileSize;
		currentStatus = STATUS.UPLOAD;

	}

	@Subscribe
	public void onAbortUpload(final UploadAbortEvent uploadAbortEvent) {
		if (uploadAbortEvent.getUpload().equals(upload)) {
			currentStatus = STATUS.ABORTED;
		}
	}

	@Subscribe
	public void onUploadUpdated(final ModelUpdatedEvent modelUpdatedEvent) {
		if (modelUpdatedEvent.getModel() instanceof Upload && upload.equals(modelUpdatedEvent.getModel())) {
			upload = (Upload) modelUpdatedEvent.getModel();
			if (null != uploadProgress) {
				uploadProgress.setUpload(upload);
			}
		}
	}

	private void playlistAction() throws SystemException {
		// Add video to playlist
		for (final Playlist playlist : playlistDao.fetchByUpload(upload)) {
			try {
				playlistService.addLatestVideoToPlaylist(playlist, upload.getVideoid());
			} catch (final SystemException e) {
				throw new SystemException(e, UploadCode.PLAYLIST_IO_ERROR);
			}
		}
	}

	private void postprocessBrowser() throws SystemException {
		try {
			browserAction();
		} finally {
			currentStatus = STATUS.DONE;
		}
	}

	private void postprocess() throws SystemException {
		try {
			playlistAction();
			updateUploadAction();
			logfileAction();
			enddirAction();
		} finally {
			currentStatus = STATUS.POSTPROCESS_BROWSER;
		}
	}

	private void updateUploadAction() throws SystemException {
		final String atomData = metadataService.atomBuilder(upload);
		try {
			metadataService.updateMetaData(atomData, upload.getVideoid(), accountDao.findById(upload.getAccountId()));
		} catch (SystemException e) {
			throw new SystemException(e, UploadCode.UPDATE_METADATA_IO_ERROR).set("atomdata", atomData);
		}
	}

	private void logfileAction() throws SystemException {
		try {
			Files.createDirectories(Paths.get(ApplicationData.DATA_DIR + "/uploads/"));
			Files.write(Paths.get(ApplicationData.DATA_DIR + "/uploads/" + upload.getVideoid() + ".json"), GsonHelper.toJSON(upload)
					.getBytes(Charsets.UTF_8));
		} catch (IOException e) {
			throw new SystemException(e, UploadCode.LOGFILE_IO_ERROR);
		}
	}

	private void browserAction() throws SystemException {
		if (null == upload.getDateOfRelease() && (null == upload.getThumbnail() || upload.getThumbnail()
				.isEmpty()) && !upload.getMonetizeClaim()) {
			return;
		}
		logger.info("Monetizing, Releasing, Partner-features, Saving...");

		extendedPlacerholders.setFile(upload.getFile());
		extendedPlacerholders.setPlaylists(playlistDao.fetchByUpload(upload));
		extendedPlacerholders.register("{title}", upload.getTitle());
		extendedPlacerholders.register("{description}", upload.getDescription());

		upload.setMonetizeTitle(extendedPlacerholders.replace(upload.getMonetizeTitle()));
		upload.setMonetizeDescription(extendedPlacerholders.replace(upload.getMonetizeDescription()));
		upload.setMonetizeId(extendedPlacerholders.replace(upload.getMonetizeId()));
		upload.setMonetizeNotes(extendedPlacerholders.replace(upload.getMonetizeNotes()));

		upload.setMonetizeTmsid(extendedPlacerholders.replace(upload.getMonetizeTmsid()));
		upload.setMonetizeIsan(extendedPlacerholders.replace(upload.getMonetizeIsan()));
		upload.setMonetizeEidr(extendedPlacerholders.replace(upload.getMonetizeEidr()));
		upload.setMonetizeTitleepisode(extendedPlacerholders.replace(upload.getMonetizeTitleepisode()));
		upload.setMonetizeSeasonNb(extendedPlacerholders.replace(upload.getMonetizeSeasonNb()));
		upload.setMonetizeEpisodeNb(extendedPlacerholders.replace(upload.getMonetizeEpisodeNb()));
		upload.setThumbnail(extendedPlacerholders.replace(upload.getThumbnail()));

		metadataService.activateBrowserfeatures(upload);
	}

	private void enddirAction() {
		enddirService.moveFileByUpload(fileToUpload, upload);
	}

	private void replacePlaceholders() {
		final List<Playlist> playlists = playlistDao.fetchByUpload(upload);
		final ExtendedPlaceholders extendedPlaceholders = new ExtendedPlaceholders(upload.getFile(), playlists);
		upload.setTitle(extendedPlaceholders.replace(upload.getTitle()));
		upload.setDescription(extendedPlaceholders.replace(upload.getDescription()));
		upload.setKeywords(extendedPlaceholders.replace(upload.getKeywords()));
	}

	public void run(final Upload upload) {
		this.upload = upload;
	}

	private void setFailedStatus(final ErrorCode errorCode) {
		upload.setFailed(true);
		upload.setStatus(errorCode.getClass().getName() + '.' + errorCode.name());
		upload.setDateOfStart(null);
		uploadDao.update(upload);
		uploadProgress.failed = true;
	}

	private void upload() throws SystemException {
		// GET END SIZE
		final long end = generateEndBytes(start, bytesToUpload);

		// Log operation
		logger.debug(String.format("start=%s end=%s filesize=%s", start, end, (int) bytesToUpload));

		// Log operation
		logger.debug(String.format("Uploaded %d bytes so far, using PUT method.", (int) totalBytesUploaded));

		if (null == uploadProgress) {
			uploadProgress = new UploadProgressEvent(upload, fileSize);
			uploadProgress.setTime(Calendar.getInstance().getTimeInMillis());
		}

		// Calculating the chunk size
		final int chunk = (int) (end - start + 1);

		try {
			// Building PUT RequestImpl for chunk data
			final URL url = URI.create(upload.getUploadurl()).toURL();
			final HttpURLConnection request = (HttpURLConnection) url.openConnection();
			request.setRequestMethod("POST");
			request.setDoOutput(true);
			request.setFixedLengthStreamingMode(chunk);
			//Properties
			request.setRequestProperty("Content-Type", upload.getMimetype());
			request.setRequestProperty("Content-Range", String.format("bytes %d-%d/%d", start, end, fileToUpload.length()));
			requestSigner.setAuthHeader(authTokenHelper.getAuthHeader(accountDao.findById(upload.getAccountId())));
			requestSigner.sign(request);
			request.connect();

			try (final BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(fileToUpload));
				 final BufferedOutputStream throttledOutputStream = new BufferedOutputStream(new ThrottledOutputStream(request
						 .getOutputStream(), throttle))) {
				final long skipped = bufferedInputStream.skip(start);
				if (start != skipped) {
					throw new SystemException(UploadCode.FILE_IO_ERROR);
				}
				flowChunk(bufferedInputStream, throttledOutputStream, start, end);

				switch (request.getResponseCode()) {
					case 200:
						throw new SystemException(UploadCode.UPLOAD_REPONSE_200);
					case 201:
						final InputSupplier<InputStream> supplier = new InputSupplier<InputStream>() {
							@Override
							public InputStream getInput() throws IOException {
								return request.getInputStream();
							}
						};
						upload.setVideoid(resumeableManager.parseVideoId(CharStreams.toString(CharStreams.newReaderSupplier(supplier, Charsets.UTF_8))));
						uploadDao.update(upload);
						currentStatus = STATUS.POSTPROCESS;
						break;
					case 308:
						// OK, the chunk completed succesfully
						logger.debug("responseMessage={}", request.getResponseMessage());
						break;
					default:
						throw new SystemException(UploadCode.UPLOAD_RESPONSE_UNKNOWN).set("code", request.getResponseCode());
				}
				bytesToUpload -= throttle.chunkSize.get();
				start = end + 1;
			}
		} catch (final FileNotFoundException ex) {
			throw new SystemException(ex, UploadCode.FILE_NOT_FOUND).set("file", fileToUpload);
		} catch (final IOException ex) {
			if (STATUS.ABORTED != currentStatus) {
				throw new SystemException(ex, UploadCode.FILE_IO_ERROR);
			}
		}
	}
}
