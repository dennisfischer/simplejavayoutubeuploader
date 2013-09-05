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

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.RateLimiter;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.chaosfisch.google.YouTubeProvider;
import de.chaosfisch.google.youtube.upload.events.UploadJobFinishedEvent;
import de.chaosfisch.google.youtube.upload.events.UploadJobProgressEvent;
import de.chaosfisch.google.youtube.upload.metadata.TagParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

public class UploadJob implements Callable<Upload> {

	private static final int    DEFAULT_BUFFER_SIZE = 65536;
	private static final Logger LOGGER              = LoggerFactory.getLogger(UploadJob.class);

	private final Set<UploadPreProcessor>  uploadPreProcessors;
	private final Set<UploadPostProcessor> uploadPostProcessors;
	private final EventBus                 eventBus;
	private final IUploadService           uploadService;
	private final RateLimiter              rateLimiter;

	private       UploadJobProgressEvent uploadProgress;
	private       Upload                 upload;
	private       long                   totalBytesUploaded;
	private final YouTubeProvider        youTubeProvider;

	@Inject
	private UploadJob(@Assisted final Upload upload, @Assisted final RateLimiter rateLimiter, final Set<UploadPreProcessor> uploadPreProcessors, final Set<UploadPostProcessor> uploadPostProcessors, final EventBus eventBus, final IUploadService uploadService, final YouTubeProvider youTubeProvider) {
		this.upload = upload;
		this.rateLimiter = rateLimiter;
		this.uploadPreProcessors = uploadPreProcessors;
		this.uploadPostProcessors = uploadPostProcessors;
		this.eventBus = eventBus;
		this.uploadService = uploadService;
		this.youTubeProvider = youTubeProvider;
		this.eventBus.register(this);
	}

	@Override
	public Upload call() throws Exception {

		for (final UploadPreProcessor preProcessor : uploadPreProcessors) {
			try {
				upload = preProcessor.process(upload);
			} catch (Exception e) {
				LOGGER.error("Preprocessor error", e);
			}
		}

		try {
			// Schritt 1: Initialize
			initialize();
			upload();

			for (final UploadPostProcessor postProcessor : uploadPostProcessors) {
				try {
					upload = postProcessor.process(upload);
				} catch (Exception e) {
					LOGGER.error("Postprocessor error", e);
				}
			}

			eventBus.post(new UploadJobFinishedEvent(upload));
		} catch (Exception e) {
			if (!upload.getStatus().isAborted()) {
				LOGGER.error("Upload error", e);
				upload.getStatus().setFailed(true);
			}
		}

		upload.getStatus().setRunning(false);
		uploadService.update(upload);
		eventBus.unregister(this);
		return upload;
	}

	private void initialize() throws FileNotFoundException {
		// Set the time uploaded started
		final GregorianCalendar calendar = new GregorianCalendar();
		upload.setDateOfStart(calendar);
		uploadService.update(upload);

		// init vars
		totalBytesUploaded = 0;

		if (!upload.getFile().exists()) {
			throw new FileNotFoundException("Datei existiert nicht.");
		}
	}

	private void upload() throws Exception {
		// Log operation
		LOGGER.debug("Uploaded {} bytes so far, using PUT method.", totalBytesUploaded);

		if (null == uploadProgress) {
			uploadProgress = new UploadJobProgressEvent(upload, upload.getFile().length());
			uploadProgress.setTime(Calendar.getInstance().getTimeInMillis());
		}

		final VideoStatus status = new VideoStatus();
		status.setPrivacyStatus("private");

		final VideoSnippet snippet = new VideoSnippet();
		snippet.setTitle(upload.getMetadata().getTitle());
		snippet.setDescription(upload.getMetadata().getDescription());
		snippet.setTags(TagParser.parse(upload.getMetadata().getKeywords(), true));

		final Video videoObjectDefiningMetadata = new Video();
		videoObjectDefiningMetadata.setStatus(status);
		videoObjectDefiningMetadata.setSnippet(snippet);

		try (final TokenInputStream tokenInputStream = new TokenInputStream(new FileInputStream(upload.getFile()))) {
			final InputStreamContent mediaContent = new InputStreamContent(upload.getMimetype(), tokenInputStream);
			mediaContent.setLength(upload.getFile().length());

			final YouTube youTube = youTubeProvider.setAccount(upload.getAccount()).get();
			final YouTube.Videos.Insert request = youTube.videos()
					.insert("snippet,statistics,status", videoObjectDefiningMetadata, mediaContent);
			// Set the upload type and add event listener.
			final MediaHttpUploader uploader = request.getMediaHttpUploader();

      /*
	   * Sets whether direct media upload is enabled or disabled. True = whole media content is
       * uploaded in a single request. False (default) = resumable media upload protocol to upload
       * in data chunks.
       */
			uploader.setDirectUploadEnabled(false);

			final Video[] video = new Video[1];

			final MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
				@Override
				public void progressChanged(final MediaHttpUploader uploader) throws IOException {
					switch (uploader.getUploadState()) {
						case INITIATION_STARTED:
							LOGGER.info("Initiation Started");
							break;
						case INITIATION_COMPLETE:
							LOGGER.info("Initiation Completed");
							break;
						case MEDIA_COMPLETE:
							LOGGER.debug("Upload created {} ", video[0].toPrettyString());

							upload.setVideoid(video[0].getId());
							upload.getStatus().setArchived(true);
							upload.getStatus().setFailed(false);
							uploadService.update(upload);
							break;
						case NOT_STARTED:
							LOGGER.info("Upload no started");
							break;
					}
				}
			};
			uploader.setProgressListener(progressListener);

			video[0] = request.execute();
		}
	}

	private class TokenInputStream extends BufferedInputStream {

		public TokenInputStream(final InputStream inputStream) {
			super(inputStream, DEFAULT_BUFFER_SIZE);
		}

		@Override
		public synchronized int read(final byte[] b, final int off, final int len) throws IOException {
			if (0 < rateLimiter.getRate()) {
				rateLimiter.acquire(b.length);
			}

			if (Thread.currentThread().isInterrupted()) {
				LOGGER.error("Upload aborted / stopped.");
				upload.getStatus().setAborted(true);
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
