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

import java.util.List;

public class Account
{
	public String					name;
	private String					password;
	public transient Integer		identity;
	public transient List<Playlist>	presets;
	public transient List<Queue>	queue;
	public transient List<Playlist>	playlists;

	public String getPassword()
	{
		return password;
	}

	public void setPassword(final String password)
	{
		this.password = password;
	}

	@Override
	public String toString()
	{
		return name;
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
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
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
		if (!(obj instanceof Account)) { return false; }
		Account other = (Account) obj;
		if (name == null)
		{
			if (other.name != null) { return false; }
		} else if (!name.equals(other.name)) { return false; }
		if (password == null)
		{
			if (other.password != null) { return false; }
		} else if (!password.equals(other.password)) { return false; }
		return true;
	}

}
