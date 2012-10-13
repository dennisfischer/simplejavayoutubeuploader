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

import org.chaosfisch.youtubeuploader.models.Queue;

public class UploadProgress
{
	private double			diffBytes;
	private long			diffTime;
	private final double	fileSize;
	private final Queue		queue;
	private long			time;
	private double			totalBytesUploaded;

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

	public double getDiffBytes()
	{
		return diffBytes;
	}

	public long getDiffTime()
	{
		return diffTime;
	}

	public double getFileSize()
	{
		return fileSize;
	}

	public Queue getQueue()
	{
		return queue;
	}

	public long getTime()
	{
		return time;
	}

	public double getTotalBytesUploaded()
	{
		return totalBytesUploaded;
	}

	public void setDiffBytes(final double diffBytes)
	{
		this.diffBytes = diffBytes;
	}

	public void setDiffTime(final long diffTime)
	{
		this.diffTime = diffTime;
	}

	public void setTime(final long time)
	{
		this.time = time;
	}

	public void setTotalBytesUploaded(final double totalBytesUploaded)
	{
		this.totalBytesUploaded = totalBytesUploaded;
	}
}