/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.upload;

import com.blogspot.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.blogspot.nurkiewicz.asyncretry.RetryExecutor;
import com.blogspot.nurkiewicz.asyncretry.function.RetryRunnable;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.RateLimiter;
import de.chaosfisch.youtube.YouTubeFactory;
import de.chaosfisch.youtube.upload.metadata.IMetadataService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Set;
import java.util.concurrent.*;

public class UploadJob implements Callable<Upload> {

	private static final int    DEFAULT_BUFFER_SIZE = 65536;
	private static final Logger LOGGER              = LoggerFactory.getLogger(UploadJob.class);
	private static final int    SC_500              = 500;
	private final Set<UploadPreProcessor>  uploadPreProcessors;
	private final Set<UploadPostProcessor> uploadPostProcessors;
	private final EventBus                 eventBus;
	private final IUploadService           uploadService;
	private final IMetadataService         metadataService;
	/**
	 * File that is uploaded
	 */
	private       File                     fileToUpload;
	private       long                     totalBytesUploaded;
	private       long                     fileSize;
	private       RateLimiter              rateLimiter;
	private       UploadJobProgressEvent   uploadProgress;
	private       Upload                   upload;

	@Inject
	public UploadJob(final Set<UploadPreProcessor> uploadPreProcessors, final Set<UploadPostProcessor> uploadPostProcessors, final EventBus eventBus, final IUploadService uploadService, final IMetadataService metadataService) {
		this.uploadPreProcessors = uploadPreProcessors;
		this.uploadPostProcessors = uploadPostProcessors;
		this.eventBus = eventBus;
		this.uploadService = uploadService;
		this.metadataService = metadataService;
		this.eventBus.register(this);
	}

	public void setUpload(final Upload upload) {
		this.upload = upload;
	}

	@Override
	public Upload call() throws Exception {

		if (null == rateLimiter || null == upload) {
			throw new IllegalArgumentException("Rate limiter or upload missing for uploadJob");
		}

		if (null == upload.getUploadurl()) {
			for (final UploadPreProcessor preProcessor : uploadPreProcessors) {
				try {
					upload = preProcessor.process(upload);
				} catch (final Exception e) {
					LOGGER.error("Preprocessor error", e);
				}
			}
		}

		final ScheduledExecutorService schedueler = Executors.newSingleThreadScheduledExecutor();
		final RetryExecutor executor = new AsyncRetryExecutor(schedueler).withExponentialBackoff(TimeUnit.SECONDS
																										 .toMillis(3), 2)
				.withMaxDelay(TimeUnit.MINUTES.toMillis(1))
				.withMaxRetries(10)
				.retryOn(IOException.class)
				.retryOn(RuntimeException.class)
				.retryOn(UploadResponseException.class)
				.abortIf(input -> input instanceof UploadResponseException && SC_500 >= ((UploadResponseException) input)
						.getStatus())
				.abortOn(FileNotFoundException.class)
				.abortOn(UploadFinishedException.class);

		try {
			// Schritt 1: Initialize
			initialize();
			// Schritt 2: MetadataUpload + UrlFetch
			executor.doWithRetry(metadata()).get();
			// Schritt 3: Chunkupload
			executor.doWithRetry(upload()).get();
		} catch (final InterruptedException ignored) {
			upload.setStatus(Status.ABORTED);
		} catch (final Exception e) {
			if (Status.FINISHED != upload.getStatus()) {
				LOGGER.error("Upload error", e);
				upload.setStatus(Status.FAILED);
			}
		} finally {
			schedueler.shutdownNow();
			eventBus.unregister(this);
		}

		if (Status.FINISHED == upload.getStatus()) {
			LOGGER.info("Starting postprocessing");
			for (final UploadPostProcessor postProcessor : uploadPostProcessors) {
				try {
					upload = postProcessor.process(upload);
				} catch (final Exception e) {
					LOGGER.error("Postprocessor error", e);
				}
			}
		}

		uploadService.update(upload);
		return upload;
	}

	private RetryRunnable upload() {
		return retryContext -> {
			if (null != upload.getUploadurl() || null != retryContext.getLastThrowable()) {
				if (0 < retryContext.getRetryCount()) {
					LOGGER.info("############ RETRY " + retryContext.getRetryCount() + " ############");
				}
			}
			uploadChunks();
		};
	}

	private void uploadChunks() throws IOException {
		while (!Thread.currentThread().isInterrupted() && totalBytesUploaded != fileSize) {
			uploadChunk();
		}
	}

	private void uploadChunk() throws IOException {

		// Log operation
		LOGGER.debug("Uploaded {} bytes so far, using PUT method.", totalBytesUploaded);

		if (null == uploadProgress) {
			uploadProgress = new UploadJobProgressEvent(upload, upload.getFile().length());
			uploadProgress.setTime(Calendar.getInstance().getTimeInMillis());
		}

		// Building PUT RequestImpl for chunk data
		final URL url = URI.create(upload.getUploadurl()).toURL();
		final HttpURLConnection request = (HttpURLConnection) url.openConnection();
		request.setRequestMethod("POST");
		request.setDoOutput(true);
		//Properties
		request.setRequestProperty("Content-Type", "application/octet-stream");
		request.connect();

	}

