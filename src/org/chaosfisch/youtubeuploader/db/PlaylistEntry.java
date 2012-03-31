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

package org.chaosfisch.youtubeuploader.db;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 13.01.12
 * Time: 15:57
 * To change this template use File | Settings | File Templates.
 */
public class PlaylistEntry implements IEntry
{
	private int    identity;
	private String playlistKey;
	private String name;
	private String url;
	private String summary;
	private int    number;

	public int getIdentity()
	{
		return this.identity;
	}

	public void setIdentity(final int identity)
	{
		this.identity = identity;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public String getUrl()
	{
		return this.url;
	}

	public void setUrl(final String url)
	{
		this.url = url;
	}

	public int getNumber()
	{
		return this.number;
	}

	public void setNumber(final int number)
	{
		this.number = number;
	}

	public String getSummary()
	{
		return this.summary;
	}

	public void setSummary(final String summary)
	{
		this.summary = summary;
	}

	private AccountEntry account;

	public AccountEntry getAccount()
	{
		return this.account;
	}

	public void setAccount(final AccountEntry account)
	{
		this.account = account;
	}

	private Collection<PlaylistEntry> presets;

	public Collection<PlaylistEntry> getPresets()
	{
		return this.presets;
	}

	public void setPresets(final Collection<PlaylistEntry> presets)
	{
		this.presets = presets;
	}

	public String toString()
	{
		return this.name;
	}

	private Collection<QueueEntry> queue;

	public Collection<QueueEntry> getQueue()
	{
		return this.queue;
	}

	public void setQueue(final Collection<QueueEntry> queue)
	{
		this.queue = queue;
	}

	public String getPlaylistKey()
	{
		return this.playlistKey;
	}

	public void setPlaylistKey(final String playlistKey)
	{
		this.playlistKey = playlistKey;
	}
}
