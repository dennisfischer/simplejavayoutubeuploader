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
import com.google.inject.Inject;
import de.chaosfisch.google.youtube.upload.events.UploadFinishedEvent;
import de.chaosfisch.google.youtube.upload.events.UploadJobAbortEvent;
import de.chaosfisch.google.youtube.upload.events.UploadJobFinishedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

public class Uploader {

	private static final int ENQUEUE_WAIT_TIME = 10000;
	private boolean running;
	private int     runningUploads;
	private int     maxUploads;
	private final        CompletionService<Upload> jobCompletionService = new ExecutorCompletionService<>(Executors.newFixedThreadPool(10));
	private final        List<Future<Upload>>      futures              = Lists.newArrayListWithExpectedSize(10);
	private static final Logger                    logger               = LoggerFactory.getLogger(Uploader.class);

	private final Thread consumer = new UploadFinishProcessor();

	private final EventBus          eventBus;
	private       IUploadService    uploadService;
	private final IUploadJobFactory uploadJobFactory;

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
		return running && maxUploads > runningUploads;
	}

	public void shutdown(final boolean force) {
		running = false;

		if (force) {
			for (final Future<Upload> job : futures) {
				job.cancel(true);
				futures.remove(job);
			}
		}
	}

	public void run() {
		if (running) {
			return;
		}
		running = true;

		while (canAddJob()) {
			enqueueUpload();
			try {
				Thread.sleep(ENQUEUE_WAIT_TIME);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	public void shutdown() {
		shutdown(false);
	}

	public void abort(final Upload upload) {
		eventBus.post(new UploadJobAbortEvent(upload));
	}

	private void enqueueUpload() {
		if (canAddJob()) {
			final Upload polled = uploadService.findNextUpload();
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

					if (upload.getPauseOnFinish()) {
						running = false;
					}
				}
				final long leftUploads = uploadService.countUnprocessed();
				logger.info("Left uploads: {}", leftUploads);
				enqueueUpload();

				if ((!running || 0 == leftUploads) && 0 == runningUploads) {
					running = false;
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
	/*
	private void uploadFinished(final Upload queue) {
			switch (actionOnFinish.get()) {
				default:
				case NOTHING:
					return;
				case CLOSE:
					logger.info("CLOSING APPLICATION");
					Platform.exit();
					break;
				case SHUTDOWN:
					logger.info("SHUTDOWN COMPUTER");
					computerUtil.shutdownComputer();
					break;
				case SLEEP:
					logger.info("HIBERNATE COMPUTER");
					computerUtil.hibernateComputer();
					break;
				case CUSTOM:
					logger.info("Custom command: {}", actionOnFinish.get().getCommand());
					computerUtil.customCommand(actionOnFinish.get().getCommand());
					break;
			}
		}
	}

	public void runStarttimeChecker() {
		executorService.submit(new Callable<Boolean>() {

			@Override
			public Boolean call() {
				while (!Thread.interrupted()) {
					if (0 < uploadService.countReadyStarttime()) {
						start();
					}
					try {
						Thread.sleep(60000);
					} catch (final InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				return true;
			}
		});
	}
	*/
}
