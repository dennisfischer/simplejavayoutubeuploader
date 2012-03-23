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

package org.chaosfisch.youtubeuploader.services.impl;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.util.AuthenticationException;
import org.chaosfisch.youtubeuploader.services.YTService;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 02.01.12
 * Time: 20:43
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class YTServiceImpl extends YouTubeService implements YTService
{

	public static final  String DEVELOPER_KEY    = "AI39si6EquMrdMz_oKMFk9rNBHqOQTUEG-kJ4I33xveO-W40U95XjJAL3-Fa9voJ3bPxkMwsT7IQKc39M3tw0o2fHswYRN0Chg";
	private static final String APPLICATION_NAME = "dennis-fischer-youtube java uploader-2.0-alpha-1";

	private String username;
	private String password;

	public YTServiceImpl()
	{
		super(APPLICATION_NAME, DEVELOPER_KEY);
	}

	public String getAuthToken() throws AuthenticationException
	{
		return this.getAuthToken(this.username, this.password, null, null, "youtube", APPLICATION_NAME);
	}

	public String getUsername()
	{
		return this.username;
	}

	public void setUsername(final String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return this.password;
	}

	public void setPassword(final String password)
	{
		this.password = password;
	}

	public void authenticate() throws AuthenticationException
	{
		this.setUserCredentials(this.username, this.password);
	}
}