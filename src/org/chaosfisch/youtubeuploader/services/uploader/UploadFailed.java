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

public class UploadFailed
{
	private final String	message;
	private final Queue		queue;

	public UploadFailed(final Queue queue, final String message)
	{
		this.queue = queue;
		this.message = message;
	}

	public String getMessage()
	{
		return message;
	}

	public Queue getQueue()
	{
		return queue;
	}
}
