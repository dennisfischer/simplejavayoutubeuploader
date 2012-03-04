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
import org.chaosfisch.plugin.ExtensionPoints.ExitExtensionPoint;
import org.chaosfisch.youtubeuploader.db.QueueEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.QueueTableModel;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.Uploader;
import org.chaosfisch.youtubeuploader.services.QueuePosition;
import org.chaosfisch.youtubeuploader.services.QueueService;

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

	private final ResourceBundle resourceBundle = ResourceBundle.getBundle("/org/chaosfisch/youtubeuploader/plugins/coreplugin/resources/queueView.properties"); //NON-NLS

	@Inject
	public QueueController(final QueueTableModel queueTableModel, final QueueService queueService, final Uploader uploader)
	{
		this.queueList = queueTableModel;
		this.queueService = queueService;
		this.uploader = uploader;
	}

	public void startQueue()
	{
		this.uploader.start();
	}

	public void stopQueue()
	{
		this.uploader.stop();
	}

	public void abortUpload(final QueueEntry queueEntry)
	{
		this.uploader.abort(queueEntry);
	}

	public void changeQueueFinished(final int item)
	{
		this.uploader.setActionOnFinish((short) item);
	}

	public void moveTop(final QueueEntry selectedRow)
	{
		this.queueService.sortQueueEntry(selectedRow, QueuePosition.QUEUE_TOP);
		this.queueList.sortQueueEntry(selectedRow, QueuePosition.QUEUE_TOP);
	}

	public void moveUp(final QueueEntry selectedRow)
	{
		this.queueService.sortQueueEntry(selectedRow, QueuePosition.QUEUE_UP);
		this.queueList.sortQueueEntry(selectedRow, QueuePosition.QUEUE_UP);
	}

	public void moveDown(final QueueEntry selectedRow)
	{
		this.queueService.sortQueueEntry(selectedRow, QueuePosition.QUEUE_DOWN);
		this.queueList.sortQueueEntry(selectedRow, QueuePosition.QUEUE_DOWN);
	}

	public void moveBottom(final QueueEntry selectedRow)
	{
		this.queueService.sortQueueEntry(selectedRow, QueuePosition.QUEUE_BOTTOM);
		this.queueList.sortQueueEntry(selectedRow, QueuePosition.QUEUE_BOTTOM);
	}

	public void changeQueueView(final short item)
	{
		this.queueList.removeAll();
		switch (item) {
			case 0:
				this.queueList.addQueueEntryList(this.queueService.getAllQueueEntry());
				break;
			case 1:
				this.queueList.addQueueEntryList(this.queueService.getArchivedQueueEntry());
				break;
			case 2:
				this.queueList.addQueueEntryList(this.queueService.getQueuedQueueEntry());
				break;
		}
	}

	public void deleteEntry(final QueueEntry queueEntityAt)
	{
		this.uploader.abort(queueEntityAt);
		this.getQueueList().removeQueueEntry(queueEntityAt);
		this.queueService.deleteQueueEntry(queueEntityAt);
	}

	public void editEntry(final QueueEntry queueEntityAt)
	{
		EventBus.publish(QueueService.EDIT_QUEUE_ENTRY, queueEntityAt);
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

	public ExitExtensionPoint uploadExitPoint()
	{
		return new ExitExtensionPoint()
		{
			@Override
			public boolean canExit()
			{
				if (QueueController.this.uploader.isRunning()) {
					final int result = JOptionPane.showConfirmDialog(null, QueueController.this.resourceBundle.getString("uploadsRunningExitMessage"), UIManager.getString("OptionPane.titleText"), //NON-NLS
							JOptionPane.YES_NO_OPTION);
					return result == 0;
				}
				return true;
			}
		};
	}
}
