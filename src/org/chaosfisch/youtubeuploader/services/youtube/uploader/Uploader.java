/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.services.youtube.uploader;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.concurrent.Task;

import javax.sql.DataSource;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.util.Computer;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Queue;
import org.chaosfisch.youtubeuploader.models.Setting;
import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class Uploader
{
	private static final long		QUEUE_SLEEPTIME			= 30000;

	public static final String		ABORT					= "uploadAbort";
	public static final String		LIMIT					= "uploadLimit";
	public static final String		PROGRESS				= "uploadProgress";
	public static final String		STARTED					= "uploadStarted";
	public static final String		STOPPED					= "uploadStopped";

	private short					actionOnFinish;
	private volatile boolean		inProgress;
	private short					maxUploads				= 1;
	private volatile short			runningUploads;
	private int						speedLimit				= 1000 * 1024;
	private static final int		DEFAULT_CHUNKSIZE		= 10 * 1048576;
	private boolean					startTimeCheckerFlag	= true;

	private final ExecutorService	executorService			= Executors.newFixedThreadPool(10);
	private final Logger			logger					= LoggerFactory.getLogger(getClass());
	@Inject private Injector		injector;

	public Uploader()
	{
		AnnotationProcessor.process(this);
	}

	public void abort(final Queue queue)
	{
		EventBus.publish(Uploader.ABORT, queue);
	}

	public void exit()
	{
		executorService.shutdownNow();
	}

	private boolean hasFreeUploadSpace()
	{
		return runningUploads < maxUploads;
	}

	public boolean isRunning()
	{
		return inProgress && (runningUploads != 0);
	}

	@EventTopicSubscriber(topic = Uploader.PROGRESS)
	public void onUploadJobDoneAndFailed(final String topic, final UploadProgress uploadProgress)
	{
		if ((uploadProgress.done == true) || (uploadProgress.failed == true))
		{
			logger.info(uploadProgress.status);
			uploadFinished(uploadProgress.getQueue());
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
			EventBus.publish(Uploader.LIMIT, speedLimit);
		}
	}

	public void start()
	{
		inProgress = true;
		final Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception
			{
				Base.open(injector.getInstance(DataSource.class));
				while (inProgress && !isCancelled())
				{

					if (hasFreeUploadSpace())
					{
						final Queue polled = Queue.findFirst("(archived = false OR archived IS NULL) AND (inprogress = false OR inprogress IS NULL) AND (failed = false OR failed IS NULL) AND (locked = false OR locked IS NULL) AND (started > NOW() OR started IS NULL) ORDER BY started DESC, sequence ASC, failed ASC");
						if (polled != null)
						{
							if (polled.parent(Account.class) == null)
							{
								polled.setBoolean("locked", true);
								polled.saveIt();
								continue;
							} else
							{
								polled.setBoolean("inprogress", true);
								polled.saveIt();
							}
							final UploadWorker uploadWorker = injector.getInstance(UploadWorker.class);
							setSpeedLimit(speedLimit);
							final Setting setting = Setting.findById("coreplugin.general.chunk_size");
							final Integer chunksize = (setting == null) || (setting.getInteger("value") == null) ? DEFAULT_CHUNKSIZE
									: setting.getInteger("value") * 1048576;
							uploadWorker.run(polled);
							executorService.submit(uploadWorker);
							runningUploads++;
						}

					}
					try
					{
						Thread.sleep(Uploader.QUEUE_SLEEPTIME);
					} catch (final InterruptedException e)
					{}
				}
				Base.close();

				return null;
			}

			@Override
			protected void done()
			{
				// TODO Auto-generated method stub
				super.done();
				try
				{
					get();
				} catch (final Throwable t)
				{
					logger.debug("ERROR", t);
				}
			}
		};
		final Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
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

		Base.open(injector.getInstance(DataSource.class));

		runningUploads--;

		final long leftUploads = Queue.count("archived = false AND failed = false");
		logger.info("Upload finished: {}; {}", queue.getString("title"), queue.getString("videoid"));
		logger.info("Running uploads: {}", runningUploads);
		logger.info("Left uploads: {}", leftUploads);

		if (queue.getBoolean("PAUSEONFINISH") == true)
		{
			inProgress = false;
		}

		if ((inProgress == false) || ((leftUploads == 0) && (runningUploads <= 0)))
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
							logger.info("CLOSING APPLICATION");
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
					Platform.exit();
				}

			}, 60000);
		}
		Base.close();
	}

	public void runStarttimeChecker()
	{
		final Task<Void> task = new Task<Void>() {

			@Override
			protected Void call() throws Exception
			{
				while (!isCancelled() && startTimeCheckerFlag)
				{

					if ((Queue.count("archived = false AND started > NOW() AND inprogress = false") != 0) && !inProgress)
					{
						start();
					}

					try
					{
						Thread.sleep(60000);
					} catch (final InterruptedException e)
					{}
				}
				return null;
			}

		};
		final Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
	}
}
