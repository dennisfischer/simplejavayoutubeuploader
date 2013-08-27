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

import com.blogspot.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.blogspot.nurkiewicz.asyncretry.RetryContext;
import com.blogspot.nurkiewicz.asyncretry.RetryExecutor;
import com.blogspot.nurkiewicz.asyncretry.function.RetryCallable;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.model.Video;
import com.google.common.base.Charsets;
import com.google.common.eventbus.EventBus;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.common.util.concurrent.RateLimiter;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import de.chaosfisch.google.account.IAccountService;
import de.chaosfisch.google.youtube.upload.events.UploadJobFinishedEvent;
import de.chaosfisch.google.youtube.upload.events.UploadJobProgressEvent;
import de.chaosfisch.google.youtube.upload.metadata.MetaBadRequestException;
import de.chaosfisch.google.youtube.upload.metadata.MetaLocationMissingException;
import de.chaosfisch.util.RegexpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.*;

public class UploadJob implements Callable<Upload> {

	private static final int    SC_OK                    = 200;
	private static final int    SC_CREATED               = 201;
	private static final int    SC_RESUME_INCOMPLETE     = 308;
	private static final int    SC_INTERNAL_SERVER_ERROR = 500;
	private static final int    SC_BAD_GATEWAY           = 502;
	private static final int    SC_SERVICE_UNAVAILABLE   = 503;
	private static final int    SC_GATEWAY_TIMEOUT       = 504;
	private static final int    DEFAULT_BUFFER_SIZE      = 65536;
	private static final int    MAX_DELAY                = 30000;
	private static final int    INITIAL_DELAY            = 5000;
	private static final double MULTIPLIER_DELAY         = 2;
	private static final int    MAX_RETRIES              = 5;
	private static final Logger logger                   = LoggerFactory.getLogger(UploadWorker.class);

	private final EventBus        eventBus;
	private final IUploadService  uploadService;
	private final IAccountService accountService;
	private final RateLimiter     rateLimiter;

	private UploadJobProgressEvent uploadProgress;
	private Upload                 upload;
	private boolean                isResuming;
	private File                   fileToUpload;
	private long                   totalBytesUploaded;
	private long                   fileSize;
	private long                   start;
	private long                   end;
	private boolean                canceled;

	@Inject
	public UploadJob(@Assisted final Upload upload, @Assisted final RateLimiter rateLimiter, final EventBus eventBus, final IUploadService uploadService, final IAccountService accountService) {
		this.upload = upload;
		this.rateLimiter = rateLimiter;
		this.eventBus = eventBus;
		this.uploadService = uploadService;
		this.accountService = accountService;
		this.eventBus.register(this);
	}

	@Override
	public Upload call() throws Exception {

		final ScheduledExecutorService schedueler = Executors.newSingleThreadScheduledExecutor();
		final RetryExecutor executor = new AsyncRetryExecutor(schedueler).withExponentialBackoff(INITIAL_DELAY, MULTIPLIER_DELAY)
				.withMaxDelay(MAX_DELAY)
				.withMaxRetries(MAX_RETRIES)
				.retryOn(Exception.class)
				.abortOn(InterruptedException.class)
				.abortOn(CancellationException.class)
				.abortOn(ExecutionException.class)
				.abortOn(MetaBadRequestException.class)
				.abortOn(UploadFinishedException.class)
				.abortOn(UploadResponseException.class);
		try {
			// Schritt 1: Initialize
			initialize();
			// Schritt 2: MetadataUpload + UrlFetch
			executor.getWithRetry(metadata()).get();
			// Schritt 3: Upload
			executor.getWithRetry(upload()).get();
			eventBus.post(new UploadJobFinishedEvent(upload));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("Upload aborted / stopped.");
			upload.getStatus().setAborted(true);
		} catch (Exception e) {
			logger.error("Upload error", e);
			upload.getStatus().setFailed(true);
		}

		upload.getStatus().setRunning(false);
		uploadService.update(upload);
		schedueler.shutdownNow();
		eventBus.unregister(this);
		canceled = true;
		return upload;
	}

	private void initialize() throws FileNotFoundException {
		// Set the time uploaded started
		final GregorianCalendar calendar = new GregorianCalendar();
		upload.setDateOfStart(calendar);
		uploadService.update(upload);

		// init vars
		fileToUpload = upload.getFile();
		fileSize = fileToUpload.length();
		totalBytesUploaded = 0;
		start = 0;
		end = fileSize - 1;

		if (!fileToUpload.exists()) {
			throw new FileNotFoundException("Datei existiert nicht.");
		}
	}

	private RetryCallable<Void> metadata() {

		return new RetryCallable<Void>() {
			@Override
			public Void call(final RetryContext retryContext) throws MetaLocationMissingException, MetaBadRequestException, UploadFinishedException, UploadResponseException, IOException {
				if (null != upload.getUploadurl() && !upload.getUploadurl().isEmpty()) {
					logger.info("Uploadurl existing: {}", upload.getUploadurl());
					resumeinfo();
					return null;
				}

				upload.setUploadurl(uploadService.fetchUploadUrl(upload));
				uploadService.update(upload);

				// Log operation
				logger.info("Uploadurl received: {}", upload.getUploadurl());
				return null;
			}
		};
	}

	private RetryCallable<Void> upload() {
		return new RetryCallable<Void>() {

			@Override
			public Void call(final RetryContext retryContext) throws UploadFinishedException, UploadResponseException, IOException {
				try {
					if (null != retryContext.getLastThrowable()) {
						throw retryContext.getLastThrowable();
					}
				} catch (Throwable e) {
					resumeinfo();
				}
				uploadChunk();

				return null;
			}
		};
	}

