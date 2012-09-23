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

package org.chaosfisch.youtubeuploader.models;

public class Playlist
{
	public transient Integer	identity;
	public String				playlistKey;
	public String				title;
	public String				url;
	public String				summary;
	public Integer				number;
	public Account				account;

	@Override
	public String toString()
	{
		return title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((account == null) ? 0 : account.hashCode());
		result = prime * result + ((number == null) ? 0 : number.hashCode());
		result = prime * result + ((playlistKey == null) ? 0 : playlistKey.hashCode());
		result = prime * result + ((summary == null) ? 0 : summary.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (!(obj instanceof Playlist)) { return false; }
		Playlist other = (Playlist) obj;
		if (account == null)
		{
			if (other.account != null) { return false; }
		} else if (!account.equals(other.account)) { return false; }
		if (number == null)
		{
			if (other.number != null) { return false; }
		} else if (!number.equals(other.number)) { return false; }
		if (playlistKey == null)
		{
			if (other.playlistKey != null) { return false; }
		} else if (!playlistKey.equals(other.playlistKey)) { return false; }
		if (summary == null)
		{
			if (other.summary != null) { return false; }
		} else if (!summary.equals(other.summary)) { return false; }
		if (title == null)
		{
			if (other.title != null) { return false; }
		} else if (!title.equals(other.title)) { return false; }
		if (url == null)
		{
			if (other.url != null) { return false; }
		} else if (!url.equals(other.url)) { return false; }
		return true;
	}

}
