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
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.util.BetterSwingWorker;
import org.chaosfisch.util.Computer;
import org.chaosfisch.youtubeuploader.db.QueueEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.worker.UploadWorker;
import org.chaosfisch.youtubeuploader.services.PlaylistService;
import org.chaosfisch.youtubeuploader.services.QueueService;

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
	public static final String ALLOWED             = "allowed";
	public static final String DENIED              = "denied";
	public static final String MODERATED           = "moderated";
	public static final String STATE               = "state";
	public static final String CONTENT_TYPE        = "Content-Type";
	public static final String LOCATION            = "Location";
	public static final String CONTENT_RANGE       = "Content-Range";
	public static final String UPLOAD_JOB          = "UPLOAD_JOB";
	public static final String ABORT_UPLOAD        = "ABORT_UPLOAD";
	public static final String START_QUEUE         = "START_QUEUE";
	public static final String STOP_QUEUE          = "STOP_QUEUE";
	public static final String UPLOAD_JOB_FINISHED = "UPLOAD_JOB_FINISHED";
	public static final String UPLOAD_FAILED       = "uploadFailed";
	public static final String UPLOAD_LOG          = "uploadLog";
	public static final String UPLOAD_PROGRESS     = "uploadProgress";
	public static final String UPLOAD_STARTED      = "uploadStarted";
	public static final String UPLOAD_FINISHED     = "uploadFinished";
	public static final String UPLOAD_LIMIT        = "uploadLimit";

	private final ExecutorService executorService;
	private final QueueService    queueService;
	private final PlaylistService playlistService;

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
						final QueueEntry polledEntry = Uploader.this.queueService.poll();
						if (polledEntry != null) {
							Uploader.this.executorService.submit(new UploadWorker(polledEntry, Uploader.this.playlistService, Uploader.this.speedLimit));
							Uploader.this.setSpeedLimit(Uploader.this.speedLimit);
							Uploader.this.runningUploads++;
						}
					}

					try {
						Thread.sleep(QUEUE_SLEEPTIME);
					} catch (InterruptedException e) {
						e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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

	public void abort(final QueueEntry queueEntry)
	{
		EventBus.publish(Uploader.ABORT_UPLOAD, queueEntry);
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
	public void onUploadJobFinished(final String topic, final Object o)
	{
		this.uploadFinished((QueueEntry) o);
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = UPLOAD_FAILED)
	public void onUploadJobFailed(final String topic, final Object o)
	{
		this.uploadFinished((QueueEntry) o);
	}

	private void uploadFinished(final QueueEntry queueEntry)
	{
		this.runningUploads--;
		queueEntry.setArchived(true);
		queueEntry.setInprogress(false);
		this.queueService.updateQueueEntry(queueEntry);
		if (this.queueService.getQueuedQueueEntry().size() == 0 && this.runningUploads == 0) {
			switch (this.actionOnFinish) {
				case 0:
					break;
				case 1:
					System.exit(0);
					break;
				case 2:
					Computer.shutdownComputer();
					break;
				case 3:
					Computer.hibernateComputer();
					break;
			}
		}
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

					if (Uploader.this.queueService.hasStarttimeEntry() && !Uploader.this.inProgress) {
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