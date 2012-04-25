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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.models.entities;

import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.impl.YTServiceImpl;

import java.util.Collection;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 07.01.12
 * Time: 16:44
 * To change this template use File | Settings | File Templates.
 */
public class AccountEntry implements IEntry
{
	private transient int                identity;
	private           String             name;
	private           String             password;
	private           String             secret;
	private           Set<PlaylistEntry> presets;

	public Set<PlaylistEntry> getPresets()
	{
		return this.presets;
	}

	public void setPresets(final Set<PlaylistEntry> presets)
	{
		this.presets = presets;
	}

	public String getSecret()
	{
		return this.secret;
	}

	public void setSecret(final String secret)
	{
		this.secret = secret;
	}

	public String getPassword()
	{
		return this.password;
	}

	public void setPassword(final String password)
	{
		this.password = password;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public int getIdentity()
	{
		return this.identity;
	}

	public void setIdentity(final int identity)
	{
		this.identity = identity;
	}

	private transient Collection<QueueEntry> queue;

	public Collection<QueueEntry> getQueue()
	{
		return this.queue;
	}

	public void setQueue(final Collection<QueueEntry> queue)
	{
		this.queue = queue;
	}

	public String toString()
	{
		return this.getName();
	}

	public YTServiceImpl getYoutubeServiceManager()
	{
		final YTServiceImpl youtubeServiceManager = new YTServiceImpl();
		youtubeServiceManager.setUsername(this.name);
		youtubeServiceManager.setPassword(this.password);

		return youtubeServiceManager;
	}

	private transient Collection<PlaylistEntry> playlists;

	public Collection<PlaylistEntry> getPlaylists()
	{
		return this.playlists;
	}

	public void setPlaylists(final Collection<PlaylistEntry> playlists)
	{
		this.playlists = playlists;
	}
}
