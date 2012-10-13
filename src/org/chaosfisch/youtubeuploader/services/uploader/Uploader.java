/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
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

import com.google.inject.Inject;
import com.google.inject.Injector;

public class Uploader
{
	public static final String		ALLOWED					= "allowed";
	public static final String		DENIED					= "denied";
	public static final String		MODERATED				= "moderated";

	private static final long		QUEUE_SLEEPTIME			= 30000;
	public static final String		QUEUE_START				= "queueStart";
	public static final String		UPLOAD_ABORT			= "uploadAbort";
	public static final String		UPLOAD_FAILED			= "uploadFailed";
	public static final String		UPLOAD_FINISHED			= "uploadFinished";
	public static final String		UPLOAD_JOB_FINISHED		= "uploadJobFinished";
	public static final String		UPLOAD_LIMIT			= "uploadLimit";
	public static final String		UPLOAD_LOG				= "uploadLog";
	public static final String		UPLOAD_PROGRESS			= "uploadProgress";

	public static final String		UPLOAD_STARTED			= "uploadStarted";
	private short					actionOnFinish;
	private final ExecutorService	executorService;
	@Inject private Injector		injector;
	private boolean					inProgress;

	@InjectLogger private Logger	logger;
	private short					maxUploads				= 1;
	@Inject private QueueDao		queueService;
	private short					runningUploads;
	@Inject private SettingsService	settingsService;
	private int						speedLimit				= 1000 * 1024;

	private boolean					startTimeCheckerFlag	= true;

	public Uploader()
	{
		executorService = Executors.newFixedThreadPool(10);
		AnnotationProcessor.process(this);
	}

	public void abort(final Queue queue)
	{
		EventBus.publish(Uploader.UPLOAD_ABORT, queue);
	}

	public void exit()
	{
		executorService.shutdownNow();
	}

	private boolean hasFreeUploadSpace()
	{
		synchronized (this)
		{
			return runningUploads < maxUploads;
		}
	}

	public boolean isRunning()
	{
		synchronized (this)
		{
			return inProgress && (runningUploads != 0);
		}
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_FAILED)
	public void onUploadJobFailed(final String topic, final UploadFailed uploadFailed)
	{
		logger.info("Upload failed");
		uploadFinished(uploadFailed.getQueue());
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_JOB_FINISHED)
	public void onUploadJobFinished(final String topic, final Queue queue)
	{
		logger.info("Upload successful");
		uploadFinished(queue);
	}

	public void runStarttimeChecker()
	{
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
				} catch (final InterruptedException e)
				{
					throw new RuntimeException("This shouldn't happen", e);
				}
			}
		}
	}

	public void setActionOnFinish(final short actionOnFinish)
	{
		this.actionOnFinish = actionOnFinish;
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

	public void setSpeedLimit(final int bytes)
	{
		speedLimit = bytes * 1024;
		if (runningUploads > 0)
		{
			speedLimit = Math.round((bytes * 1024) / runningUploads);
			EventBus.publish(Uploader.UPLOAD_LIMIT, speedLimit);
		}
	}

	public void start()
	{
		inProgress = true;

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
				} catch (final InterruptedException e)
				{
					throw new RuntimeException("This shouldn't happen", e);
				}
			}
		}
	}

	public void stop()
	{
		inProgress = false;
	}

	public void stopStarttimeChecker()
	{
		startTimeCheckerFlag = false;
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
}
