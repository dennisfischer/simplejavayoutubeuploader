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

public class UploadException extends Exception
{
	private static final long	serialVersionUID	= 1097833255891875198L;

	public UploadException(final String message)
	{
		super(message);
	}

	public UploadException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}