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

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import de.chaosfisch.google.youtube.upload.events.UploadEvent;
import de.chaosfisch.google.youtube.upload.events.UploadFinishedEvent;
import de.chaosfisch.google.youtube.upload.events.UploadJobAbortEvent;
import de.chaosfisch.google.youtube.upload.events.UploadJobFinishedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.*;

public class Uploader {

	private static final int    ENQUEUE_WAIT_TIME   = 10000;
	private static final int    DEFAULT_MAX_UPLOADS = 1;
	private static final Logger logger              = LoggerFactory.getLogger(Uploader.class);

	private       int                       maxUploads           = DEFAULT_MAX_UPLOADS;
	private final CompletionService<Upload> jobCompletionService = new ExecutorCompletionService<>(Executors.newFixedThreadPool(10));
	private final List<Future<Upload>>      futures              = Lists.newArrayListWithExpectedSize(10);

	private final Thread consumer = new UploadFinishProcessor();
	private       int               runningUploads;
	private       IUploadService    uploadService;
	private final EventBus          eventBus;
	private final IUploadJobFactory uploadJobFactory;
	private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> task;

	@Inject
	public Uploader(final EventBus eventBus, final IUploadJobFactory uploadJobFactory) {
		this.eventBus = eventBus;
		this.uploadJobFactory = uploadJobFactory;
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

	public void shutdown(final boolean force) {
		uploadService.setRunning(false);

		if (force) {
			for (final Future<Upload> job : futures) {
				job.cancel(true);
				futures.remove(job);
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
		});
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
		eventBus.post(new UploadJobAbortEvent(upload));
	}

	private void enqueueUpload() {
		if (canAddJob()) {
			final Upload polled = uploadService.fetchNextUpload();
			if (null != polled) {
				if (null == polled.getAccount()) {
					polled.getStatus().setLocked(true);
					uploadService.update(polled);
				} else {
					polled.getStatus().setRunning(true);
					uploadService.update(polled);
					jobCompletionService.submit(uploadJobFactory.create(polled));
					runningUploads++;
				}
			}
		}
	}

	public void setUploadService(final IUploadService uploadService) {
		this.uploadService = uploadService;
	}

	private class UploadFinishProcessor extends Thread {
		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				final Upload upload = getUpload();
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
			} catch (ExecutionException | InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			}
		}
	}

	public void runStarttimeChecker() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				scheduleTask();
				if (0 < uploadService.countReadyStarttime()) {
					run();
				}
			}
		}).start();
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
	}

}
