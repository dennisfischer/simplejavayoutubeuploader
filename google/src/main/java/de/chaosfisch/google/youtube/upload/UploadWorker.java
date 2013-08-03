/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.upload;

import com.google.common.base.Charsets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.inject.Inject;
import de.chaosfisch.google.account.IAccountService;
import de.chaosfisch.google.auth.IGoogleRequestSigner;
import de.chaosfisch.google.youtube.upload.events.UploadJobAbortEvent;
import de.chaosfisch.google.youtube.upload.events.UploadJobProgressEvent;
import de.chaosfisch.google.youtube.upload.metadata.IMetadataService;
import de.chaosfisch.google.youtube.upload.metadata.MetaBadRequestException;
import de.chaosfisch.google.youtube.upload.metadata.MetaIOException;
import de.chaosfisch.google.youtube.upload.metadata.MetaLocationMissingException;
import de.chaosfisch.google.youtube.upload.resume.IResumeableManager;
import de.chaosfisch.google.youtube.upload.resume.ResumeIOException;
import de.chaosfisch.google.youtube.upload.resume.ResumeInfo;
import de.chaosfisch.google.youtube.upload.resume.ResumeInvalidResponseException;
import de.chaosfisch.http.IRequestUtil;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class UploadWorker extends Task<Void> {

	private static final int  SC_OK                = 200;
	private static final int  SC_CREATED           = 201;
	private static final int  SC_RESUME_INCOMPLETE = 308;
	private static final long chunkSize            = 10485760;

	/** Status enum for handling control flow */
	protected enum STATUS {
		ABORTED, DONE, FAILED, INITIALIZE, METADATA, POSTPROCESS, RESUMEINFO, UPLOAD;
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

	@Inject
	private       IMetadataService     metadataService;
	@Inject
	private       IUploadService       uploadService;
	@Inject
	private       IAccountService      accountService;
	@Inject
	private       IGoogleRequestSigner requestSigner;
	@Inject
	private       IResumeableManager   resumeableManager;
	private final EventBus             eventBus;
	@Inject
	private       IRequestUtil         requestUtil;

	private static final Logger logger = LoggerFactory.getLogger(UploadWorker.class);

	private UploadJobProgressEvent uploadProgress;

	@Inject
	public UploadWorker(final EventBus eventBus) {
		this.eventBus = eventBus;
		this.eventBus.register(this);
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
					break;
				default:
					break;
			}

		}
		eventBus.unregister(this);
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

		upload.getStatus().setRunning(false);
		switch (currentStatus) {
			case DONE:
				upload.getStatus().setArchived(true);
				uploadService.update(upload);
				uploadProgress.done = true;
				break;
			case FAILED:
				setFailedStatus();
				break;
			case ABORTED:
				setFailedStatus();
				break;
			default:
				setFailedStatus();
				break;
		}
		eventBus.post(uploadProgress);
	}

	private void resumeinfo() throws ResumeIOException, ResumeInvalidResponseException {
		final ResumeInfo resumeInfo = resumeableManager.fetchResumeInfo(upload);
		if (null == resumeInfo) {
			currentStatus = STATUS.FAILED;

			///MAX RETRIES
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
			final int bytesRead = requestUtil.flowChunk(inputStream, outputStream, buffer, 0, UploadWorker.DEFAULT_BUFFER_SIZE);
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
		if (0 < bytesToUpload - chunkSize) {
			end = start + chunkSize - 1;
		} else {
			end = start + (int) bytesToUpload - 1;
		}
		return end;
	}

	private void initialize() throws FileNotFoundException {
		// Set the time uploaded started
		final GregorianCalendar calendar = new GregorianCalendar();
		upload.setDateOfStart(calendar);
		uploadService.update(upload);

		// Get File and Check if existing
		fileToUpload = upload.getFile();

		if (!fileToUpload.exists()) {
			throw new FileNotFoundException("Datei existiert nicht.");
		}
	}

	//	private void updatePlaylists() {
	//		final Object monitor = new Object();
	//
	//		final EventHandler<WorkerStateEvent> eventHandler = new EventHandler<WorkerStateEvent>() {
	//			@Override
	//			public void handle(final WorkerStateEvent workerStateEvent) {
	//				currentStatus = STATUS.METADATA;
	//				synchronized (monitor) {
	//					monitor.notifyAll();
	//				}
	//			}
	//		};
	//
	//		Platform.runLater(new Runnable() {
	//			@Override
	//			public void run() {
	//				final RefreshPlaylistsCommand command = commandProvider.get(RefreshPlaylistsCommand.class);
	//				command.accounts = new Account[] {upload.getAccount()};
	//				command.setOnFailed(eventHandler);
	//				command.setOnSucceeded(eventHandler);
	//				command.start();
	//			}
	//		});
	//
	//		//noinspection SynchronizationOnLocalVariableOrMethodParameter
	//		synchronized (monitor) {
	//			try {
	//				monitor.wait();
	//			} catch (InterruptedException e) {
	//				Thread.currentThread().interrupt();
	//			}
	//		}
	//	}

	private void metadata() throws MetaLocationMissingException, MetaBadRequestException, MetaIOException {

		if (null != upload.getUploadurl() && !upload.getUploadurl().isEmpty()) {
			logger.info("Uploadurl existing: {}", upload.getUploadurl());
			currentStatus = STATUS.RESUMEINFO;
			return;
		}

		final String atomData = metadataService.atomBuilder(upload);
		upload.setUploadurl(metadataService.createMetaData(atomData, fileToUpload, upload.getAccount()));
		uploadService.update(upload);

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
	public void onAbortUpload(final UploadJobAbortEvent uploadAbortEvent) {
		if (uploadAbortEvent.getUpload().equals(upload)) {
			currentStatus = STATUS.ABORTED;
		}
	}

	public void updateUpload(final Upload received) {
		if (upload.equals(received)) {
			upload = received;
			if (null != uploadProgress) {
				uploadProgress.setUpload(upload);
			}
		}
	}

	//	private void playlistAction() throws SystemException {
	//		// Add video to playlist
	//		for (final Playlist playlist : upload.getPlaylists()) {
	//			try {
	//				playlistService.addVideoToPlaylist(playlist, upload.getVideoid());
	//			} catch (final SystemException e) {
	//				throw new SystemException(e, UploadCode.PLAYLIST_IO_ERROR);
	//			}
	//		}
	//	}

	//	private void postprocessBrowser() throws SystemException {
	//		try {
	//			browserAction();
	//		} finally {
	//			currentStatus = STATUS.DONE;
	//		}
	//	}

	//	private void postprocess() throws SystemException {
	//		try {
	//			playlistAction();
	//			updateUploadAction();
	//			logfileAction();
	//			enddirAction();
	//		} finally {
	//			currentStatus = STATUS.POSTPROCESS_BROWSER;
	//		}
	//	}

	private void updateUploadAction() throws MetaIOException, MetaBadRequestException {
		final String atomData = metadataService.atomBuilder(upload);
		metadataService.updateMetaData(atomData, upload.getVideoid(), upload.getAccount());
	}

	//	private void logfileAction() throws SystemException {
	//		try {
	//			Files.createDirectories(Paths.get(ApplicationData.DATA_DIR + "/uploads/"));
	//			Files.write(Paths.get(ApplicationData.DATA_DIR + "/uploads/" + upload.getVideoid() + ".json"), jsonSerializer
	//					.toJSON(upload)
	//					.getBytes(Charsets.UTF_8));
	//		} catch (IOException e) {
	//			throw new SystemException(e, UploadCode.LOGFILE_IO_ERROR);
	//		}
	//	}
	//
	//	private void browserAction() throws SystemException {
	//		if (null == upload.getDateOfRelease() && (null == upload.getThumbnail() || !upload.getThumbnail()
	//				.exists()) && !upload.getMonetization().getClaim()) {
	//			return;
	//		}
	//		logger.info("Monetizing, Releasing, Partner-features, Saving...");
	//
	//		extendedPlacerholders.setFile(upload.getFile());
	//		extendedPlacerholders.setPlaylists(upload.getPlaylists());
	//		extendedPlacerholders.register("{title}", upload.getMetadata().getTitle());
	//		extendedPlacerholders.register("{description}", upload.getMetadata().getDescription());
	//
	//		final Monetization monetization = upload.getMonetization();
	//		monetization.setTitle(extendedPlacerholders.replace(monetization.getTitle()));
	//		monetization.setDescription(extendedPlacerholders.replace(monetization.getDescription()));
	//		monetization.setCustomId(extendedPlacerholders.replace(monetization.getCustomId()));
	//		monetization.setNotes(extendedPlacerholders.replace(monetization.getNotes()));
	//
	//		monetization.setTmsid(extendedPlacerholders.replace(monetization.getTmsid()));
	//		monetization.setIsan(extendedPlacerholders.replace(monetization.getEidr()));
	//		monetization.setTitleepisode(extendedPlacerholders.replace(monetization.getTitleepisode()));
	//		monetization.setSeasonNb(extendedPlacerholders.replace(monetization.getSeasonNb()));
	//		monetization.setEpisodeNb(extendedPlacerholders.replace(monetization.getEpisodeNb()));
	//		upload.setThumbnail(extendedPlacerholders.replace(upload.getThumbnail()));
	//
	//		metadataService.activateBrowserfeatures(upload);
	//	}
	//
	//	private void enddirAction() {
	//		enddirService.moveFileByUpload(fileToUpload, upload);
	//	}
	//
	//	private void replacePlaceholders() {
	//		final List<Playlist> playlists = upload.getPlaylists();
	//		final ExtendedPlaceholders extendedPlaceholders = new ExtendedPlaceholders(upload.getFile(), playlists, resourceBundle);
	//		final Metadata metadata = upload.getMetadata();
	//		metadata.setTitle(extendedPlaceholders.replace(metadata.getTitle()));
	//		metadata.setDescription(extendedPlaceholders.replace(metadata.getDescription()));
	//		metadata.setKeywords(extendedPlaceholders.replace(metadata.getKeywords()));
	//	}

	private void setFailedStatus() {
		upload.getStatus().setFailed(true);
		upload.setDateOfStart(null);
		uploadService.update(upload);
		uploadProgress.failed = true;
	}

	private void upload() throws UploadIOException, UploadResponseException {
		// GET END SIZE
		final long end = generateEndBytes(start, bytesToUpload);

		// Log operation
		logger.debug(String.format("start=%s end=%s filesize=%s", start, end, (int) bytesToUpload));

		// Log operation
		logger.debug(String.format("Uploaded %d bytes so far, using PUT method.", (int) totalBytesUploaded));

		if (null == uploadProgress) {
			uploadProgress = new UploadJobProgressEvent(upload, fileSize);
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
			requestSigner.setAccount(upload.getAccount());
			requestSigner.sign(request);
			request.connect();

			try (final BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(fileToUpload));
				 final BufferedOutputStream throttledOutputStream = new BufferedOutputStream(request.getOutputStream())) {
				bufferedInputStream.skip(start);
				flowChunk(bufferedInputStream, throttledOutputStream, start, end);

				switch (request.getResponseCode()) {
					case SC_OK:
						//FILE UPLOADED
						throw new UploadResponseException(SC_OK);
					case SC_CREATED:
						final InputSupplier<InputStream> supplier = new InputSupplier<InputStream>() {
							@Override
							public InputStream getInput() throws IOException {
								return request.getInputStream();
							}
						};
						upload.setVideoid(resumeableManager.parseVideoId(CharStreams.toString(CharStreams.newReaderSupplier(supplier, Charsets.UTF_8))));
						uploadService.update(upload);
						currentStatus = STATUS.POSTPROCESS;
						break;
					case SC_RESUME_INCOMPLETE:
						// OK, the chunk completed succesfully
						logger.debug("responseMessage={}", request.getResponseMessage());
						break;
					default:
						throw new UploadResponseException(request.getResponseCode());
				}

				bytesToUpload -= chunkSize;
				start = end + 1;
			}
		} catch (final IOException e) {
			throw new UploadIOException(e);
		}
	}
}
