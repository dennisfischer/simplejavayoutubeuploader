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

package org.chaosfisch.youtubeuploader.services;

import com.google.gdata.data.IEntry;
import com.google.gdata.data.IFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

import java.io.IOException;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 26.02.12
 * Time: 20:47
 * To change this template use File | Settings | File Templates.
 */
public interface YTService
{
	void setPassword(final String password);

	String getPassword();

	void setUsername(final String username);

	String getUsername();

	String getAuthToken() throws AuthenticationException;

	void authenticate() throws AuthenticationException;

	@SuppressWarnings("RedundantThrows") <E extends IEntry> E insert(URL feedUrl, E entry) throws IOException, ServiceException;

	@SuppressWarnings("RedundantThrows") <F extends IFeed> F getFeed(URL feedUrl, Class<F> feedClass) throws IOException, ServiceException;
}
