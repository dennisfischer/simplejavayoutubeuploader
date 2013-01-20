/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.services.youtube.uploader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javax.sql.DataSource;

import org.chaosfisch.util.Computer;
import org.chaosfisch.util.EventBusUtil;
import org.chaosfisch.util.ThreadUtil;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Upload;
import org.chaosfisch.youtubeuploader.models.events.ModelPostSavedEvent;
import org.chaosfisch.youtubeuploader.services.youtube.uploader.events.UploadAbortEvent;
import org.chaosfisch.youtubeuploader.services.youtube.uploader.events.UploadProgressEvent;
import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class Uploader {
	public static final String		ABORT					= "uploadAbort";
	public static final String		PROGRESS				= "uploadProgress";

	public SimpleIntegerProperty	actionOnFinish			= new SimpleIntegerProperty(0);
	public SimpleBooleanProperty	inProgressProperty		= new SimpleBooleanProperty(false);
	public SimpleIntegerProperty	maxUploads				= new SimpleIntegerProperty(1);

	private volatile short			runningUploads			= 0;
	private boolean					startTimeCheckerFlag	= true;

	private final ExecutorService	executorService			= Executors.newFixedThreadPool(5);
	private final Logger			logger					= LoggerFactory.getLogger(getClass());
	@Inject private Injector		injector;
	@Inject private DataSource		datasource;
	@Inject private EventBus		eventBus;

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

	public boolean isRunning() {
		return inProgressProperty.get() && runningUploads != 0;
	}

	public void exit() {
		executorService.shutdownNow();
	}

	public void start() {
		inProgressProperty.set(true);
		ThreadUtil.doInBackground(new Runnable() {

			@Override
			public void run() {
				for (int i = 0; i < maxUploads.get() && hasFreeUploadSpace(); i++) {
					sendUpload();
					try {
						Thread.sleep(10000);
					} catch (final InterruptedException e) {}
				}

			}
		});
	}

	private synchronized void sendUpload() {
		if (!Base.hasConnection()) {
			Base.open(injector.getInstance(DataSource.class));
		}

		if (inProgressProperty.get() && hasFreeUploadSpace()) {
			final Upload polled = Upload
					.findFirst("(archived = false OR archived IS NULL) AND (inprogress = false OR inprogress IS NULL) AND (failed = false OR failed IS NULL) AND (locked = false OR locked IS NULL) AND (started < NOW() OR started IS NULL) ORDER BY started DESC, failed ASC");
			if (polled != null) {
				if (polled.parent(Account.class) == null) {
					polled.setBoolean("locked", true);
					polled.saveIt();
				} else {
					polled.setBoolean("inprogress", true);
					polled.saveIt();
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
		startTimeCheckerFlag = false;
	}

	private void uploadFinished(final Upload queue) {
		if (!Base.hasConnection()) {
			Base.open(injector.getInstance(DataSource.class));
		}
		runningUploads--;

		final long leftUploads = Upload.count("archived = false AND failed = false");
		logger.info("Upload finished: {}; {}", queue.getString("title"), queue.getString("videoid"));
		logger.info("Running uploads: {}", runningUploads);
		logger.info("Left uploads: {}", leftUploads);

		if (queue.getBoolean("PAUSEONFINISH") == true) {
			inProgressProperty.set(false);
		} else {
			sendUpload();
		}

		if (!inProgressProperty.get() || leftUploads == 0 && runningUploads <= 0) {
			inProgressProperty.set(false);
			logger.info("All uploads finished");
			switch (actionOnFinish.get()) {
				case 0:
					return;
				case 1:
					logger.info("CLOSING APPLICATION");
					Platform.exit();
				break;
				case 2:
					logger.info("SHUTDOWN COMPUTER");
					Computer.shutdownComputer();
				break;
				case 3:
					logger.info("HIBERNATE COMPUTER");
					Computer.hibernateComputer();
				break;
			}
		}
	}

	public void runStarttimeChecker() {
		ThreadUtil.doInBackground(new Runnable() {

			@Override
			public void run() {
				if (!Base.hasConnection()) {
					Base.open(datasource);
				}
				while (!Thread.interrupted() && startTimeCheckerFlag) {

					if (Upload.count("archived = false AND started < NOW() AND inprogress = false") != 0) {
						start();
					}

					try {
						Thread.sleep(60000);
					} catch (final InterruptedException e) {}
				}
			}
		});
	}

	@Subscribe
	public void onUploadJobDoneAndFailed(final UploadProgressEvent uploadProgressEvent) {
		if (uploadProgressEvent.done == true || uploadProgressEvent.failed == true) {
			logger.info("Status: {}", uploadProgressEvent.status);
			uploadFinished(uploadProgressEvent.getQueue());
		}
	}

	@Subscribe
	public void onUploadSaved(final ModelPostSavedEvent modelPostSavedEvent) {
		if (modelPostSavedEvent.getModel() instanceof Upload && !modelPostSavedEvent.getModel().getBoolean("inprogress")) {
			sendUpload();
		}
	}
}
