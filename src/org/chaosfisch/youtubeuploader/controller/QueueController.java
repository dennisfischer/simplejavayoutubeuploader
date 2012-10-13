/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
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

package org.chaosfisch.youtubeuploader.controller;

import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.youtubeuploader.dao.spi.QueueDao;
import org.chaosfisch.youtubeuploader.models.Queue;
import org.chaosfisch.youtubeuploader.models.QueuePosition;
import org.chaosfisch.youtubeuploader.services.uploader.Uploader;
import org.chaosfisch.youtubeuploader.view.UploadViewPanel;

import com.google.inject.Inject;

public class QueueController
{

	public static final String		ETA_PROPERTY			= "ETA";																						// NON-NLS
	public static final String		FILE_PROPERTY			= "File";																						// NON-NLS
	public static final String		PROGRESS_PROPERTY		= "Progress";																					// NON-NLS
	public static final String		STARTTIME_PROPERTY		= "Starttime";																					// NON-NLS
	public static final String		STATUS_PROPERTY			= "Status";																					// NON-NLS
	public static final String		TITLE_PROPERTY			= "Title";																						// NON-NLS
	public static final String		UPLOADED_BYTES_PROPERTY	= "Uploaded Bytes";																			// NON-NLS

	private final QueueTableModel	queueList;
	private final QueueDao			queueService;
	private final ResourceBundle	resourceBundle			= ResourceBundle
																	.getBundle("org.chaosfisch.youtubeuploader.plugins.coreplugin.resources.coreplugin");	// NON-NLS

	private final Uploader			uploader;

	@Inject
	public QueueController(final QueueTableModel queueTableModel, final QueueDao queueService, final Uploader uploader)
	{
		queueList = queueTableModel;
		this.queueService = queueService;
		this.uploader = uploader;
		AnnotationProcessor.process(this);
	}

	public void abortUpload(final Queue queue)
	{
		uploader.abort(queue);
	}

	public void changeMaxUpload(final short maxUploads)
	{
		uploader.setMaxUploads(maxUploads);
	}

	public void changeQueueFinished(final int item)
	{
		uploader.setActionOnFinish((short) item);
	}

	public void changeQueueView(final short item)
	{
		if (queueList.getRowCount() > 0)
		{
			System.out.println(queueList.getRowCount());
			queueList.removeRowRange(0, queueList.getRowCount() - 1);
		}
		switch (item)
		{
			case 0:
				for (final Queue queue : queueService.getAll())
				{
					queueList.addRow(queue);
				}
				break;
			case 1:
				for (final Queue queue : queueService.getArchived())
				{
					queueList.addRow(queue);
				}
				break;
			case 2:
				for (final Queue queue : queueService.getQueued())
				{
					queueList.addRow(queue);
				}
				break;
		}
	}

	public void changeSpeedLimit(final int bytes)
	{
		uploader.setSpeedLimit(bytes);
	}

	public void deleteEntry(final Queue queue)
	{
		uploader.abort(queue);
		getQueueList().removeElement(queue);
		queueService.delete(queue);
	}

	public void editEntry(final Queue queue)
	{
		EventBus.publish(UploadViewPanel.EDIT_QUEUE_ENTRY, queue);
	}

	public QueueTableModel getQueueList()
	{
		return queueList;
	}

	public QueueDao getQueueService()
	{
		return queueService;
	}

	public void moveBottom(final Queue queue)
	{
		queueService.sort(queue, QueuePosition.QUEUE_BOTTOM);
		queueList.sortQueueEntry(queue, QueuePosition.QUEUE_BOTTOM);
	}

	public void moveDown(final Queue queue)
	{
		queueService.sort(queue, QueuePosition.QUEUE_DOWN);
		queueList.sortQueueEntry(queue, QueuePosition.QUEUE_DOWN);
	}

	public void moveTop(final Queue queue)
	{
		queueService.sort(queue, QueuePosition.QUEUE_TOP);
		queueList.sortQueueEntry(queue, QueuePosition.QUEUE_TOP);
	}

	public void moveUp(final Queue queue)
	{
		queueService.sort(queue, QueuePosition.QUEUE_UP);
		queueList.sortQueueEntry(queue, QueuePosition.QUEUE_UP);
	}

	@EventTopicSubscriber(topic = Uploader.QUEUE_START)
	public void onQueueStart(final String topic, final Object o)
	{
		if (!uploader.isRunning())
		{
			uploader.start();
		}
	}

	public void startQueue()
	{
		uploader.start();
	}

	public void stopQueue()
	{
		uploader.stop();
	}

	public ExtensionPoint uploadExitPoint()
	{
		return new ExitExtensionPoint() {
			@Override
			public boolean canExit()
			{
				if (uploader.isRunning())
				{
					final int result = JOptionPane.showConfirmDialog(null, resourceBundle.getString("uploadsRunningExitMessage"),
							UIManager.getString("OptionPane.titleText"),
							// NON-NLS
							JOptionPane.YES_NO_OPTION);
					return result == 0;
				}
				return true;
			}
		};
	}
}
