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
import de.chaosfisch.youtube.upload.job.UploadJob;
import de.chaosfisch.youtube.upload.metadata.IMetadataService;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Uploader {

	private static final int    ENQUEUE_WAIT_TIME   = 10000;
	private static final int    ONE_KILOBYTE        = 1024;
	private static final Logger logger              = LoggerFactory.getLogger(Uploader.class);
	private static final int    DEFAULT_MAX_UPLOADS = 1;

	private final SimpleIntegerProperty maxUploads  = new SimpleIntegerProperty(DEFAULT_MAX_UPLOADS);
	private final SimpleIntegerProperty maxSpeed    = new SimpleIntegerProperty(0);
	private final SimpleBooleanProperty stopOnError = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty running     = new SimpleBooleanProperty(false);

	private final AtomicInteger                             runningUploads       = new AtomicInteger(0);
	private final ExecutorService                           executorService      = Executors.newFixedThreadPool(10);
	private final CompletionService<UploadModel>            jobCompletionService = new ExecutorCompletionService<>(executorService);
	private final ScheduledExecutorService                  timer                = Executors.newSingleThreadScheduledExecutor();
	private final RateLimiter                               rateLimitter         = RateLimiter.create(Double.MAX_VALUE);
	private final HashMap<UploadModel, Future<UploadModel>> futures              = Maps.newHashMapWithExpectedSize(10);
	private final IUploadService   uploadService;
	private final IMetadataService metadataService;

	private UploadFinishProcessor consumer;
	private ScheduledFuture<?>    task;

	@Inject
	public Uploader(final IUploadService uploadService, final IMetadataService metadataService) {
		this.metadataService = metadataService;
		this.uploadService = uploadService;

		maxUploads.addListener(o -> {
			if (canAddJob()) {
				startNextUpload();
			}
		});

		maxSpeed.addListener(o -> rateLimitter.setRate(0 == maxSpeed.get() ? Double.MAX_VALUE : maxSpeed.get() * ONE_KILOBYTE));
	}

	private void startNextUpload() {
		if (canAddJob()) {
			final UploadModel uploadModel = uploadService.fetchNextUpload();
			if (null != uploadModel) {
				createConsumer();
				markUploadRunning(uploadModel);
				final UploadJob uploadJob = new UploadJob.Builder(uploadModel, uploadService, metadataService).withRateLimiter(rateLimitter).build();
				futures.put(uploadModel, jobCompletionService.submit(uploadJob));
				runningUploads.incrementAndGet();
			}
		}
	}

	private void markUploadRunning(final UploadModel uploadModel) {
		uploadModel.setStatus(Status.RUNNING);
		uploadService.store(uploadModel);
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
		return running.get() && maxUploads.get() > runningUploads.get();
	}

	public void run() {
		if (running.get()) {
			return;
		}
		running.set(true);

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
		running.set(false);

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
		if (-1 != delay && (0 == runningUploads.get() || canAddJob())) {
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

	public int getMaxUploads() {
		return maxUploads.get();
	}

	public void setMaxUploads(final int maxUploads) {
		this.maxUploads.set(maxUploads);
	}

	public SimpleIntegerProperty maxUploadsProperty() {
		return maxUploads;
	}

	public int getMaxSpeed() {
		return maxSpeed.get();
	}

	public void setMaxSpeed(final int maxSpeed) {
		this.maxSpeed.set(maxSpeed);
	}

	public SimpleIntegerProperty maxSpeedProperty() {
		return maxSpeed;
	}

	public boolean getStopOnError() {
		return stopOnError.get();
	}

	public void setStopOnError(final boolean stopOnError) {
		this.stopOnError.set(stopOnError);
	}

	public SimpleBooleanProperty stopOnErrorProperty() {
		return stopOnError;
	}

	public boolean getRunning() {
		return running.get();
	}

	public ReadOnlyBooleanProperty runningProperty() {
		return ReadOnlyBooleanWrapper.readOnlyBooleanProperty(running);
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

				if ((!running.get() || 0 == leftUploads) && 0 == runningUploads.get()) {
					running.set(false);
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
				runningUploads.decrementAndGet();
			}
		}

		private void updateQueueStatus(final UploadModel upload) {
			if (Status.FAILED == upload.getStatus() && stopOnError.get()) {
				uploadService.stopUploading();
			}
		}

		private void removeUpload(final UploadModel upload) {
			futures.remove(upload);
			if (null != upload) {
				logger.info("Running uploads: {}", runningUploads);

				if (upload.getStopAfter()) {
					running.set(false);
				}
			}
		}
	}
}
