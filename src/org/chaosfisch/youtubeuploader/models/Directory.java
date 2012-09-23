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

public class Directory
{
	public String				directory;
	public Boolean				active;
	public Boolean				locked;
	public transient Integer	identity;
	public Preset				preset;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Directory [directory=" + directory + ", active=" + active + ", locked=" + locked + ", preset=" + preset + "]";
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
		result = prime * result + ((active == null) ? 0 : active.hashCode());
		result = prime * result + ((directory == null) ? 0 : directory.hashCode());
		result = prime * result + ((locked == null) ? 0 : locked.hashCode());
		result = prime * result + ((preset == null) ? 0 : preset.hashCode());
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
		if (!(obj instanceof Directory)) { return false; }
		Directory other = (Directory) obj;
		if (active == null)
		{
			if (other.active != null) { return false; }
		} else if (!active.equals(other.active)) { return false; }
		if (directory == null)
		{
			if (other.directory != null) { return false; }
		} else if (!directory.equals(other.directory)) { return false; }
		if (locked == null)
		{
			if (other.locked != null) { return false; }
		} else if (!locked.equals(other.locked)) { return false; }
		if (preset == null)
		{
			if (other.preset != null) { return false; }
		} else if (!preset.equals(other.preset)) { return false; }
		return true;
	}

}