	private RetryRunnable metadata() {
		return retryContext -> {
			fileSize = fileToUpload.length();
			totalBytesUploaded = 0;

			if (null != upload.getUploadurl() && !upload.getUploadurl().isEmpty()) {
				LOGGER.info("Uploadurl existing: {}", upload.getUploadurl());
				return;
			}

			upload.setUploadurl(fetchUploadUrl(upload));
			uploadService.update(upload);

			// Log operation
			LOGGER.info("Uploadurl received: {}", upload.getUploadurl());
		};
	}

	private String fetchUploadUrl(final Upload upload) {
		// Upload atomData and fetch uploadUrl
		final Video video = metadataService.buildVideoEntry(upload);


		try {
			final InputStreamContent mediaContent = new InputStreamContent("video/*", new TokenInputStream(new FileInputStream(upload.getFile())));
			final YouTube.Videos.Insert videoInsert = YouTubeFactory.getYouTube(upload.getAccount()).videos()
					.insert("snippet,status", video, mediaContent);

			final MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();
			uploader.setDirectUploadEnabled(false);

			final MediaHttpUploaderProgressListener progressListener = uploader1 -> {
				switch (uploader.getUploadState()) {
					case INITIATION_STARTED:
						System.out.println("Initiation Started");
						break;
					case INITIATION_COMPLETE:
						System.out.println("Initiation Completed");
						break;
					case MEDIA_IN_PROGRESS:
						System.out.println("Upload in progress");
						System.out.println("Upload percentage: " + uploader.getProgress());
						break;
					case MEDIA_COMPLETE:
						System.out.println("Upload Completed!");
						break;
					case NOT_STARTED:
						System.out.println("Upload Not Started!");
						break;
				}
			};
			uploader.setProgressListener(progressListener);

			// Call the API and upload the video.
			final Video returnedVideo = videoInsert.execute();
			metadataService.updateMetaData(metadataService.updateVideoEntry(returnedVideo, upload), upload.getAccount());
		} catch (final GoogleJsonResponseException e) {
			e.printStackTrace();
			System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : "
									   + e.getDetails().getMessage());
		} catch (final IOException e) {
			e.printStackTrace();
			System.err.println("IOException: " + e.getMessage());
		} catch (final Throwable t) {
			t.printStackTrace();
			System.err.println("Throwable: " + t.getMessage());
		}
		return "";//TODO fix fetchUploadUrl;
	}

	private void initialize() throws FileNotFoundException {
		// Set the time uploaded started
		upload.setDateTimeOfStart(LocalDateTime.now().minusSeconds(1));
		uploadService.update(upload);

		// Get File and Check if existing
		fileToUpload = upload.getFile();

		if (!fileToUpload.exists()) {
			throw new FileNotFoundException("Datei existiert nicht.");
		}
	}

	private void handleSuccessfulUpload() throws UploadFinishedException {
		upload.setStatus(Status.FINISHED);
		uploadService.update(upload);
		throw new UploadFinishedException();
	}

	public void setRateLimiter(final RateLimiter rateLimiter) {
		this.rateLimiter = rateLimiter;
	}

	private static class UploadResponseException extends Exception {
		private static final long serialVersionUID = 9064482080311824304L;
		private final int status;

		public UploadResponseException(final int status) {
			super(String.format("Upload response exception: %d", status));
			this.status = status;
		}

		private int getStatus() {
			return status;
		}
	}

	private static class UploadFinishedException extends Exception {
		private static final long serialVersionUID = -5907578118391546810L;

		public UploadFinishedException() {
			super("Upload finished!");
		}
	}

	private class TokenInputStream extends BufferedInputStream {

		public TokenInputStream(final InputStream inputStream) {
			super(inputStream, DEFAULT_BUFFER_SIZE);
		}

		@Override
		public synchronized int read(@NotNull final byte[] b, final int off, final int len) throws IOException {
			if (0 < rateLimiter.getRate()) {
				rateLimiter.acquire(b.length);
			}

			if (Thread.currentThread().isInterrupted()) {
				LOGGER.error("Upload aborted / stopped.");
				upload.setStatus(Status.ABORTED);
				throw new CancellationException("Thread cancled");
			}

			final int bytes = super.read(b, off, len);

			// Event Upload Progress
			// Calculate all uploadinformation
			totalBytesUploaded += b.length;
			final long diffTime = Calendar.getInstance().getTimeInMillis() - uploadProgress.getTime();
			if (1000 < diffTime) {
				uploadProgress.setBytes(totalBytesUploaded);
				uploadProgress.setTime(diffTime);
				eventBus.post(uploadProgress);
			}

			return bytes;
		}
	}
}
