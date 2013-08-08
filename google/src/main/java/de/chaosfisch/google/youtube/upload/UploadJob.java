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
import com.blogspot.nurkiewicz.asyncretry.RetryExecutor;
import com.google.common.base.Charsets;
import com.google.common.eventbus.EventBus;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.chaosfisch.google.account.IAccountService;
import de.chaosfisch.google.auth.IGoogleRequestSigner;
import de.chaosfisch.google.youtube.upload.events.UploadJobProgressEvent;
import de.chaosfisch.google.youtube.upload.metadata.MetaBadRequestException;
import de.chaosfisch.google.youtube.upload.metadata.MetaIOException;
import de.chaosfisch.google.youtube.upload.metadata.MetaLocationMissingException;
import de.chaosfisch.google.youtube.upload.resume.IResumeableManager;
import de.chaosfisch.google.youtube.upload.resume.ResumeInfo;
import de.chaosfisch.http.IRequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class UploadJob implements Callable<Upload> {

	/** Status enum for handling control flow */
	protected enum STATUS {
		ABORTED, DONE, FAILED, INITIALIZE, METADATA, POSTPROCESS, RESUMEINFO, UPLOAD
	}

	private static final int  SC_OK                = 200;
	private static final int  SC_CREATED           = 201;
	private static final int  SC_RESUME_INCOMPLETE = 308;
	private static final long chunkSize            = 10485760;

	private static final int    DEFAULT_BUFFER_SIZE = 65536;
	private              STATUS currentStatus       = STATUS.INITIALIZE;

	/** File that is uploaded */
	private File fileToUpload;
	private long start;
	private long totalBytesUploaded;
	private long bytesToUpload;
	private long fileSize;

	private       Upload          upload;
	private final EventBus        eventBus;
	private final IUploadService  uploadService;
	private final IAccountService accountService;

	@Inject
	private IRequestUtil         requestUtil;
	@Inject
	private IGoogleRequestSigner requestSigner;
	@Inject
	private IResumeableManager   resumeableManager;

	private static final Logger logger = LoggerFactory.getLogger(UploadWorker.class);

	private UploadJobProgressEvent uploadProgress;

	@Inject
	public UploadJob(@Assisted final Upload upload, final EventBus eventBus, final IUploadService uploadService, final IAccountService accountService) {
		this.upload = upload;
		this.eventBus = eventBus;
		this.uploadService = uploadService;
		this.accountService = accountService;
		this.eventBus.register(this);
	}

	@Override
	public Upload call() throws Exception {

		final ScheduledExecutorService schedueler = Executors.newSingleThreadScheduledExecutor();
		final RetryExecutor executor = new AsyncRetryExecutor(schedueler).withExponentialBackoff(5000, 2)
				.withMaxDelay(30000)
				.withMaxRetries(5)
				.retryOn(MetaIOException.class)
				.retryOn(MetaLocationMissingException.class)
				.abortOn(MetaBadRequestException.class)
				.abortOn(FileNotFoundException.class);

		// Schritt 1: Initialize
		executor.getWithRetry(initialize()).get();
		// Schritt 2: MetadataUpload + UrlFetch
		executor.getWithRetry(metadata()).get();
		// Schritt 3: Chunkupload
		executor.getWithRetry(upload()).get();
		// Schritt 4: Fetchen des Resumeinfo
		executor.getWithRetry(resumeinfo()).get();
		// Schritt 5: Postprocessing

		eventBus.unregister(this);
		return upload;
	}

	private Callable<Void> initialize() {

		return new Callable<Void>() {
			@Override
			public Void call() throws FileNotFoundException {
				// Set the time uploaded started
				final GregorianCalendar calendar = new GregorianCalendar();
				upload.setDateOfStart(calendar);
				uploadService.update(upload);

				// Get File and Check if existing
				fileToUpload = upload.getFile();

				if (!fileToUpload.exists()) {
					throw new FileNotFoundException("Datei existiert nicht.");
				}

				return null;
			}
		};
	}

	private Callable<Void> metadata() {

		return new Callable<Void>() {
			@Override
			public Void call() throws MetaLocationMissingException, MetaBadRequestException, MetaIOException {
				if (null != upload.getUploadurl() && !upload.getUploadurl().isEmpty()) {
					logger.info("Uploadurl existing: {}", upload.getUploadurl());
					currentStatus = STATUS.RESUMEINFO;
					return null;
				}

				upload.setUploadurl(uploadService.fetchUploadUrl(upload));
				uploadService.update(upload);

				// Log operation
				logger.info("Uploadurl received: {}", upload.getUploadurl());
				// INIT Vars
				fileSize = fileToUpload.length();
				totalBytesUploaded = 0;
				start = 0;
				bytesToUpload = fileSize;
				currentStatus = STATUS.UPLOAD;
				return null;
			}
		};
	}

	private Callable<Void> upload() {
		return new Callable<Void>() {
			@Override
			public Void call() throws Exception {

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
					request.setRequestProperty("Content-Range", String.format("bytes %d-%d/%d", start, end, fileToUpload
							.length()));
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
				return null;
			}
		};
	}

	private Callable<Void> resumeinfo() {
		return new Callable<Void>() {
			@Override
			public Void call() throws Exception {
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

				return null;
			}
		};
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

	private void flowChunk(final InputStream inputStream, final OutputStream outputStream, final long startByte, final long endByte) throws IOException {

		// Write Chunk
		final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		long totalRead = 0;

		while (!Thread.currentThread()
				.isInterrupted() && STATUS.UPLOAD == currentStatus && totalRead != endByte - startByte + 1) {
			// Upload bytes in buffer
			final int bytesRead = requestUtil.flowChunk(inputStream, outputStream, buffer, 0, DEFAULT_BUFFER_SIZE);
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

	public void updateUpload(final Upload received) {
		if (upload.equals(received)) {
			upload = received;
			if (null != uploadProgress) {
				uploadProgress.setUpload(upload);
			}
		}
	}

}
