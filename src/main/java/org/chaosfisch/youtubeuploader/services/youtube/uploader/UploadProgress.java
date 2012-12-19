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

import org.chaosfisch.youtubeuploader.models.Upload;

public class UploadProgress
{
	private double			diffBytes;
	private long			diffTime;
	private final double	fileSize;
	public final Upload		queue;
	private long			time;
	private double			totalBytesUploaded;
	public boolean			failed;
	public boolean			done;
	public String			status;

	public UploadProgress(final Upload queue, final double fileSize)
	{
		this.queue = queue;
		this.fileSize = fileSize;
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

	public Upload getQueue()
	{
		return queue;
	}

	public double getTotalBytesUploaded()
	{
		return totalBytesUploaded;
	}

	public long getTime()
	{
		return time;
	}

	public void setBytes(final double addBytes)
	{
		diffBytes = addBytes - totalBytesUploaded;
		totalBytesUploaded += diffBytes;
	}

	public void setTime(final long diffTime)
	{
		this.diffTime = diffTime;
		time += diffTime;
	}
}