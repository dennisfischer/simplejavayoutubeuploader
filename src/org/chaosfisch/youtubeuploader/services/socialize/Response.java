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

package org.chaosfisch.youtubeuploader.services.socialize;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.chaosfisch.google.request.HTTP_STATUS;

/**
 * HTTP response. Return one of these from serve().
 */
public class Response
{
	/**
	 * Default constructor: response = HTTP_OK, data = mime = 'null'
	 */
	public Response()
	{
		status = HTTP_STATUS.OK.toString();
	}

	/**
	 * Basic constructor.
	 */
	public Response(final String status, final String mimeType, final InputStream data)
	{
		this.status = status;
		this.mimeType = mimeType;
		this.data = data;
	}

	/**
	 * Convenience method that makes an InputStream out of given text.
	 */
	public Response(final String status, final String mimeType, final String txt)
	{
		this.status = status;
		this.mimeType = mimeType;
		try
		{
			data = new ByteArrayInputStream(txt.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException uee)
		{
			uee.printStackTrace();
		}
	}

	/**
	 * Adds given line to the header.
	 */
	public void addHeader(final String name, final String value)
	{
		header.put(name, value);
	}

	/**
	 * HTTP status code after processing, e.g. "200 OK", HTTP_OK
	 */
	public final String		status;

	/**
	 * MIME type of content, e.g. "text/html"
	 */
	public String			mimeType;

	/**
	 * Data of the response, may be null.
	 */
	public InputStream		data;

	/**
	 * Headers for the HTTP response. Use addHeader() to add lines.
	 */
	public final Properties	header	= new Properties();
}
