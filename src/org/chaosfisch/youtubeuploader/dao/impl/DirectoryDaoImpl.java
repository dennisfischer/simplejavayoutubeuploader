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

package org.chaosfisch.youtubeuploader.dao.impl;

import java.io.File;
import java.util.List;

import org.bushe.swing.event.EventBus;
import org.chaosfisch.youtubeuploader.dao.mappers.DirectoryMapper;
import org.chaosfisch.youtubeuploader.dao.spi.DirectoryDao;
import org.chaosfisch.youtubeuploader.models.Directory;
import org.chaosfisch.youtubeuploader.models.Preset;
import org.mybatis.guice.transactional.Transactional;

import com.google.inject.Inject;

public class DirectoryDaoImpl implements DirectoryDao
{

	@Inject
	DirectoryMapper	directoryMapper;

	@Override
	@Transactional
	public List<Directory> getAll()
	{
		return directoryMapper.getDirectories();
	}

	@Override
	@Transactional
	public List<Directory> getActive()
	{
		final Directory findEntry = new Directory();
		findEntry.active = true;
		return directoryMapper.findMultiple(findEntry);
	}

	@Override
	@Transactional
	public Directory findFile(final File file)
	{
		final Directory findEntry = new Directory();
		findEntry.directory = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(File.separator));
		return directoryMapper.findDirectories(findEntry);
	}

	@Override
	@Transactional
	public Directory find(final Directory directory)
	{
		return directoryMapper.findDirectories(directory);
	}

	@Override
	@Transactional
	public Directory create(final Directory directory)
	{
		EventBus.publish(DirectoryDao.DIRECTORY_PRE_ADDED, directory);
		directoryMapper.createDirectory(directory);
		EventBus.publish(DirectoryDao.DIRECTORY_POST_ADDED, directory);
		return directory;
	}

	@Override
	@Transactional
	public void delete(final Directory directory)
	{
		EventBus.publish(DirectoryDao.DIRECTORY_PRE_REMOVED, directory);
		directoryMapper.deleteDirectory(directory);
		EventBus.publish(DirectoryDao.DIRECTORY_POST_REMOVED, directory);
	}

	@Override
	@Transactional
	public Directory update(final Directory directory)
	{
		EventBus.publish(DirectoryDao.DIRECTORY_PRE_UPDATED, directory);
		directoryMapper.updateDirectory(directory);
		EventBus.publish(DirectoryDao.DIRECTORY_POST_UPDATED, directory);
		return directory;
	}

	@Override
	@Transactional
	public List<Preset> findPresets()
	{
		return directoryMapper.getQualifiedPresets();
	}
}
