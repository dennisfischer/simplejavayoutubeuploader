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

package org.chaosfisch.youtubeuploader.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bushe.swing.event.EventBus;
import org.chaosfisch.util.Mimetype;
import org.chaosfisch.util.logger.InjectLogger;
import org.chaosfisch.youtubeuploader.dao.spi.DirectoryDao;
import org.chaosfisch.youtubeuploader.dao.spi.PlaylistDao;
import org.chaosfisch.youtubeuploader.dao.spi.QueueDao;
import org.chaosfisch.youtubeuploader.models.Directory;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.models.Preset;
import org.chaosfisch.youtubeuploader.models.Queue;
import org.chaosfisch.youtubeuploader.services.uploader.Uploader;

import com.google.inject.Inject;
import com.teamdev.filewatch.FileEvent;
import com.teamdev.filewatch.FileEventFilter;
import com.teamdev.filewatch.FileEventsAdapter;
import com.teamdev.filewatch.FileEventsListener;
import com.teamdev.filewatch.FileWatcher;
import com.teamdev.filewatch.WatchingAttributes;

public class DirectoryWorker extends Thread
{
	@Inject
	private QueueDao				queueService;
	@Inject
	private DirectoryDao			directoryService;
	@Inject
	private PlaylistDao				playlistService;
	@InjectLogger
	private Logger					logger;
	final Collection<FileWatcher>	fileWatcherList	= new ArrayList<FileWatcher>(50);
	final Collection<File>			inProgress		= new ArrayList<File>(10);

	@Override
	public void run()
	{

		final List<Directory> directories = directoryService.getActive();
		final FileEventsListener fileEventsAdapter = new FileEventsAdapter() {
			@Override
			public void fileAdded(final FileEvent.Added added)
			{
				if (!inProgress.contains(added.getFile()))
				{
					inProgress.add(added.getFile());
					addToUpload(added.getFile());
				}
			}

			@Override
			public void fileChanged(final FileEvent.Changed changed)
			{
				if (!inProgress.contains(changed.getFile()))
				{
					inProgress.add(changed.getFile());
					addToUpload(changed.getFile());
				}
			}
		};

		final Set<WatchingAttributes> watchingAttributes = EnumSet.allOf(WatchingAttributes.class);
		watchingAttributes.remove(WatchingAttributes.DirectoryName);
		watchingAttributes.remove(WatchingAttributes.Subtree);

		final FileEventFilter fileMaskFilter = new MediaFileFilter();

		for (final Directory directory : directories)
		{
			final File file = new File(directory.directory);
			final FileWatcher fileWatcher = FileWatcher.create(file);
			fileWatcher.addFileEventsListener(fileEventsAdapter);
			fileWatcher.setOptions(watchingAttributes);
			fileWatcher.setFilter(fileMaskFilter);
			fileWatcher.start();
			fileWatcherList.add(fileWatcher);
		}
	}

	private void addToUpload(final File file)
	{
		final Directory directory = directoryService.findFile(file);
		if (directory == null) { return; }

		final Preset preset = directory.preset;

		final Queue queue = new Queue();
		final Playlist playlist = preset.playlist;

		if ((preset.title != null) && !preset.title.isEmpty())
		{
			queue.title = preset.title;
		} else
		{
			queue.title = file.getName();
		}
		queue.file = file.getAbsolutePath();
		queue.account = preset.account;
		queue.category = preset.category;
		queue.description = preset.description;
		queue.keywords = preset.keywords;
		queue.comment = preset.comment;
		queue.commentvote = preset.commentvote;
		queue.embed = preset.embed;
		queue.mobile = preset.mobile;
		queue.rate = preset.rate;
		queue.videoresponse = preset.videoresponse;
		queue.monetize = preset.monetize;
		queue.monetizeOverlay = preset.monetizeOverlay;
		queue.monetizeTrueview = preset.monetizeTrueview;
		queue.monetizeProduct = preset.monetizeProduct;
		queue.enddir = preset.enddir;

		switch (preset.visibility)
		{
			case 1:
				queue.unlisted = true;
				break;
			case 2:
				queue.privatefile = true;
				break;
		}

		final int dotPos = file.toString().lastIndexOf(".") + 1;
		final String extension = file.toString().substring(dotPos);
		queue.mimetype = Mimetype.getMimetypeByExtension(extension);

		queue.playlist = playlist;
		if (playlist != null)
		{
			playlist.number++;
			playlistService.update(playlist);
		}

		queueService.create(queue);
		EventBus.publish(Uploader.QUEUE_START, null);
	}

	public void stopActions()
	{
		for (final FileWatcher fileWatcher : fileWatcherList)
		{
			fileWatcher.stop();
		}
	}

	private static class MediaFileFilter implements FileEventFilter
	{

		private static final long	WAIT_CHECKTIME	= 750;

		@Override
		public boolean accept(final FileEvent fileEvent)
		{
			final File file = fileEvent.getFile();
			final String[] extensions = Mimetype.EXTENSIONS;
			final int dotPos = file.toString().lastIndexOf(".") + 1;
			final String fileExtension = file.toString().substring(dotPos);

			boolean flag = false;
			for (final String extension : extensions)
			{
				if (extension.equals(fileExtension))
				{
					flag = true;
				}
			}
			if (!flag) { return false; }

			final long checkedAt = file.lastModified();
			final long fileSizeAt = file.length();
			try
			{
				Thread.sleep(MediaFileFilter.WAIT_CHECKTIME);
			} catch (InterruptedException ignored)
			{
				throw new RuntimeException("This shouldn't happen");
			}
			return !((file.lastModified() != checkedAt) || (fileSizeAt != file.length()));
		}
	}
}
