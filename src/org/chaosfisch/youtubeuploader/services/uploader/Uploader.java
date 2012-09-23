/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Dennis Fischer
 ******************************************************************************/

package org.chaosfisch.youtubeuploader.services.uploader;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.util.Computer;
import org.chaosfisch.util.logger.InjectLogger;
import org.chaosfisch.youtubeuploader.dao.spi.QueueDao;
import org.chaosfisch.youtubeuploader.models.Queue;
import org.chaosfisch.youtubeuploader.services.SettingsService;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class Uploader
{
	public static final String		ALLOWED					= "allowed";
	public static final String		DENIED					= "denied";
	public static final String		MODERATED				= "moderated";

	public static final String		UPLOAD_JOB_FINISHED		= "uploadJobFinished";
	public static final String		UPLOAD_ABORT			= "uploadAbort";
	public static final String		UPLOAD_FAILED			= "uploadFailed";
	public static final String		UPLOAD_LOG				= "uploadLog";
	public static final String		UPLOAD_PROGRESS			= "uploadProgress";
	public static final String		UPLOAD_STARTED			= "uploadStarted";
	public static final String		UPLOAD_FINISHED			= "uploadFinished";
	public static final String		UPLOAD_LIMIT			= "uploadLimit";
	public static final String		QUEUE_START				= "queueStart";

	private final ExecutorService	executorService;
	@Inject
	private QueueDao				queueService;
	@Inject
	private SettingsService			settingsService;
	@Inject
	private Injector				injector;
	@InjectLogger
	private Logger					logger;

	private boolean					inProgress;
	private short					runningUploads;
	private short					actionOnFinish;
	private short					maxUploads				= 1;
	private int						speedLimit				= 1000 * 1024;
	private boolean					startTimeCheckerFlag	= true;

	private static final long		QUEUE_SLEEPTIME			= 30000;

	public Uploader()
	{
		executorService = Executors.newFixedThreadPool(10);
		AnnotationProcessor.process(this);
	}

	public void start()
	{
		inProgress = true;

		new BetterSwingWorker() {
			@Override
			protected void background()
			{
				while (inProgress)
				{
					if (hasFreeUploadSpace())
					{
						final Queue polled = queueService.poll();
						if (polled != null)
						{
							final UploadWorker uploadWorker = injector.getInstance(UploadWorker.class);
							setSpeedLimit(speedLimit);
							uploadWorker.run(polled, speedLimit,
									1048576 * Integer.parseInt((String) settingsService.get("coreplugin.general.CHUNK_SIZE", "10")));
							executorService.submit(uploadWorker);
							synchronized (this)
							{
								runningUploads++;
							}
						}
					}

					try
					{
						Thread.sleep(Uploader.QUEUE_SLEEPTIME);
					} catch (InterruptedException e)
					{
						throw new RuntimeException("This shouldn't happen", e);
					}
				}
			}
		}.execute();
	}

	public void stop()
	{
		inProgress = false;
	}

	public void abort(final Queue queue)
	{
		EventBus.publish(Uploader.UPLOAD_ABORT, queue);
	}

	public boolean isRunning()
	{
		synchronized (this)
		{
			return inProgress && (runningUploads != 0);
		}
	}

	private boolean hasFreeUploadSpace()
	{
		synchronized (this)
		{
			return runningUploads < maxUploads;
		}
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_JOB_FINISHED)
	public void onUploadJobFinished(final String topic, final Queue queue)
	{
		logger.info("Upload successful");
		uploadFinished(queue);
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_FAILED)
	public void onUploadJobFailed(final String topic, final UploadFailed uploadFailed)
	{
		logger.info("Upload failed");
		uploadFinished(uploadFailed.getQueue());
	}

	private void uploadFinished(final Queue queue)
	{
		synchronized (this)
		{
			logger.info(String.format("Upload finished: %s; %s", queue.title, queue.videoId));
			runningUploads--;
			logger.info(String.format("Running uploads: %s", runningUploads));
			queueService.update(queue);
			if (queueService.getValidQueued().isEmpty() && (runningUploads <= 0))
			{
				logger.info("All uploads finished");
				final Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run()
					{
						switch (actionOnFinish)
						{
							case 0:
								return;
							case 1:
								System.out.println("CLOSING APPLICATION");
								System.exit(0);
								return;
							case 2:
								System.out.println("SHUTDOWN COMPUTER");
								Computer.shutdownComputer();
								return;
							case 3:
								System.out.println("HIBERNATE COMPUTER");
								Computer.hibernateComputer();
								return;
						}
					}
				}, 60000);
			}

			logger.info(String.format("Left uploads: %d", queueService.getValidQueued().size()));
		}
	}

	public void setActionOnFinish(final short actionOnFinish)
	{
		this.actionOnFinish = actionOnFinish;
	}

	public void setSpeedLimit(final int bytes)
	{
		speedLimit = bytes * 1024;
		if (runningUploads > 0)
		{
			speedLimit = Math.round((bytes * 1024) / runningUploads);
			EventBus.publish(Uploader.UPLOAD_LIMIT, speedLimit);
		}
	}

	public void setMaxUploads(final short maxUploads)
	{
		if (maxUploads > 10)
		{
			this.maxUploads = 10;
		} else
		{
			this.maxUploads = maxUploads;
		}
	}

	public void exit()
	{
		executorService.shutdownNow();
	}

	public void runStarttimeChecker()
	{
		final BetterSwingWorker startTimeChecker = new BetterSwingWorker() {
			@Override
			protected void background()
			{
				while (!Thread.currentThread().isInterrupted() && startTimeCheckerFlag)
				{

					if (queueService.hasStarttime() && !inProgress)
					{
						start();
					}

					try
					{
						Thread.sleep(60000);
					} catch (InterruptedException e)
					{
						throw new RuntimeException("This shouldn't happen", e);
					}
				}
			}

		};
		startTimeChecker.execute();
	}

	public void stopStarttimeChecker()
	{
		startTimeCheckerFlag = false;
	}
}
