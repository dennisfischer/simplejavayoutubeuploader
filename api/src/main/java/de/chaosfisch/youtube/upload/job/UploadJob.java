/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.upload.job;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.RateLimiter;
import de.chaosfisch.youtube.YouTubeFactory;
import de.chaosfisch.youtube.upload.IUploadService;
import de.chaosfisch.youtube.upload.Status;
import de.chaosfisch.youtube.upload.UploadModel;
import de.chaosfisch.youtube.upload.metadata.IMetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

public class UploadJob implements Callable<UploadModel> {

	private static final int    DEFAULT_BUFFER_SIZE = 65536;
	private static final Logger LOGGER              = LoggerFactory.getLogger(UploadJob.class);
	private final Collection<UploadJobPreProcessor>   preProcessors;
	private final Collection<UploadeJobPostProcessor> postProcessors;
	private final IUploadService                      uploadService;
	private final Optional<EventBus>                  eventBus;
	private final IMetadataService                    metadataService;
	private       RateLimiter                         rateLimiter;

	private long                   totalBytesUploaded;
	private UploadJobProgressEvent uploadProgress;
	private UploadModel            upload;

	private UploadJob(final Builder builder) {
		upload = builder.upload;
		uploadService = builder.uploadService;
		preProcessors = builder.preProcessors;
		postProcessors = builder.postProcessors;
		metadataService = builder.metadataService;
		eventBus = Optional.ofNullable(builder.eventBus);
		rateLimiter = builder.rateLimiter;
	}

	public void setUpload(final UploadModel upload) {
		this.upload = upload;
	}

	@Override
	public UploadModel call() throws Exception {
		if (null == upload.getUploadurl()) {
			for (final UploadJobPreProcessor preProcessor : preProcessors) {
				try {
					upload = preProcessor.process(upload);
				} catch (final Exception e) {
					LOGGER.error("Preprocessor error", e);
				}
			}
		}

		try {
			upload();
		} catch (final Exception e) {
			if (Status.FINISHED != upload.getStatus()) {
				LOGGER.error("Upload error", e);
				upload.setStatus(Status.FAILED);
			}
		} finally {
			eventBus.ifPresent(t -> t.unregister(this));
		}

		if (Status.FINISHED == upload.getStatus()) {
			LOGGER.info("Starting postprocessing");
			for (final UploadeJobPostProcessor postProcessor : postProcessors) {
				try {
					upload = postProcessor.process(upload);
				} catch (final Exception e) {
					LOGGER.error("Postprocessor error", e);
				}
			}
		}

		uploadService.store(upload);
		return upload;
	}

	private void upload() {
		// Set the time uploaded started
		upload.setDateTimeOfStart(ZonedDateTime.now().minusSeconds(1));
		uploadService.store(upload);

		if (null == uploadProgress) {
			uploadProgress = new UploadJobProgressEvent();
			uploadProgress.setTime(Calendar.getInstance().getTimeInMillis());
		}

		// Upload atomData and fetch uploadUrl
		final Video video = metadataService.buildVideoEntry(upload);

		try {
			final InputStreamContent mediaContent = new InputStreamContent("video/*", new TokenInputStream(new FileInputStream(upload.getFile())));
			final YouTube.Videos.Insert videoInsert = YouTubeFactory.getYouTube(upload.getAccount()).videos().insert("snippet,status", video, mediaContent);

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

			upload.setStatus(Status.FINISHED);
			uploadService.store(upload);
		} catch (final GoogleJsonResponseException e) {
			e.printStackTrace();
			System.err.println("GoogleJsonResponseException code: " + e.getDetails().getCode() + " : " + e.getDetails().getMessage());
		} catch (final IOException e) {
			e.printStackTrace();
			System.err.println("IOException: " + e.getMessage());
		} catch (final Throwable t) {
			t.printStackTrace();
			System.err.println("Throwable: " + t.getMessage());
		}

	}

	public void setRateLimiter(final RateLimiter rateLimiter) {
		this.rateLimiter = rateLimiter;
	}

	public static class Builder {

		public final  IMetadataService                    metadataService;
		private final UploadModel                         upload;
		private final IUploadService                      uploadService;
		private       Collection<UploadJobPreProcessor>   preProcessors;
		private       Collection<UploadeJobPostProcessor> postProcessors;
		private       RateLimiter                         rateLimiter;
		private       EventBus                            eventBus;

		public Builder(final UploadModel upload, final IUploadService uploadService, final IMetadataService metadataService) {
			this.upload = upload;
			this.uploadService = uploadService;
			this.metadataService = metadataService;
		}

		public Builder withPreProcessors(final Collection<UploadJobPreProcessor> preProcessors) {
			this.preProcessors = preProcessors;
			return this;
		}

		public Builder withPostProcessors(final Collection<UploadeJobPostProcessor> postProcessors) {
			this.postProcessors = postProcessors;
			return this;
		}

		public Builder withRateLimiter(final RateLimiter rateLimiter) {
			this.rateLimiter = rateLimiter;
			return this;
		}

		public Builder withEventBus(final EventBus eventBus) {
			this.eventBus = eventBus;
			return this;
		}

		public UploadJob build() {
			if (null == rateLimiter) {
				throw new IllegalArgumentException("Rate limiter missing for uploadJob");
			}
			return new UploadJob(this);
		}
	}

	private class TokenInputStream extends BufferedInputStream {

		public TokenInputStream(final InputStream inputStream) {
			super(inputStream, DEFAULT_BUFFER_SIZE);
		}

		@Override
		public synchronized int read(final byte[] b, final int off, final int len) throws IOException {
			rateLimiter.acquire(b.length);

			if (Thread.currentThread().isInterrupted()) {
				LOGGER.error("Upload aborted / stopped.");
				upload.setStatus(Status.ABORTED);
				throw new CancellationException("Thread cancled");
			}

			final int bytes = super.read(b, off, len);

			eventBus.ifPresent(e -> {
				// Event Upload Progress
				// Calculate all uploadinformation
				totalBytesUploaded += b.length;
				final long diffTime = Calendar.getInstance().getTimeInMillis() - uploadProgress.getTime();
				if (1000 < diffTime) {
					uploadProgress.setBytes(totalBytesUploaded);
					uploadProgress.setTime(diffTime);
					e.post(uploadProgress);
				}
			});

			return bytes;
		}
	}
}
