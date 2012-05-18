/*
 * Copyright (c) 2012, Dennis Fischer
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader;

import com.google.inject.Inject;
import org.apache.log4j.Logger;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.util.BetterSwingWorker;
import org.chaosfisch.util.Computer;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Queue;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PlaylistService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.QueueService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.worker.UploadFailed;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.worker.UploadWorker;
import org.chaosfisch.youtubeuploader.services.settingsservice.spi.SettingsService;
import org.chaosfisch.youtubeuploader.util.logger.InjectLogger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 06.01.12
 * Time: 22:54
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class Uploader
{
	public static final String ALLOWED   = "allowed";
	public static final String DENIED    = "denied";
	public static final String MODERATED = "moderated";

	public static final String UPLOAD_JOB_FINISHED = "uploadJobFinished";
	public static final String UPLOAD_ABORT        = "uploadAbort";
	public static final String UPLOAD_FAILED       = "uploadFailed";
	public static final String UPLOAD_LOG          = "uploadLog";
	public static final String UPLOAD_PROGRESS     = "uploadProgress";
	public static final String UPLOAD_STARTED      = "uploadStarted";
	public static final String UPLOAD_FINISHED     = "uploadFinished";
	public static final String UPLOAD_LIMIT        = "uploadLimit";
	public static final String QUEUE_START         = "queueStart";

	private final         ExecutorService executorService;
	private final         QueueService    queueService;
	private final         PlaylistService playlistService;
	@Inject private       SettingsService settingsService;
	@InjectLogger private Logger          logger;

	private static final long QUEUE_SLEEPTIME = 60000;

	private boolean inProgress           = false;
	private short   runningUploads       = 0;
	private short   maxUploads           = 1;
	private short   actionOnFinish       = 0;
	private int     speedLimit           = 1000 * 1024;
	private boolean startTimeCheckerFlag = true;

	@Inject
	public Uploader(final QueueService queueService, final PlaylistService playlistService)
	{
		this.queueService = queueService;
		this.playlistService = playlistService;
		this.executorService = Executors.newFixedThreadPool(10);
		AnnotationProcessor.process(this);
	}

	public void start()
	{
		this.inProgress = true;

		new BetterSwingWorker()
		{
			@Override
			protected void background()
			{
				while (Uploader.this.inProgress) {
					if (Uploader.this.hasFreeUploadSpace()) {
						final Queue polled = Uploader.this.queueService.poll();
						if (polled != null) {
							Uploader.this.executorService.submit(new UploadWorker(polled, Uploader.this.playlistService, Uploader.this.speedLimit, 1024 * 1024 * Integer.parseInt(
									(String) Uploader.this.settingsService.get("coreplugin.general.CHUNK_SIZE", "10"))));
							Uploader.this.setSpeedLimit(Uploader.this.speedLimit);
							Uploader.this.runningUploads++;
						}
					}

					try {
						Thread.sleep(QUEUE_SLEEPTIME);
					} catch (InterruptedException ignored) {
					}
				}
			}

			@Override
			protected void onDone()
			{

			}
		}.execute();
	}

	public void stop()
	{
		this.inProgress = false;
	}

	public void abort(final Queue queue)
	{
		EventBus.publish(Uploader.UPLOAD_ABORT, queue);
	}

	public boolean isRunning()
	{
		return this.inProgress && (this.runningUploads != 0);
	}

	private boolean hasFreeUploadSpace()
	{
		return this.runningUploads < this.maxUploads;
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = Uploader.UPLOAD_JOB_FINISHED)
	public void onUploadJobFinished(final String topic, final Queue queue)
	{
		this.uploadFinished(queue);
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = UPLOAD_FAILED)
	public void onUploadJobFailed(final String topic, final UploadFailed uploadFailed)
	{
		this.uploadFinished(uploadFailed.getQueue());
	}

	private void uploadFinished(final Queue queue)
	{
		this.logger.info("Upload finished: " + queue.title + "; " + queue.videoId);
		this.runningUploads--;
		queue.archived = true;
		queue.inprogress = false;
		this.logger.info("Running uploads: " + this.runningUploads);
		this.queueService.updateQueue(queue);
		if (this.queueService.getQueued().size() == 0 && this.runningUploads == 0) {
			this.logger.info("All uploads finished");
			switch (this.actionOnFinish) {
				case 0:
					return;
				case 1:
					System.exit(0);
					return;
				case 2:
					Computer.shutdownComputer();
					return;
				case 3:
					Computer.hibernateComputer();
					return;
			}
		}

		this.logger.info("Left uploads: " + this.queueService.getQueued().size());
	}

	public void setActionOnFinish(final short actionOnFinish)
	{
		this.actionOnFinish = actionOnFinish;
	}

	public void setSpeedLimit(final int bytes)
	{
		this.speedLimit = bytes * 1024;
		if (this.runningUploads > 0) {
			this.speedLimit = Math.round(bytes * 1024 / this.runningUploads);
			EventBus.publish(UPLOAD_LIMIT, this.speedLimit);
		}
	}

	public void setMaxUploads(final short maxUploads)
	{
		if (maxUploads > 10) {
			this.maxUploads = 10;
		} else {
			this.maxUploads = maxUploads;
		}
	}

	public void exit()
	{
		this.executorService.shutdownNow();
	}

	public void runStarttimeChecker()
	{
		final BetterSwingWorker startTimeChecker = new BetterSwingWorker()
		{
			@Override
			protected void background()
			{
				while (!Thread.currentThread().isInterrupted() && Uploader.this.startTimeCheckerFlag) {

					if (Uploader.this.queueService.hasStarttime() && !Uploader.this.inProgress) {
						Uploader.this.start();
					}

					try {
						Thread.sleep(60000);
					} catch (InterruptedException ignored) {
					}
				}
			}

			@Override
			protected void onDone()
			{
				//To change body of implemented methods use File | Settings | File Templates.
			}
		};
		startTimeChecker.execute();
	}

	public void stopStarttimeChecker()
	{
		this.startTimeCheckerFlag = false;
	}
}