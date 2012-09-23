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

import org.chaosfisch.youtubeuploader.models.Queue;

public class UploadProgress
{
	private final Queue		queue;
	private final double	fileSize;
	private long			time;
	private double			totalBytesUploaded;
	private double			diffBytes;
	private long			diffTime;

	public UploadProgress(final Queue queue, final double fileSize, final double totalBytesUploaded, final double diffBytes, final long time,
			final long diffTime)
	{
		this.queue = queue;
		this.fileSize = fileSize;
		this.totalBytesUploaded = totalBytesUploaded;
		this.diffBytes = diffBytes;
		this.time = time;
		this.diffTime = diffTime;
	}

	public Queue getQueue()
	{
		return queue;
	}

	public double getFileSize()
	{
		return fileSize;
	}

	public double getTotalBytesUploaded()
	{
		return totalBytesUploaded;
	}

	public long getTime()
	{
		return time;
	}

	public void setTotalBytesUploaded(final double totalBytesUploaded)
	{
		this.totalBytesUploaded = totalBytesUploaded;
	}

	public double getDiffBytes()
	{
		return diffBytes;
	}

	public void setDiffBytes(final double diffBytes)
	{
		this.diffBytes = diffBytes;
	}

	public void setTime(final long time)
	{
		this.time = time;
	}

	public long getDiffTime()
	{
		return diffTime;
	}

	public void setDiffTime(final long diffTime)
	{
		this.diffTime = diffTime;
	}
}
