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

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.*;

public class Uploader {

	private static final String                                    STOP_ON_ERROR        = "stopOnError";
	private static final int                                       ENQUEUE_WAIT_TIME    = 10000;
	private static final int                                       DEFAULT_MAX_UPLOADS  = 1;
	private              int                                       maxUploads           = DEFAULT_MAX_UPLOADS;
	private static final int                                       ONE_KILOBYTE         = 1024;
	private static final Logger                                    logger               = LoggerFactory.getLogger(Uploader.class);
	private final        ExecutorService                           executorService      = Executors.newFixedThreadPool(10);
	private final        CompletionService<UploadModel>            jobCompletionService = new ExecutorCompletionService<>(executorService);
	private final        ScheduledExecutorService                  timer                = Executors.newSingleThreadScheduledExecutor();
	private final        RateLimiter                               rateLimitter         = RateLimiter.create(Double.MAX_VALUE);
	private final        HashMap<UploadModel, Future<UploadModel>> futures              = Maps.newHashMapWithExpectedSize(10);
	private final IUploadJobFactory     uploadJobFactory;
	private final Configuration         configuration;
	private       int                   runningUploads;
	private       IUploadService        uploadService;
	private       UploadFinishProcessor consumer;
	private       ScheduledFuture<?>    task;

	@Inject
	public Uploader(final IUploadJobFactory uploadJobFactory, final Configuration configuration) {
		this.uploadJobFactory = uploadJobFactory;
		this.configuration = configuration;
	}

	public void setMaxUploads(final int maxUploads) {
		this.maxUploads = maxUploads;
		if (canAddJob()) {
			startNextUpload();
		}
	}

	private void startNextUpload() {
		if (canAddJob()) {
			final UploadModel nextUpload = uploadService.fetchNextUpload();
			if (null != nextUpload) {
				createConsumer();
				markUploadRunning(nextUpload);
				futures.put(nextUpload, jobCompletionService.submit(uploadJobFactory.create(nextUpload, rateLimitter)));
				runningUploads++;
			}
		}
	}

	private void markUploadRunning(final UploadModel nextUpload) {
		nextUpload.setStatus(Status.RUNNING);
		uploadService.update(nextUpload);
	}

	private void createConsumer() {
		if (null != consumer && consumer.isAlive()) {
			return;
		}
		consumer = new UploadFinishProcessor();
		consumer.setDaemon(true);
		consumer.start();
	}

	private boolean canAddJob() {
		return uploadService.getRunning() && maxUploads > runningUploads;
	}

	public void run() {
		if (uploadService.getRunning()) {
			return;
		}
		uploadService.setRunning(true);

		final Thread thread = new Thread(() -> {
			while (canAddJob() && hasJobs()) {
				startNextUpload();

				try {
					Thread.sleep(ENQUEUE_WAIT_TIME);
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}, "Enqueue-Thread");
		thread.setDaemon(true);
		thread.start();
	}

	private boolean hasJobs() {
		return 0 < uploadService.countUnprocessed();
	}

	public void shutdown() {
		shutdown(false);
	}

	void shutdown(final boolean force) {
		uploadService.setRunning(false);

		if (force) {
			for (final Map.Entry<UploadModel, Future<UploadModel>> job : futures.entrySet()) {
				job.getValue().cancel(true);
				futures.remove(job.getKey());
			}
		}
	}

	public void abort(final UploadModel upload) {
		futures.get(upload).cancel(true);
	}

	public void setUploadService(final IUploadService uploadService) {
		this.uploadService = uploadService;
	}

	public void setMaxSpeed(final int maxSpeed) {
		rateLimitter.setRate(0 == maxSpeed ? Double.MAX_VALUE : maxSpeed * ONE_KILOBYTE);
	}

	public void runStarttimeChecker() {
		logger.debug("Running starttime checker");
		final long delay = uploadService.getStarttimeDelay();
		logger.debug("Delay to upload is {}", delay);
		final TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				if (0 < uploadService.countReadyStarttime()) {
					Uploader.this.run();
				}
				runStarttimeChecker();
			}
		};
		if (-1 != delay && (0 == runningUploads || canAddJob())) {
			task = timer.schedule(timerTask, delay, TimeUnit.MILLISECONDS);
		}
	}

	/* TODO
	@Subscribe
	public void onUploadEvent(final UploadEvent uploadEvent) {
		if (null != task && !task.isCancelled()) {
			task.cancel(false);
		}
		runStarttimeChecker();
	}
*/
	public void stopStarttimeChecker() {
		timer.shutdownNow();
		executorService.shutdownNow();
	}

	private class UploadFinishProcessor extends Thread {
		public UploadFinishProcessor() {
			super("Upload Finish Processor-Thread");
		}

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				final UploadModel upload = getUpload();
				removeUpload(upload);
				updateQueueStatus(upload);
				final long leftUploads = uploadService.countUnprocessed();
				logger.info("Left uploads: {}", leftUploads);
				startNextUpload();

				if ((!uploadService.getRunning() || 0 == leftUploads) && 0 == runningUploads) {
					uploadService.setRunning(false);
					logger.info("All uploads finished");
				}
			}
		}

		private UploadModel getUpload() {
			try {
				final Future<UploadModel> uploadJobFuture = jobCompletionService.take();
				final UploadModel upload = uploadJobFuture.get();
				logger.info("Upload finished: {}", upload);
				return upload;
			} catch (ExecutionException | CancellationException | InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			} finally {
				runningUploads--;
			}
		}

		private void updateQueueStatus(final UploadModel upload) {
			if (Status.FAILED == upload.getStatus() && configuration.getBoolean(STOP_ON_ERROR, false)) {
				uploadService.stopUploading();
			}
		}

		private void removeUpload(final UploadModel upload) {
			futures.remove(upload);
			if (null != upload) {
				logger.info("Running uploads: {}", runningUploads);

				if (upload.getStopAfter()) {
					uploadService.setRunning(false);
				}
			}
		}
	}
}
