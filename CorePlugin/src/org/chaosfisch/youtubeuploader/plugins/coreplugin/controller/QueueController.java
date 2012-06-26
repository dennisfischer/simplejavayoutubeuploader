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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.controller;

import com.google.inject.Inject;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.plugin.ExtensionPoints.ExitExtensionPoint;
import org.chaosfisch.plugin.ExtensionPoints.ExtensionPoint;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Queue;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.QueueTableModel;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.QueuePosition;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.QueueService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.Uploader;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.view.UploadViewPanel;

import javax.swing.*;
import java.util.ResourceBundle;

public class QueueController
{

	public static final String STATUS_PROPERTY         = "Status"; //NON-NLS
	public static final String TITLE_PROPERTY          = "Title"; //NON-NLS
	public static final String FILE_PROPERTY           = "File"; //NON-NLS
	public static final String STARTTIME_PROPERTY      = "Starttime"; //NON-NLS
	public static final String UPLOADED_BYTES_PROPERTY = "Uploaded Bytes"; //NON-NLS
	public static final String ETA_PROPERTY            = "ETA"; //NON-NLS
	public static final String PROGRESS_PROPERTY       = "Progress"; //NON-NLS

	private final QueueTableModel queueList;
	private final QueueService    queueService;
	private final Uploader        uploader;

	private final ResourceBundle resourceBundle = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.plugins.coreplugin.resources.coreplugin"); //NON-NLS

	@Inject
	public QueueController(final QueueTableModel queueTableModel, final QueueService queueService, final Uploader uploader)
	{
		this.queueList = queueTableModel;
		this.queueService = queueService;
		this.uploader = uploader;
		AnnotationProcessor.process(this);
	}

	public void startQueue()
	{
		this.uploader.start();
	}

	public void stopQueue()
	{
		this.uploader.stop();
	}

	public void abortUpload(final Queue queue)
	{
		this.uploader.abort(queue);
	}

	public void changeQueueFinished(final int item)
	{
		this.uploader.setActionOnFinish((short) item);
	}

	public void moveTop(final Queue queue)
	{
		this.queueService.sort(queue, QueuePosition.QUEUE_TOP);
		this.queueList.sortQueueEntry(queue, QueuePosition.QUEUE_TOP);
	}

	public void moveUp(final Queue queue)
	{
		this.queueService.sort(queue, QueuePosition.QUEUE_UP);
		this.queueList.sortQueueEntry(queue, QueuePosition.QUEUE_UP);
	}

	public void moveDown(final Queue queue)
	{
		this.queueService.sort(queue, QueuePosition.QUEUE_DOWN);
		this.queueList.sortQueueEntry(queue, QueuePosition.QUEUE_DOWN);
	}

	public void moveBottom(final Queue queue)
	{
		this.queueService.sort(queue, QueuePosition.QUEUE_BOTTOM);
		this.queueList.sortQueueEntry(queue, QueuePosition.QUEUE_BOTTOM);
	}

	public void changeQueueView(final short item)
	{
		if (this.queueList.getRowCount() > 0) {
			System.out.println(this.queueList.getRowCount());
			this.queueList.removeRowRange(0, this.queueList.getRowCount() - 1);
		}
		switch (item) {
			case 0:
				for (final Queue queue : this.queueService.getAll()) {
					this.queueList.addRow(queue);
				}
				break;
			case 1:
				for (final Queue queue : this.queueService.getArchived()) {
					this.queueList.addRow(queue);
				}
				break;
			case 2:
				for (final Queue queue : this.queueService.getQueued()) {
					this.queueList.addRow(queue);
				}
				break;
		}
	}

	public void deleteEntry(final Queue queue)
	{
		this.uploader.abort(queue);
		this.getQueueList().removeElement(queue);
		this.queueService.delete(queue);
	}

	public void editEntry(final Queue queue)
	{
		EventBus.publish(UploadViewPanel.EDIT_QUEUE_ENTRY, queue);
	}

	public QueueTableModel getQueueList()
	{
		return this.queueList;
	}

	public QueueService getQueueService()
	{
		return this.queueService;
	}

	public void changeSpeedLimit(final int bytes)
	{
		this.uploader.setSpeedLimit(bytes);
	}

	public void changeMaxUpload(final short maxUploads)
	{
		this.uploader.setMaxUploads(maxUploads);
	}

	public ExtensionPoint uploadExitPoint()
	{
		return new ExitExtensionPoint()
		{
			@Override
			public boolean canExit()
			{
				if (QueueController.this.uploader.isRunning()) {
					final int result = JOptionPane.showConfirmDialog(null, QueueController.this.resourceBundle.getString("uploadsRunningExitMessage"), UIManager.getString("OptionPane.titleText"),
																	 //NON-NLS
																	 JOptionPane.YES_NO_OPTION);
					return result == 0;
				}
				return true;
			}
		};
	}

	@EventTopicSubscriber(topic = Uploader.QUEUE_START)
	public void onQueueStart(final String topic, final Object o)
	{
		if (!this.uploader.isRunning()) {
			this.uploader.start();
		}
	}
}