	private void uploadChunk() throws UploadResponseException, IOException, UploadFinishedException {
		// Log operation
		logger.debug("start={} end={} filesize={}", start, end, fileSize);

		// Log operation
		logger.debug("Uploaded {} bytes so far, using PUT method.", totalBytesUploaded);

		if (null == uploadProgress) {
			uploadProgress = new UploadJobProgressEvent(upload, fileSize);
			uploadProgress.setTime(Calendar.getInstance().getTimeInMillis());
		}

		// Building PUT RequestImpl for chunk data
		final URL url = URI.create(upload.getUploadurl()).toURL();
		final HttpURLConnection request = (HttpURLConnection) url.openConnection();
		request.setRequestMethod("PUT");
		request.setDoOutput(true);
		request.setChunkedStreamingMode((int) fileSize);
		//Properties
		request.setRequestProperty("Content-Type", upload.getMimetype());
		request.setRequestProperty("Content-Length", String.format("%d", fileSize - start));
		if (isResuming) {
			request.setRequestProperty("Content-Range", String.format("bytes %d-%d/%d", start, end, fileSize));
		}
		request.setRequestProperty("Authorization", accountService.getAuthentication(upload.getAccount()).getHeader());
		request.connect();

		try (final TokenOutputStream tokenOutputStream = new TokenOutputStream(request.getOutputStream(), this)) {

			final InputSupplier<InputStream> fileInputSupplier = ByteStreams.slice(Files.newInputStreamSupplier(fileToUpload), start, end);
			ByteStreams.copy(fileInputSupplier, tokenOutputStream);

			final int code = request.getResponseCode();
			switch (code) {
				case SC_OK:
				case SC_CREATED:

					final JsonFactory factory = new GsonFactory();
					final Video video = factory.fromInputStream(request.getInputStream(), Charsets.UTF_8, Video.class);
					logger.debug("Upload created {} ", video.toPrettyString());

					upload.setVideoid(video.getId());
					upload.getStatus().setArchived(true);
					upload.getStatus().setFailed(false);
					uploadService.update(upload);
					break;
				case SC_RESUME_INCOMPLETE:
					System.out.println("Why is this called?");
					break;

				case SC_INTERNAL_SERVER_ERROR:
				case SC_BAD_GATEWAY:
				case SC_SERVICE_UNAVAILABLE:
				case SC_GATEWAY_TIMEOUT:
					throw new IOException(String.format("Unexepected response: %d", code));
				default:
					throw new UploadResponseException(code);
			}
		}
	}

	private void resumeinfo() throws UploadFinishedException, UploadResponseException, IOException {
		isResuming = true;
		fetchResumeInfo(upload);

		logger.info("Resuming stalled upload to: {}", upload.getUploadurl());

		totalBytesUploaded = start;
		// possibly rolling back the previously saved value
		fileSize = fileToUpload.length();
		logger.info("Next byte to upload is {}-{}.", start, end);
	}

	private void fetchResumeInfo(final Upload upload) throws IOException, UploadFinishedException, UploadResponseException {
		final HttpResponse<String> response = Unirest.put(upload.getUploadurl())
				.header("Content-Type", "application/atom+xml; charset=UTF-8;")
				.header("Content-Range", String.format("bytes */%d", fileSize))
				.header("Authorization", accountService.getAuthentication(upload.getAccount()).getHeader())
				.asString();

		switch (response.getCode()) {
			case SC_CREATED:
				logger.debug("Upload created {} ", response.getBody());
				throw new UploadFinishedException();
						/*
						upload.setVideoid(resumeableManager.parseVideoId());
						uploadService.update(upload);
						*/
			case SC_INTERNAL_SERVER_ERROR:
			case SC_BAD_GATEWAY:
			case SC_SERVICE_UNAVAILABLE:
			case SC_GATEWAY_TIMEOUT:
				throw new IOException(String.format("Unexepected response: %d", response.getCode()));
			default:
				throw new UploadResponseException(response.getCode());
			case SC_RESUME_INCOMPLETE:
				start = 0;
				end = fileSize - 1;

				if (!response.getHeaders().containsKey("Range")) {
					logger.info("PUT to {} did not return Range-header.", upload.getUploadurl());
				} else {
					logger.info("Range header is: {}", response.getHeaders().get("Range"));

					final String[] parts = RegexpUtils.getPattern("-").split(response.getHeaders().get("Range"));
					if (1 < parts.length) {
						start = Long.parseLong(parts[1]) + 1;
					}
				}
				if (response.getHeaders().containsKey("Location")) {
					upload.setUploadurl(response.getHeaders().get("Location"));
					uploadService.update(upload);
				}
				break;
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

	private static class UploadFinishedException extends Exception {
		private static final long serialVersionUID = -9034528149972478083L;
	}

	private static class TokenOutputStream extends BufferedOutputStream {
		private final UploadJob job;

		public TokenOutputStream(final OutputStream outputStream, final UploadJob job) {
			super(outputStream, DEFAULT_BUFFER_SIZE);
			this.job = job;
		}

		@Override
		public synchronized void write(final byte[] b, final int off, final int len) throws IOException {
			if (0 < job.rateLimiter.getRate()) {
				job.rateLimiter.acquire(b.length);
			}
			super.write(b, off, len);
			if (job.canceled) {
				throw new CancellationException("Cancled");
			}

			flush();
			// Event Upload Progress
			// Calculate all uploadinformation
			job.totalBytesUploaded += b.length;
			final long diffTime = Calendar.getInstance().getTimeInMillis() - job.uploadProgress.getTime();
			if (1000 < diffTime) {
				job.uploadProgress.setBytes(job.totalBytesUploaded);
				job.uploadProgress.setTime(diffTime);
				job.eventBus.post(job.uploadProgress);
			}
		}
	}
}
