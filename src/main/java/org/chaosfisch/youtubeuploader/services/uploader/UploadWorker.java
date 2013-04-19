/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader.services.uploader;

import com.google.common.base.Charsets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.inject.Inject;
import com.google.inject.Injector;
import javafx.concurrent.Task;
import org.chaosfisch.exceptions.SystemException;
import org.chaosfisch.google.auth.AuthCode;
import org.chaosfisch.google.auth.GoogleAuthUtil;
import org.chaosfisch.io.Throttle;
import org.chaosfisch.io.ThrottledOutputStream;
import org.chaosfisch.io.http.RequestSigner;
import org.chaosfisch.io.http.RequestUtil;
import org.chaosfisch.util.EventBusUtil;
import org.chaosfisch.util.ExtendedPlaceholders;
import org.chaosfisch.util.TagParser;
import org.chaosfisch.youtubeuploader.db.dao.PlaylistDao;
import org.chaosfisch.youtubeuploader.db.dao.UploadDao;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;
import org.chaosfisch.youtubeuploader.services.EnddirService;
import org.chaosfisch.youtubeuploader.services.MetadataService;
import org.chaosfisch.youtubeuploader.services.PlaylistService;
import org.chaosfisch.youtubeuploader.services.ResumeableManager;
import org.chaosfisch.youtubeuploader.services.impl.MetadataCode;
import org.chaosfisch.youtubeuploader.services.impl.ResumeInfo;
import org.chaosfisch.youtubeuploader.services.uploader.events.UploadAbortEvent;
import org.chaosfisch.youtubeuploader.services.uploader.events.UploadProgressEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class UploadWorker extends Task<Void> {

	/** Status enum for handling control flow */
	protected enum STATUS {
		ABORTED, DONE, FAILED, FAILED_FILE, FAILED_META, INITIALIZE, METADATA, POSTPROCESS, RESUMEINFO, UPLOAD
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
	private RequestSigner        requestSigner;
	@Inject
	private MetadataService      metadataService;
	@Inject
	private EnddirService        enddirService;
	@Inject
	private Injector             injector;
	@Inject
	private GoogleAuthUtil       authTokenHelper;
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
		while (!(currentStatus.equals(STATUS.ABORTED) || currentStatus.equals(STATUS.DONE) || currentStatus.equals(STATUS.FAILED) || currentStatus
				.equals(STATUS.FAILED_FILE) || currentStatus.equals(STATUS.FAILED_META)) && resumeableManager.canContinue() && !isCancelled() && !Thread
				.currentThread()
				.isInterrupted()) {
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
					default:
						break;
				}
				resumeableManager.setRetries(0);
			} catch (final SystemException e) {
				if (e.getErrorCode() instanceof MetadataCode) {
					logger.warn("MetadataException - upload aborted", e);
					currentStatus = STATUS.FAILED_META;
				} else if (e.getErrorCode() instanceof AuthCode) {
					logger.warn("AuthException", e);
					resumeableManager.setRetries(resumeableManager.getRetries() + 1);
					resumeableManager.delay();
				} else if (e.getErrorCode() instanceof UploadCode) {
					if (e.getErrorCode().equals(UploadCode.FILE_NOT_FOUND)) {
						logger.warn("File not found - upload failed", e);
						currentStatus = STATUS.FAILED_FILE;
					} else {
						logger.warn("UploadException", e);
						currentStatus = STATUS.RESUMEINFO;
					}
				}
			}
		}
		return null;
	}

	private void resumeinfo() throws SystemException {
		final ResumeInfo resumeInfo = resumeableManager.fetchResumeInfo(upload);
		if (resumeInfo == null) {
			currentStatus = STATUS.FAILED;
			throw new SystemException(UploadCode.MAX_RETRIES_REACHED).set("url", upload.getUploadurl())
					.set("time", Calendar.getInstance().getTime().toString());
		}
		logger.info("Resuming stalled upload to: {}", upload.getUploadurl());
		if (resumeInfo.videoId != null) { // upload actually completed despite
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
				setFailedStatus("Upload failed!");
				break;
			case FAILED_FILE:
				setFailedStatus("File not found!");
				break;
			case FAILED_META:
				setFailedStatus("Corrupted Uploadinformation!");
				break;
			case ABORTED:
				setFailedStatus("Beendet auf Userrequest");
				break;
			default:
				setFailedStatus("Unknown-Error");
				break;
		}
		eventBus.post(uploadProgress);
		cancel();
	}

	private void flowChunk(final InputStream inputStream, final OutputStream outputStream, final long startByte, final long endByte) throws IOException {

		// Write Chunk
		final byte[] buffer = new byte[UploadWorker.DEFAULT_BUFFER_SIZE];
		long totalRead = 0;

		while (!isCancelled() && currentStatus == STATUS.UPLOAD && totalRead != endByte - startByte + 1) {
			// Upload bytes in buffer
			final int bytesRead = RequestUtil.flowChunk(inputStream, outputStream, buffer, 0, UploadWorker.DEFAULT_BUFFER_SIZE);
			// Calculate all uploadinformation
			totalRead += bytesRead;
			totalBytesUploaded += bytesRead;

			// PropertyChangeEvent
			final long diffTime = Calendar.getInstance().getTimeInMillis() - uploadProgress.getTime();
			if (diffTime > 1000 || totalRead == endByte - startByte + 1) {
				uploadProgress.setBytes(totalBytesUploaded);
				uploadProgress.setTime(diffTime);
				eventBus.post(uploadProgress);
			}
		}
	}

	private long generateEndBytes(final long start, final double bytesToUpload) {
		final long end;
		if (bytesToUpload - throttle.chunkSize.get() > 0) {
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

		currentStatus = STATUS.METADATA;
	}

	private void metadata() throws SystemException {

		if (upload.getUploadurl() != null && !upload.getUploadurl().isEmpty()) {
			logger.info("Uploadurl existing: {}", upload.getUploadurl());
			currentStatus = STATUS.RESUMEINFO;
			return;
		}

		replacePlaceholders();
		final String atomData = metadataService.atomBuilder(upload);
		upload.setUploadurl(metadataService.submitMetadata(atomData, fileToUpload, uploadDao.fetchOneAccountByUpload(upload)));
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

	private void playlistAction() {
		// Add video to playlist
		for (final Playlist playlist : playlistDao.fetchByUpload(upload)) {
			try {
				playlistService.addLatestVideoToPlaylist(playlist, upload.getVideoid());
			} catch (final SystemException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void postprocess() {
		playlistAction();
		enddirAction();
		browserAction();
		currentStatus = STATUS.DONE;
	}

	private void browserAction() {
		if (upload.getDateOfRelease() == null && (upload.getThumbnail() == null || upload.getThumbnail()
				.isEmpty()) && !upload.getMonetizeClaim()) {
			return;
		}
		logger.info("Monetizing, Releasing, Partner-features, Saving...");

		extendedPlacerholders.setFile(upload.getFile());
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

		metadataService.activateBrowserfeatures(upload);
	}

	private void enddirAction() {
		enddirService.moveFileByUpload(fileToUpload, upload);
	}

	private void replacePlaceholders() {
		final ExtendedPlaceholders extendedPlaceholders = new ExtendedPlaceholders(upload.getFile(), null,
				// upload.parent(Playlist.class),
				0);// upload.getNumber());
		upload.setTitle(extendedPlaceholders.replace(upload.getTitle()));
		upload.setDescription(extendedPlaceholders.replace(upload.getDescription()));
		upload.setKeywords(extendedPlaceholders.replace(upload.getKeywords()));
		upload.setKeywords(TagParser.parseAll(upload.getKeywords()));
		upload.setKeywords(upload.getKeywords().replaceAll("\"", ""));
	}

	public void run(final Upload upload) {
		this.upload = upload;
	}

	private void setFailedStatus(final String status) {
		upload.setFailed(true);
		upload.setDateOfStart(null);
		uploadDao.update(upload);
		uploadProgress.failed = true;
		uploadProgress.status = status;
	}

	private void upload() throws SystemException {
		// GET END SIZE
		final long end = generateEndBytes(start, bytesToUpload);

		// Log operation
		logger.debug(String.format("start=%s end=%s filesize=%s", start, end, (int) bytesToUpload));

		// Log operation
		logger.debug(String.format("Uploaded %d bytes so far, using PUT method.", (int) totalBytesUploaded));

		if (uploadProgress == null) {
			uploadProgress = new UploadProgressEvent(upload, fileSize);
			uploadProgress.setTime(Calendar.getInstance().getTimeInMillis());
		}

		// Calculating the chunk size
		final int chunk = (int) (end - start + 1);

		try {
			// Building PUT Request for chunk data
			final URL url = URI.create(upload.getUploadurl()).toURL();
			final HttpURLConnection request = (HttpURLConnection) url.openConnection();
			request.setRequestMethod("POST");
			request.setDoOutput(true);
			request.setFixedLengthStreamingMode(chunk);
			//Properties
			request.setRequestProperty("Content-Type", upload.getMimetype());
			request.setRequestProperty("Content-Range", String.format("bytes %d-%d/%d", start, end, fileToUpload.length()));
			requestSigner.signWithAuthorization(request, authTokenHelper.getAuthHeader(uploadDao.fetchOneAccountByUpload(upload)));
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
			throw SystemException.wrap(ex, UploadCode.FILE_NOT_FOUND).set("file", fileToUpload);
		} catch (final IOException ex) {
			if (currentStatus != STATUS.ABORTED) {
				throw SystemException.wrap(ex, UploadCode.FILE_IO_ERROR);
			}
		}
	}
}
