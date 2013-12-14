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
import com.blogspot.nurkiewicz.asyncretry.function.RetryRunnable;
import com.google.api.client.auth.oauth2.Credential;
import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.eventbus.EventBus;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.common.util.concurrent.RateLimiter;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import de.chaosfisch.google.GDATAConfig;
import de.chaosfisch.google.YouTubeProvider;
import de.chaosfisch.google.youtube.upload.events.UploadJobProgressEvent;
import de.chaosfisch.google.youtube.upload.metadata.IMetadataService;
import de.chaosfisch.google.youtube.upload.metadata.MetaBadRequestException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import java.util.Set;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UploadJob implements Callable<Upload> {

    private static final int SC_OK = 200;
    private static final int SC_CREATED = 201;
    private static final int SC_MULTIPLE_CHOICES = 300;
    private static final int SC_RESUME_INCOMPLETE = 308;
    private static final int SC_BAD_REQUEST = 400;
    private static final long chunkSize = 10485760;
    private static final int DEFAULT_BUFFER_SIZE = 65536;
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadJob.class);
    private static final String METADATA_CREATE_RESUMEABLE_URL = "http://uploads.gdata.youtube.com/resumable/feeds/api/users/default/uploads";
    private static final Pattern RANGE_HEADER_PATTERN = Pattern.compile("-");
    private static final int SC_500 = 500;

    /**
     * File that is uploaded
     */
    private File fileToUpload;
    private long start;
    private long bytesToUpload;
    private long totalBytesUploaded;
    private long fileSize;

    private final Set<UploadPreProcessor> uploadPreProcessors;
    private final Set<UploadPostProcessor> uploadPostProcessors;
    private final EventBus eventBus;
    private final IUploadService uploadService;
    private final RateLimiter rateLimiter;

    private UploadJobProgressEvent uploadProgress;
    private Upload upload;
    private final YouTubeProvider youTubeProvider;
    private final IMetadataService metadataService;
    private Credential credential;

    @Inject
    private UploadJob(@Assisted final Upload upload, @Assisted final RateLimiter rateLimiter, final Set<UploadPreProcessor> uploadPreProcessors, final Set<UploadPostProcessor> uploadPostProcessors, final EventBus eventBus, final IUploadService uploadService, final YouTubeProvider youTubeProvider, final IMetadataService metadataService) {
        this.upload = upload;
        this.rateLimiter = rateLimiter;
        this.uploadPreProcessors = uploadPreProcessors;
        this.uploadPostProcessors = uploadPostProcessors;
        this.eventBus = eventBus;
        this.uploadService = uploadService;
        this.youTubeProvider = youTubeProvider;
        this.metadataService = metadataService;
        this.eventBus.register(this);
    }

    @Override
    public Upload call() throws Exception {

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
                .abortIf(new Predicate<Throwable>() {
                    @Override
                    public boolean apply(@Nullable final Throwable input) {
                        return input instanceof UploadResponseException && SC_500 >= ((UploadResponseException) input).getStatus();
                    }
                })
                .abortOn(MetaBadRequestException.class)
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
            upload.getStatus().setAborted(true);
        } catch (final Exception e) {
            if (!upload.getStatus().isArchived()) {
                LOGGER.error("Upload error", e);
                upload.getStatus().setFailed(true);
            }
        } finally {
            schedueler.shutdownNow();
            eventBus.unregister(this);
        }

        if (upload.getStatus().isArchived()) {
            LOGGER.info("Starting postprocessing");
            for (final UploadPostProcessor postProcessor : uploadPostProcessors) {
                try {
                    upload = postProcessor.process(upload);
                } catch (final Exception e) {
                    LOGGER.error("Postprocessor error", e);
                }
            }
        }

        upload.getStatus().setRunning(false);
        uploadService.update(upload);
        return upload;
    }

    private void initialize() throws FileNotFoundException {
        // Set the time uploaded started
        upload.setDateTimeOfStart(DateTime.now());
        uploadService.update(upload);

        // Get File and Check if existing
        fileToUpload = upload.getFile();

        if (!fileToUpload.exists()) {
            throw new FileNotFoundException("Datei existiert nicht.");
        }
    }

    private RetryRunnable metadata() {
        return new RetryRunnable() {

            @Override
            public void run(final RetryContext retryContext) throws IOException, MetaBadRequestException, UnirestException {
                fileSize = fileToUpload.length();
                totalBytesUploaded = 0;
                start = 0;
                bytesToUpload = fileSize;

                if (null != upload.getUploadurl() && !upload.getUploadurl().isEmpty()) {
                    LOGGER.info("Uploadurl existing: {}", upload.getUploadurl());
                    return;
                }

                upload.setUploadurl(fetchUploadUrl(upload));
                uploadService.update(upload);

                // Log operation
                LOGGER.info("Uploadurl received: {}", upload.getUploadurl());
            }
        };
    }

    private String fetchUploadUrl(final Upload upload) throws MetaBadRequestException, UnirestException, IOException {
        // Upload atomData and fetch uploadUrl
        final String atomData = metadataService.atomBuilder(upload);
        final HttpResponse<String> response = Unirest.post(METADATA_CREATE_RESUMEABLE_URL)
                .header("GData-Version", GDATAConfig.GDATA_V2)
                .header("X-GData-Key", "key=" + GDATAConfig.DEVELOPER_KEY)
                .header("Content-Type", "application/atom+xml; charset=UTF-8;")
                .header("Slug", fileToUpload.getName())
                .header("Authorization", getAuthHeader())
                .body(atomData)
                .asString();

        LOGGER.info("fetchUploadUrl response code: {}", response.getCode());
        LOGGER.info("fetchUploadUrl response headers: {}", response.getHeaders());
        LOGGER.info("fetchUploadUrl response: {}", response.getBody());
        // Check the response code for any problematic codes.
        if (SC_BAD_REQUEST == response.getCode()) {
            throw new MetaBadRequestException(atomData, response.getCode());
        }
        // Check if uploadurl is available
        if (response.getHeaders().containsKey("location")) {
            return response.getHeaders().get("location");
        } else {
            throw new MetaBadRequestException("Location missing", response.getCode());
        }
    }

    private String getAuthHeader() throws IOException {
        if (null == credential) {
            credential = youTubeProvider.getCredential(upload.getAccount());
        }

        if (null == credential.getAccessToken() || null != credential.getExpiresInSeconds() && 60 >= credential.getExpiresInSeconds()) {
            credential.refreshToken();
        }
        return String.format("Bearer %s", credential.getAccessToken());
    }

    private RetryRunnable upload() {
        return new RetryRunnable() {

            @Override
            public void run(final RetryContext retryContext) throws IOException, UploadResponseException, UploadFinishedException, UnirestException {
                if (null != upload.getUploadurl() || null != retryContext.getLastThrowable()) {
                    if (0 < retryContext.getRetryCount()) {
                        LOGGER.info("############ RETRY " + retryContext.getRetryCount() + " ############");
                    }
                    resumeinfo();
                }
                uploadChunks();
            }
        };
    }

    private void uploadChunks() throws IOException, UploadResponseException, UploadFinishedException {
        while (!Thread.currentThread().isInterrupted() && totalBytesUploaded != fileSize) {
            uploadChunk();
        }
    }

    private void uploadChunk() throws IOException, UploadResponseException, UploadFinishedException {
        // GET END SIZE
        final long end = generateEndBytes(start, bytesToUpload);

        // Log operation
        LOGGER.debug("start={} end={} filesize={}", start, end, fileSize);

        // Log operation
        LOGGER.debug("Uploaded {} bytes so far, using PUT method.", totalBytesUploaded);

        if (null == uploadProgress) {
            uploadProgress = new UploadJobProgressEvent(upload, upload.getFile().length());
            uploadProgress.setTime(Calendar.getInstance().getTimeInMillis());
        }

        // Calculating the chunk size
        final int chunk = (int) (end - start + 1);

        // Building PUT RequestImpl for chunk data
        final URL url = URI.create(upload.getUploadurl()).toURL();
        final HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setRequestMethod("POST");
        request.setDoOutput(true);
        request.setFixedLengthStreamingMode(chunk);
        //Properties
        request.setRequestProperty("Content-Type", upload.getMimetype());
        request.setRequestProperty("Content-Range", String.format("bytes %d-%d/%d", start, end, fileToUpload.length()));
        request.setRequestProperty("Authorization", getAuthHeader());
        request.setRequestProperty("GData-Version", GDATAConfig.GDATA_V2);
        request.setRequestProperty("X-GData-Key", String.format("key=%s", GDATAConfig.DEVELOPER_KEY));
        request.connect();

        try (final TokenInputStream tokenInputStream = new TokenInputStream(new FileInputStream(upload.getFile()));
             final BufferedOutputStream throttledOutputStream = new BufferedOutputStream(request.getOutputStream())) {
            tokenInputStream.skip(start);
            flowChunk(tokenInputStream, throttledOutputStream, start, end);

            switch (request.getResponseCode()) {
                case SC_OK:
                case SC_CREATED:
                    //FILE UPLOADED
                    final InputSupplier<InputStream> supplier = new InputSupplier<InputStream>() {
                        @Override
                        public InputStream getInput() throws IOException {
                            return request.getInputStream();
                        }
                    };
                    handleSuccessfulUpload(CharStreams.toString(CharStreams.newReaderSupplier(supplier, Charsets.UTF_8)));

                    break;
                case SC_RESUME_INCOMPLETE:
                    // OK, the chunk completed succesfully
                    LOGGER.debug("responseMessage={}", request.getResponseMessage());
                    break;
                default:
                    throw new UploadResponseException(request.getResponseCode());
            }

            bytesToUpload -= chunkSize;
            start = end + 1;
        }
    }

    private void resumeinfo() throws UploadFinishedException, UploadResponseException, UnirestException, IOException {
        final HttpResponse<String> response = Unirest.put(upload.getUploadurl())
                .header("GData-Version", GDATAConfig.GDATA_V2)
                .header("X-GData-Key", "key=" + GDATAConfig.DEVELOPER_KEY)
                .header("Content-Type", "application/atom+xml; charset=UTF-8;")
                .header("Authorization", getAuthHeader())
                .header("Content-Range", "bytes */*")
                .asString();

        if (SC_OK <= response.getCode() && SC_MULTIPLE_CHOICES > response.getCode()) {
            handleSuccessfulUpload(response.getBody());
        } else if (SC_RESUME_INCOMPLETE != response.getCode()) {
            throw new UploadResponseException(response.getCode());
        }

        if (!response.getHeaders().containsKey("range")) {
            LOGGER.info("PUT to {} did not return Range-header.", upload.getUploadurl());
            totalBytesUploaded = 0;
        } else {
            LOGGER.info("Range header is: {}", response.getHeaders().get("range"));

            final String[] parts = RANGE_HEADER_PATTERN.split(response.getHeaders().get("range"));
            if (1 < parts.length) {
                totalBytesUploaded = Long.parseLong(parts[1]) + 1;
            } else {
                totalBytesUploaded = 0;
            }

            bytesToUpload = fileSize - totalBytesUploaded;
            start = totalBytesUploaded;
            LOGGER.info("Next byte to upload is {}.", start);
        }
        if (response.getHeaders().containsKey("location")) {
            upload.setUploadurl(response.getHeaders().get("location"));
            uploadService.update(upload);
        }
    }

    private void handleSuccessfulUpload(final String body) throws UploadFinishedException {
        upload.setVideoid(parseVideoId(body));
        upload.getStatus().setArchived(true);
        uploadService.update(upload);
        throw new UploadFinishedException();
    }

    String parseVideoId(final String atomData) {
        LOGGER.info(atomData);
        final Pattern pattern = Pattern.compile("<yt:videoid>(.*)</yt:videoid>");
        final Matcher matcher = pattern.matcher(atomData);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "missed";
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

    private void flowChunk(final InputStream inputStream, final OutputStream outputStream, final long startByte, final long endByte) throws IOException {

        // Write Chunk
        final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long totalRead = 0;

        while (!Thread.currentThread().isInterrupted() && totalRead != endByte - startByte + 1) {
            // Upload bytes in buffer
            final int bytesRead = flowChunk(inputStream, outputStream, buffer, 0, DEFAULT_BUFFER_SIZE);
            // Calculate all uploadinformation
            totalRead += bytesRead;
        }
    }

    int flowChunk(final InputStream is, final OutputStream os, final byte[] buf, final int off, final int len) throws IOException {
        final int numRead;
        if (0 <= (numRead = is.read(buf, off, len))) {
            os.write(buf, 0, numRead);
        }
        os.flush();
        return numRead;
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
}
