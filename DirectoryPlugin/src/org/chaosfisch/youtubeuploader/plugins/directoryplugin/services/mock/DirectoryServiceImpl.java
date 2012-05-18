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

package org.chaosfisch.youtubeuploader.plugins.directoryplugin.services.mock;

import org.chaosfisch.youtubeuploader.plugins.directoryplugin.models.Directory;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.services.spi.DirectoryService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 05.05.12
 * Time: 12:44
 * To change this template use File | Settings | File Templates.
 */
public class DirectoryServiceImpl implements DirectoryService
{
	@Override public List<Directory> getAll()
	{
		return new ArrayList<Directory>(0);
	}

	@Override public List<Directory> getAllActive()
	{
		return new ArrayList<Directory>(0);
	}

	@Override public Directory findByFile(File file)
	{
		return new Directory();
	}

	@Override public Directory createDirectoryEntry(Directory directory)
	{
		return new Directory();
	}

	@Override public Directory deleteDirectoryEntry(Directory directory)
	{
		return new Directory();
	}

	@Override public Directory updateDirectoryEntry(Directory directory)
	{
		return new Directory();
	}
}
