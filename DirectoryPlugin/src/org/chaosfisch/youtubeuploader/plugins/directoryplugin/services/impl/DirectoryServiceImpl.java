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

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.bushe.swing.event.EventBus;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Preset;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.mappers.DirectoryMapper;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.models.Directory;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.services.spi.DirectoryService;

import java.io.File;
import java.util.List;

public class DirectoryServiceImpl implements DirectoryService
{

	@Inject DirectoryMapper directoryMapper;

	@Override @Transactional public List<Directory> getAll()
	{
		return directoryMapper.getDirectories();
	}

	@Override @Transactional public List<Directory> getActive()
	{
		final Directory findEntry = new Directory();
		findEntry.active = true;
		return directoryMapper.findDirectories(findEntry);
	}

	@Override @Transactional public Directory findFile(final File file)
	{
		final String directory = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(File.separator) + 1);
		final Directory findEntry = new Directory();
		findEntry.directory = directory;
		final List<Directory> entries = directoryMapper.findDirectories(findEntry);
		if (entries.isEmpty()) {
			return null;
		}
		return entries.get(0);
	}

	@Override @Transactional public List<Directory> find(final Directory directory)
	{
		return directoryMapper.findDirectories(directory);
	}

	@Override @Transactional public Directory create(final Directory directory)
	{
		directoryMapper.createDirectory(directory);
		EventBus.publish(DirectoryService.DIRECTORY_ADDED, directory);
		return directory;
	}

	@Override @Transactional public void delete(final Directory directory)
	{
		directoryMapper.deleteDirectory(directory);
		EventBus.publish(DirectoryService.DIRECTORY_REMOVED, directory);
	}

	@Override @Transactional public Directory update(final Directory directory)
	{
		directoryMapper.updateDirectory(directory);
		EventBus.publish(DirectoryService.DIRECTORY_UPDATED, directory);
		return directory;
	}

	@Override @Transactional public List<Preset> findPresets()
	{
		return directoryMapper.getQualifiedPresets();
	}
}