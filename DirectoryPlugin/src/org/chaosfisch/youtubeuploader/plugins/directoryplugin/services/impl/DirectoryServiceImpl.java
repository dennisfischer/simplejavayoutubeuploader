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

package org.chaosfisch.youtubeuploader.plugins.directoryplugin.services.impl;

import org.chaosfisch.youtubeuploader.plugins.directoryplugin.db.DirectoryEntry;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.services.DirectoryService;

import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 15.03.12
 * Time: 18:03
 * To change this template use File | Settings | File Templates.
 */
public class DirectoryServiceImpl implements DirectoryService
{
	@Override public List getAll()
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override public List getAllActive()
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override public DirectoryEntry getByFileDirectory(File file)
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override public void createDirectoryEntry(DirectoryEntry directoryEntry)
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override public void deleteDirectoryEntry(DirectoryEntry directoryEntry)
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override public void updateDirectoryEntry(DirectoryEntry directoryEntry)
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
