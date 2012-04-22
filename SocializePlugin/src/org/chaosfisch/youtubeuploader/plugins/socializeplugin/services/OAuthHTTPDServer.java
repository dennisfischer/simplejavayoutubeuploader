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

package org.chaosfisch.youtubeuploader.plugins.socializeplugin.services;

import java.io.IOException;
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
	private String code = null;

	public OAuthHTTPDServer(final int port) throws IOException
	{
		super(port, null);    //To change body of overridden methods use File | Settings | File Templates.
	}

	@Override public Response serve(final String uri, final String method, final Properties header, final Properties parms, final Properties files)
	{
		if (parms.getProperty("code") != null) {
			this.code = parms.getProperty("code");
			synchronized (this) {
				this.notifyAll();
			}
			final String msg = "Danke für die Autorisierung!";
			return new Response(HTTP_OK, MIME_HTML, msg);
		} else if (parms.getProperty("error_reason") != null) {
			synchronized (this) {
				this.notifyAll();
			}
			final String msg = "Die Autorisierung wurde durch den Benutzer abgelehnt!";
			return new Response(HTTP_OK, MIME_PLAINTEXT, msg);
		}
		synchronized (this) {
			this.notifyAll();
		}
		return new Response(HTTP_NOTFOUND, MIME_PLAINTEXT, "Not found!");
	}

	public String getCode()
	{
		return this.code;
	}
}
