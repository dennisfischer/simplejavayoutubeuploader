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

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.RateLimiter;
import com.google.inject.Inject;
import de.chaosfisch.google.youtube.upload.events.UploadEvent;
import de.chaosfisch.google.youtube.upload.events.UploadFinishedEvent;
import de.chaosfisch.google.youtube.upload.events.UploadJobFinishedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.*;

public class Uploader {

	private static final int    ENQUEUE_WAIT_TIME   = 10000;
	private static final int    DEFAULT_MAX_UPLOADS = 1;
	private static final int    ONE_KILOBYTE        = 1024;
	private static final Logger logger              = LoggerFactory.getLogger(Uploader.class);

	private final ExecutorService                 executorService      = Executors.newFixedThreadPool(10);
	private final ScheduledExecutorService        timer                = Executors.newSingleThreadScheduledExecutor();
	private final RateLimiter                     rateLimitter         = RateLimiter.create(Double.MAX_VALUE);
	private final CompletionService<Upload>       jobCompletionService = new ExecutorCompletionService<>(executorService);
	private final HashMap<Upload, Future<Upload>> futures              = Maps.newHashMapWithExpectedSize(10);

	private final EventBus              eventBus;
	private final IUploadJobFactory     uploadJobFactory;
	private       int                   runningUploads;
	private       IUploadService        uploadService;
	private       UploadFinishProcessor consumer;
	private       ScheduledFuture<?>    task;

	private int maxUploads = DEFAULT_MAX_UPLOADS;

	@Inject
	public Uploader(final EventBus eventBus, final IUploadJobFactory uploadJobFactory) {
		this.eventBus = eventBus;
		this.uploadJobFactory = uploadJobFactory;
	}

	private void createConsumer() {
		if (null != consumer && consumer.isAlive()) {
			return;
		}
		consumer = new UploadFinishProcessor();
		consumer.setDaemon(true);
		consumer.start();
	}

	public void setMaxUploads(final int maxUploads) {
		this.maxUploads = maxUploads;
		if (canAddJob()) {
			enqueueUpload();
		}
	}

	private boolean canAddJob() {
		return uploadService.getRunning() && maxUploads > runningUploads;
	}

	void shutdown(final boolean force) {
		uploadService.setRunning(false);

		if (force) {
			for (final Map.Entry<Upload, Future<Upload>> job : futures.entrySet()) {
				job.getValue().cancel(true);
				futures.remove(job.getKey());
			}
		}
	}

	public void run() {
		if (uploadService.getRunning()) {
			return;
		}
		uploadService.setRunning(true);

		final Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (canAddJob() && hasJobs()) {
					enqueueUpload();

					try {
						Thread.sleep(ENQUEUE_WAIT_TIME);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
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

	public void abort(final Upload upload) {
		futures.get(upload).cancel(true);
	}

	private void enqueueUpload() {
		if (canAddJob()) {
			final Upload polled = uploadService.fetchNextUpload();
			if (null != polled) {
				if (null == polled.getAccount()) {
					polled.getStatus().setLocked(true);
					uploadService.update(polled);
				} else {
					createConsumer();
					polled.getStatus().setRunning(true);
					uploadService.update(polled);
					futures.put(polled, jobCompletionService.submit(uploadJobFactory.create(polled, rateLimitter)));
					runningUploads++;
				}
			}
		}
	}

	public void setUploadService(final IUploadService uploadService) {
		this.uploadService = uploadService;
	}

	public void setMaxSpeed(final int maxSpeed) {
		rateLimitter.setRate(0 == maxSpeed ? Double.MAX_VALUE : maxSpeed * ONE_KILOBYTE);
	}

	private class UploadFinishProcessor extends Thread {
		public UploadFinishProcessor() {
			super("Upload Finish Processor-Thread");
		}

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				final Upload upload = getUpload();
				futures.remove(upload);
				if (null != upload) {
					logger.info("Running uploads: {}", runningUploads);

					if (upload.isPauseOnFinish()) {
						uploadService.setRunning(false);
					}
				}
				final long leftUploads = uploadService.countUnprocessed();
				logger.info("Left uploads: {}", leftUploads);
				enqueueUpload();

				if ((!uploadService.getRunning() || 0 == leftUploads) && 0 == runningUploads) {
					uploadService.setRunning(false);
					logger.info("All uploads finished");
					eventBus.post(new UploadFinishedEvent());
				}
			}
		}

		private Upload getUpload() {
			try {
				final Future<Upload> uploadJobFuture = jobCompletionService.take();
				final Upload upload = uploadJobFuture.get();
				logger.info("Upload finished: {}; {}", upload.getMetadata().getTitle(), upload.getVideoid());
				eventBus.post(new UploadJobFinishedEvent(upload));
				runningUploads--;
				return upload;
			} catch (ExecutionException | CancellationException | InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			}
		}
	}

	public void runStarttimeChecker() {
		final Thread th = new Thread(new Runnable() {
			@Override
			public void run() {
				scheduleTask();
				if (0 < uploadService.countReadyStarttime()) {
					run();
				}
			}
		}, "Starttime-Thread");
		th.setDaemon(true);
		th.start();
	}

	private void scheduleTask() {
		final long delay = uploadService.getStarttimeDelay();
		final TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				if (0 < uploadService.countReadyStarttime()) {
					Uploader.this.run();
				}
				scheduleTask();
			}
		};
		if (0 < delay) {
			task = timer.schedule(timerTask, delay, TimeUnit.MILLISECONDS);
		}
	}

	@Subscribe
	public void onUploadEvent(final UploadEvent uploadEvent) {
		if (null != task && !task.isCancelled()) {
			task.cancel(false);
		}
		runStarttimeChecker();
	}

	public void stopStarttimeChecker() {
		timer.shutdownNow();
		executorService.shutdownNow();
	}
}
