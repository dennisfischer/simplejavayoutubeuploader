/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.services.youtube.uploader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Calendar;

import javafx.concurrent.Task;

import javax.sql.DataSource;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.google.auth.AuthenticationException;
import org.chaosfisch.google.auth.RequestSigner;
import org.chaosfisch.util.ExtendedPlaceholders;
import org.chaosfisch.util.GoogleAuthUtil;
import org.chaosfisch.util.TagParser;
import org.chaosfisch.util.io.InputStreams;
import org.chaosfisch.util.io.Request;
import org.chaosfisch.util.io.Request.Method;
import org.chaosfisch.util.io.RequestUtil;
import org.chaosfisch.util.io.Throttle;
import org.chaosfisch.util.io.ThrottledOutputStream;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.models.Upload;
import org.chaosfisch.youtubeuploader.services.youtube.impl.ResumeInfo;
import org.chaosfisch.youtubeuploader.services.youtube.spi.EnddirService;
import org.chaosfisch.youtubeuploader.services.youtube.spi.MetadataService;
import org.chaosfisch.youtubeuploader.services.youtube.spi.PlaylistService;
import org.chaosfisch.youtubeuploader.services.youtube.spi.ResumeableManager;
import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class UploadWorker extends Task<Void> {

	/**
	 * Status enum for handling control flow
	 */
	protected enum STATUS {
		ABORTED, DONE, FAILED, FAILED_FILE, FAILED_META, INITIALIZE, METADATA, POSTPROCESS, RESUMEINFO, UPLOAD
	}

	private static final int				DEFAULT_BUFFER_SIZE	= 65536;
	private STATUS							currentStatus		= STATUS.INITIALIZE;

	private long							start;
	private double							totalBytesUploaded;
	private double							bytesToUpload;
	private double							fileSize;

	/**
	 * File that is uploaded
	 */
	private File							fileToUpload;
	private Upload							upload;

	private final Logger					logger				= LoggerFactory.getLogger(getClass() + " -> "
																		+ Thread.currentThread().getName());
	@Inject private PlaylistService			playlistService;
	@Inject private RequestSigner			requestSigner;
	@Inject private MetadataService			metadataService;
	@Inject private EnddirService			enddirService;
	@Inject private Injector				injector;
	@Inject private GoogleAuthUtil			authTokenHelper;
	@Inject private Throttle				throttle;
	@Inject private ResumeableManager		resumeableManager;
	@Inject private ExtendedPlaceholders	extendedPlacerholders;

	private UploadProgress					uploadProgress;

	public UploadWorker() {
		AnnotationProcessor.process(this);
	}

	@Override
	protected Void call() throws Exception {
		// Einstiegspunkt in diesen Thread.
		/*
		 * Abzuarbeiten sind mehrere Teilschritte, jeder Schritt kann jedoch
		 * fehlschlagen und muss wiederholbar sein.
		 */
		Base.open(injector.getInstance(DataSource.class));
		while (!(currentStatus.equals(STATUS.ABORTED) || currentStatus.equals(STATUS.DONE) || currentStatus.equals(STATUS.FAILED)
				|| currentStatus.equals(STATUS.FAILED_FILE) || currentStatus.equals(STATUS.FAILED_META))
				&& resumeableManager.canContinue() && !isCancelled()) {
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
			} catch (final FileNotFoundException e) {
				logger.warn("File not found - upload failed", e);
				currentStatus = STATUS.FAILED_FILE;
			} catch (final MetadataException e) {
				logger.warn("MetadataException - upload aborted", e);
				currentStatus = STATUS.FAILED_META;
			} catch (final AuthenticationException e) {
				logger.warn("AuthException", e);
				resumeableManager.setRetries(resumeableManager.getRetries() + 1);
				resumeableManager.delay();
			} catch (final UploadException e) {
				logger.warn("UploadException", e);
				currentStatus = STATUS.RESUMEINFO;
			}
		}
		return null;
	}

	private void resumeinfo() throws UploadException, AuthenticationException {
		final ResumeInfo resumeInfo = resumeableManager.fetchResumeInfo(upload);
		if (resumeInfo == null) {
			currentStatus = STATUS.FAILED;
			throw new UploadException(String.format("Giving up uploading '%s'.", upload.getString("uploadurl")));
		}
		logger.info("Resuming stalled upload to: {}", upload.getString("uploadurl"));
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
		} catch (final Throwable t) {
			logger.debug("ERROR", t);
		}

		upload.setBoolean("inprogress", false);
		switch (currentStatus) {
			case DONE:
				upload.setBoolean("archived", true);
				upload.saveIt();
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
		EventBus.publish(Uploader.PROGRESS, uploadProgress);
		Base.close();
		cancel();
	}

	private void flowChunk(final InputStream inputStream, final OutputStream outputStream, final long startByte, final long endByte)
			throws IOException {

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
				EventBus.publish(Uploader.PROGRESS, uploadProgress);
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
		upload.setDate("started", Calendar.getInstance().getTime());
		upload.saveIt();

		// Get File and Check if existing
		fileToUpload = new File(upload.getString("file"));

		if (!fileToUpload.exists()) {
			throw new FileNotFoundException("Datei existiert nicht.");
		}

		currentStatus = STATUS.METADATA;
	}

	private void metadata() throws MetadataException, AuthenticationException {

		if (upload.getString("uploadurl") != null && !upload.getString("uploadurl").isEmpty()) {
			logger.info("Uploadurl existing: {}", upload.getString("uploadurl"));
			currentStatus = STATUS.RESUMEINFO;
			return;
		}

		replacePlaceholders();
		final String atomData = metadataService.atomBuilder(upload);
		upload.setString("uploadurl", metadataService.submitMetadata(atomData, fileToUpload, upload.parent(Account.class)));
		upload.saveIt();

		// Log operation
		logger.info("Uploadurl received: {}", upload.getString("uploadurl"));
		// INIT Vars
		fileSize = fileToUpload.length();
		totalBytesUploaded = 0;
		start = 0;
		bytesToUpload = fileSize;
		currentStatus = STATUS.UPLOAD;

	}

	@EventTopicSubscriber(topic = Uploader.ABORT)
	public void onAbortUpload(final String topic, final Upload abort) {
		if (abort.equals(upload)) {
			currentStatus = STATUS.ABORTED;
		}
	}

	private void playlistAction() {
		// Add video to playlist
		if (upload.getAll(Playlist.class) != null) {
			for (final Playlist playlist : upload.getAll(Playlist.class)) {
				playlistService.addLatestVideoToPlaylist(playlist, upload.getString("videoId"));
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
		if (upload.get("release") == null && (upload.getString("thumbnail") == null || upload.getString("thumbnail").isEmpty())) {
			return;
		}
		// !upload.getBoolean("monetize") && !upload.getBoolean("claim") &&

		logger.info("Monetizing, Releasing, Partner-features, Saving...");

		// extendedPlacerholders.register("{title}", upload.getString("title"));
		// extendedPlacerholders.register("{description}",
		// upload.getString("description"));
		//
		// upload.setString("webTitle",
		// extendedPlacerholders.replace(upload.getString("webTitle")));
		// upload.setString("webDescription",
		// extendedPlacerholders.replace(upload
		// .getString("webDescription")));
		// upload.setString("webID",
		// extendedPlacerholders.replace(upload.getString("webID")));
		// upload.setString("webNotes",
		// extendedPlacerholders.replace(upload.getString("webNotes")));
		//
		// upload.setString("tvTMSID",
		// extendedPlacerholders.replace(upload.getString("tvTMSID")));
		// upload.setString("tvISAN",
		// extendedPlacerholders.replace(upload.getString("tvISAN")));
		// upload.setString("tvEIDR",
		// extendedPlacerholders.replace(upload.getString("tvEIDR")));
		// upload.setString("showTitle",
		// extendedPlacerholders.replace(upload.getString("showTitle")));
		// upload.setString("episodeTitle",
		// extendedPlacerholders.replace(upload.getString("episodeTitle")));
		// upload.setString("seasonNb",
		// extendedPlacerholders.replace(upload.getString("seasonNb")));
		// upload.setString("episodeNb",
		// extendedPlacerholders.replace(upload.getString("episodeNb")));
		// upload.setString("tvID",
		// extendedPlacerholders.replace(upload.getString("tvID")));
		// upload.setString("tvNotes",
		// extendedPlacerholders.replace(upload.getString("tvNotes")));
		//
		// upload.setString("movieTitle",
		// extendedPlacerholders.replace(upload.getString("movieTitle")));
		// upload.setString("movieDescription", extendedPlacerholders
		// .replace(upload.getString("movieDescription")));
		// upload.setString("movieTMSID",
		// extendedPlacerholders.replace(upload.getString("movieTMSID")));
		// upload.setString("movieISAN",
		// extendedPlacerholders.replace(upload.getString("movieISAN")));
		// upload.setString("movieEIDR",
		// extendedPlacerholders.replace(upload.getString("movieEIDR")));
		// upload.setString("movieID",
		// extendedPlacerholders.replace(upload.getString("movieID")));
		// upload.setString("movieNotes",
		// extendedPlacerholders.replace(upload.getString("movieNotes")));
		metadataService.activateBrowserfeatures(upload);
	}

	private void enddirAction() {
		enddirService.moveFileByUpload(fileToUpload, upload);
	}

	private void replacePlaceholders() {
		final ExtendedPlaceholders extendedPlaceholders = new ExtendedPlaceholders(upload.getString("file"), null,
		// upload.parent(Playlist.class),
				upload.getInteger("number"));
		upload.setString("title", extendedPlaceholders.replace(upload.getString("title")));
		upload.setString("description", extendedPlaceholders.replace(upload.getString("description")));
		upload.setString("keywords", extendedPlaceholders.replace(upload.getString("keywords")));

		upload.setString("keywords", TagParser.parseAll(upload.getString("keywords")));
		upload.setString("keywords", upload.getString("keywords").replaceAll("\"", ""));
	}

	public void run(final Upload queue) {
		upload = queue;
	}

	private void setFailedStatus(final String status) {
		upload.setBoolean("failed", true);
		upload.set("started", null);
		upload.saveIt();
		uploadProgress.failed = true;
		uploadProgress.status = status;
	}

	private void upload() throws UploadException, AuthenticationException {
		// GET END SIZE
		final long end = generateEndBytes(start, bytesToUpload);

		// Log operation
		logger.debug(String.format("start=%s end=%s filesize=%s", start, end, (int) bytesToUpload));

		// Log operation
		logger.debug(String.format("Uploaded %d bytes so far, using PUT method.", (int) totalBytesUploaded));

		if (uploadProgress == null) {
			uploadProgress = new UploadProgress(upload, fileSize);
			uploadProgress.setTime(Calendar.getInstance().getTimeInMillis());
		}

		// Calculating the chunk size
		final int chunk = (int) (end - start + 1);

		try {
			// Building PUT Request for chunk data
			final HttpURLConnection request = new Request.Builder(upload.getString("uploadurl"), Method.POST).headers(
					ImmutableMap.of("Content-Type", upload.getString("mimetype"), "Content-Range",
							String.format("bytes %d-%d/%d", start, end, fileToUpload.length()))).buildHttpUrlConnection();

			requestSigner.signWithAuthorization(request, authTokenHelper.getAuthHeader(upload.parent(Account.class)));

			// Input
			final BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(fileToUpload));
			// Output
			request.setDoOutput(true);
			request.setFixedLengthStreamingMode(chunk);
			request.connect();
			final BufferedOutputStream throttledOutputStream = new BufferedOutputStream(new ThrottledOutputStream(
					request.getOutputStream(), throttle));
			try {
				final long skipped = bufferedInputStream.skip(start);
				if (start != skipped) {
					throw new UploadException("Fehler beim Lesen der Datei!");
				}
				flowChunk(bufferedInputStream, throttledOutputStream, start, end);

				switch (request.getResponseCode()) {
					case 200:
						throw new UploadException("Received 200 response during resumable uploading");
					case 201:
						upload.setString("videoid", resumeableManager.parseVideoId(InputStreams.toString(request.getInputStream())));
						upload.saveIt();
						currentStatus = STATUS.POSTPROCESS;
					break;
					case 308:
						// OK, the chunk completed succesfully
						logger.debug("responseMessage={}", request.getResponseMessage());
					break;
					default:
						throw new UploadException(String.format("Unexpected return code while uploading: %s", request.getResponseMessage()));
				}
				bytesToUpload -= throttle.chunkSize.get();
				start = end + 1;
			} finally {
				try {
					bufferedInputStream.close();
					throttledOutputStream.close();
				} catch (final IOException ignored) {
					// throw new RuntimeException("This shouldn't happen", e);
				}
			}
		} catch (final FileNotFoundException ex) {
			throw new UploadException("Datei konnte nicht gefunden werden!", ex);
		} catch (final IOException ex) {
			if (currentStatus != STATUS.ABORTED) {
				throw new UploadException("Fehler beim Schreiben der Datei (0x00001)", ex);
			}
		}
	}
}
