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

package org.chaosfisch.youtubeuploader.plugins.directoryplugin.models.entities;

import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.entities.IEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.entities.PresetEntry;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 07.01.12
 * Time: 18:33
 * To change this template use File | Settings | File Templates.
 */
public class DirectoryEntry implements IEntry
{
	private transient int     identity;
	private           String  directory;
	private           boolean active;
	private           boolean locked;

	public boolean isLocked()
	{
		return this.locked;
	}

	public void setLocked(final boolean locked)
	{
		this.locked = locked;
	}

	public boolean isActive()
	{
		return this.active;
	}

	public void setActive(final boolean active)
	{
		this.active = active;
	}

	public String getDirectory()
	{
		return this.directory;
	}

	public void setDirectory(final String directory)
	{
		this.directory = directory;
	}

	public int getIdentity()
	{
		return this.identity;
	}

	public void setIdentity(final int identity)
	{
		this.identity = identity;
	}

	private PresetEntry preset;

	public PresetEntry getPreset()
	{
		return this.preset;
	}

	public void setPreset(final PresetEntry preset)
	{
		this.preset = preset;
	}
}
