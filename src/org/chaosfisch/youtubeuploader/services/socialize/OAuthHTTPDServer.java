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

import java.util.Properties;

import org.chaosfisch.google.request.HTTP_STATUS;
import org.chaosfisch.youtubeuploader.I18nHelper;

public class OAuthHTTPDServer extends NanoHTTPD
{
	private String	code;

	public OAuthHTTPDServer(final int port)
	{
		super(port, null);
	}

	@Override
	public Response serve(final String uri, final String method, final Properties header, final Properties parms, final Properties files)
	{
		if (parms.getProperty("code") != null)
		{
			code = parms.getProperty("code");
			synchronized (this)
			{
				notifyAll();
			}
			final String msg = I18nHelper.message("message.authorization.successful");
			return new Response(HTTP_STATUS.OK.toString(), NanoHTTPD.MIME_HTML, msg);
		} else if (parms.getProperty("error_reason") != null)
		{
			synchronized (this)
			{
				notifyAll();
			}
			final String msg = I18nHelper.message("message.authorization.denied");
			return new Response(HTTP_STATUS.OK.toString(), NanoHTTPD.MIME_PLAINTEXT, msg);
		}
		synchronized (this)
		{
			notifyAll();
		}
		return new Response(HTTP_STATUS.NOTFOUND.toString(), NanoHTTPD.MIME_PLAINTEXT, "Not found!");
	}

	public String getCode()
	{
		return code;
	}
}
