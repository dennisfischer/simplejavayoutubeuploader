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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import de.chaosfisch.google.youtube.upload.events.UploadAbortEvent;
import de.chaosfisch.google.youtube.upload.events.UploadProgressEvent;
import de.chaosfisch.util.ComputerUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.chaosfisch.youtubeuploader.db.data.ActionOnFinish;
import org.chaosfisch.youtubeuploader.db.events.ModelAddedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Uploader {
	public final SimpleObjectProperty<ActionOnFinish> actionOnFinish     = new SimpleObjectProperty<>();
	public final SimpleBooleanProperty                inProgressProperty = new SimpleBooleanProperty(false);
	public final SimpleIntegerProperty                maxUploads         = new SimpleIntegerProperty(1);

	private volatile short runningUploads;

	private final ExecutorService executorService = Executors.newFixedThreadPool(7);

	private static final Logger logger = LoggerFactory.getLogger(Upload.class);

	private       ComputerUtil   computerUtil;
	private final EventBus       eventBus;
	private final IUploadService uploadService;

	@Inject
	public Uploader(final EventBus eventBus, final IUploadService uploadService) {

		this.eventBus = eventBus;
		this.uploadService = uploadService;
		this.eventBus.register(this);
		maxUploads.addListener(new ChangeListener<Number>() {

			@Override
			public void changed(final ObservableValue<? extends Number> observable, final Number oldValue, final Number newValue) {
				sendUpload();
			}
		});
	}

	public void abort(final Upload upload) {
		eventBus.post(new UploadAbortEvent(upload));
	}

	private boolean hasFreeUploadSpace() {
		return runningUploads < maxUploads.get();
	}

	public void exit() {
		executorService.shutdownNow();
	}

	public void start() {
		inProgressProperty.set(true);
		executorService.submit(new Callable<Boolean>() {

			@Override
			public Boolean call() {
				for (int i = 0; i < maxUploads.get() && hasFreeUploadSpace(); i++) {
					sendUpload();
					try {
						Thread.sleep(10000);
					} catch (final InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				return true;
			}
		});
	}

	private synchronized void sendUpload() {
		if (!executorService.isShutdown() && inProgressProperty.get() && hasFreeUploadSpace()) {
			final Upload polled = uploadService.findNextUpload();
			if (null != polled) {
				if (null == polled.getAccount()) {
					polled.getStatus().setLocked(true);
					uploadService.update(polled);
				} else {
					polled.getStatus().setRunning(true);
					uploadService.update(polled);
					final UploadWorker uploadWorker = injector.getInstance(UploadWorker.class);
					uploadWorker.run(polled);
					executorService.submit(uploadWorker);
					runningUploads++;
				}
			}
		}
	}

	public void stop() {
		inProgressProperty.set(false);
	}

	public void stopStarttimeChecker() {
		Thread.currentThread().interrupt();
	}

	private void uploadFinished(final Upload queue) {
		runningUploads--;

		final long leftUploads = uploadService.countUnprocessed();
		logger.info("Upload finished: {}; {}", queue.getMetadata().getTitle(), queue.getVideoid());
		logger.info("Running uploads: {}", runningUploads);
		logger.info("Left uploads: {}", leftUploads);

		if (queue.getPauseOnFinish()) {
			inProgressProperty.set(false);
		} else {
			sendUpload();
		}

		if (!inProgressProperty.get() || 0 == leftUploads && 0 >= runningUploads) {
			inProgressProperty.set(false);
			logger.info("All uploads finished");
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

	@Subscribe
	public void onUploadJobDoneAndFailed(final UploadProgressEvent uploadProgressEvent) {
		if (uploadProgressEvent.done || uploadProgressEvent.failed) {
			uploadFinished(uploadProgressEvent.getUpload());
		}
	}

	@Subscribe
	public void onUploadSaved(final ModelAddedEvent modelPostSavedEvent) {
		if (modelPostSavedEvent.getModel() instanceof Upload && !((Upload) modelPostSavedEvent.getModel()).getInprogress()) {
			sendUpload();
		}
	}
}
