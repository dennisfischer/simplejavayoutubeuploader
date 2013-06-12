/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.google.youtube.upload;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Injector;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.chaosfisch.google.youtube.upload.events.UploadAbortEvent;
import org.chaosfisch.google.youtube.upload.events.UploadProgressEvent;
import org.chaosfisch.util.Computer;
import org.chaosfisch.util.EventBusUtil;
import org.chaosfisch.youtubeuploader.db.dao.AccountDao;
import org.chaosfisch.youtubeuploader.db.dao.UploadDao;
import org.chaosfisch.youtubeuploader.db.data.ActionOnFinish;
import org.chaosfisch.youtubeuploader.db.events.ModelAddedEvent;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;
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
	private final Logger          logger          = LoggerFactory.getLogger(getClass());
	@Inject
	private Injector   injector;
	@Inject
	private EventBus   eventBus;
	@Inject
	private UploadDao  uploadDao;
	@Inject
	private AccountDao accountDao;

	public Uploader() {
		EventBusUtil.getInstance().register(this);
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
			final Upload polled = uploadDao.fetchNextUpload();
			if (null != polled) {
				if (null == accountDao.findById(polled.getAccountId())) {
					polled.setLocked(true);
					uploadDao.update(polled);
				} else {
					polled.setInprogress(true);
					uploadDao.update(polled);
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

		final long leftUploads = uploadDao.countLeftUploads();
		logger.info("Upload finished: {}; {}", queue.getTitle(), queue.getVideoid());
		logger.info("Running uploads: {}", runningUploads);
		logger.info("Left uploads: {}", leftUploads);

		if (queue.getPauseonfinish()) {
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
					Computer.shutdownComputer();
					break;
				case SLEEP:
					logger.info("HIBERNATE COMPUTER");
					Computer.hibernateComputer();
					break;
				case CUSTOM:
					logger.info("Custom command: {}", actionOnFinish.get().getCommand());
					Computer.customCommand(actionOnFinish.get().getCommand());
					break;
			}
		}
	}

	public void runStarttimeChecker() {
		executorService.submit(new Callable<Boolean>() {

			@Override
			public Boolean call() {
				while (!Thread.interrupted()) {
					if (0 < uploadDao.countAvailableStartingUploads()) {
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
