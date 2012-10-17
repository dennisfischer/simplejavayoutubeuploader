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

import org.chaosfisch.youtubeuploader.models.Queue;
import org.chaosfisch.youtubeuploader.services.uploader.Uploader;

import com.google.inject.Inject;

public class QueueController
{

	public static final String	ETA_PROPERTY			= "ETA";
	public static final String	FILE_PROPERTY			= "File";
	public static final String	PROGRESS_PROPERTY		= "Progress";
	public static final String	STARTTIME_PROPERTY		= "Starttime";
	public static final String	STATUS_PROPERTY			= "Status";
	public static final String	TITLE_PROPERTY			= "Title";
	public static final String	UPLOADED_BYTES_PROPERTY	= "Uploaded Bytes";

	@Inject private Uploader	uploader;

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

	public void changeSpeedLimit(final int bytes)
	{
		uploader.setSpeedLimit(bytes);
	}

	public void deleteEntry(final Queue queue)
	{
		uploader.abort(queue);
		queue.delete();
	}

	/*
	 * public void moveBottom(final Queue queue) { queueService.sort(queue,
	 * QueuePosition.QUEUE_BOTTOM); queueList.sortQueueEntry(queue,
	 * QueuePosition.QUEUE_BOTTOM); }
	 * 
	 * public void moveDown(final Queue queue) { queueService.sort(queue,
	 * QueuePosition.QUEUE_DOWN); queueList.sortQueueEntry(queue,
	 * QueuePosition.QUEUE_DOWN); }
	 * 
	 * public void moveTop(final Queue queue) { queueService.sort(queue,
	 * QueuePosition.QUEUE_TOP); queueList.sortQueueEntry(queue,
	 * QueuePosition.QUEUE_TOP); }
	 * 
	 * public void moveUp(final Queue queue) { queueService.sort(queue,
	 * QueuePosition.QUEUE_UP); queueList.sortQueueEntry(queue,
	 * QueuePosition.QUEUE_UP); }
	 */

	public void startQueue()
	{
		uploader.start();
	}

	public void stopQueue()
	{
		uploader.stop();
	}
}