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

package org.chaosfisch.youtubeuploader.services.socialize;

import org.chaosfisch.google.request.HTTP_STATUS;
import org.chaosfisch.youtubeuploader.I18nHelper;

import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 18.04.12
 * Time: 13:11
 * To change this template use File | Settings | File Templates.
 */
public class OAuthHTTPDServer extends NanoHTTPD
{
	private String code;

	public OAuthHTTPDServer(final int port)
	{
		super(port, null);    //To change body of overridden methods use File | Settings | File Templates.
	}

	@Override public Response serve(final String uri, final String method, final Properties header, final Properties parms, final Properties files)
	{
		if (parms.getProperty("code") != null) { //NON-NLS
			code = parms.getProperty("code");//NON-NLS
			synchronized (this) {
				notifyAll();
			}
			final String msg = I18nHelper.message("message.authorization.successful");
			return new Response(HTTP_STATUS.OK.toString(), NanoHTTPD.MIME_HTML, msg);
		} else if (parms.getProperty("error_reason") != null) {//NON-NLS
			synchronized (this) {
				notifyAll();
			}
			final String msg = I18nHelper.message("message.authorization.denied");
			return new Response(HTTP_STATUS.OK.toString(), NanoHTTPD.MIME_PLAINTEXT, msg);
		}
		synchronized (this) {
			notifyAll();
		}
		return new Response(HTTP_STATUS.NOTFOUND.toString(), NanoHTTPD.MIME_PLAINTEXT, "Not found!"); //NON-NLS
	}

	public String getCode()
	{
		return code;
	}
}
