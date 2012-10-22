/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.bushe.swing.event.EventBus;
import org.chaosfisch.util.Mimetype;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Directory;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.models.Preset;
import org.chaosfisch.youtubeuploader.models.Queue;
import org.chaosfisch.youtubeuploader.services.uploader.Uploader;
import org.javalite.activejdbc.Model;

import com.teamdev.filewatch.FileEvent;
import com.teamdev.filewatch.FileEventFilter;
import com.teamdev.filewatch.FileEventsAdapter;
import com.teamdev.filewatch.FileEventsListener;
import com.teamdev.filewatch.FileWatcher;
import com.teamdev.filewatch.WatchingAttributes;

public class DirectoryWorker extends Thread
{

	final Collection<FileWatcher>	fileWatcherList	= new ArrayList<FileWatcher>(50);
	final Collection<File>			inProgress		= new ArrayList<File>(10);

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
			} catch (final InterruptedException ignored)
			{
				throw new RuntimeException("This shouldn't happen");
			}
			return !((file.lastModified() != checkedAt) || (fileSizeAt != file.length()));
		}
	}

	private void addToUpload(final File file)
	{
		final Directory directory = Model.findFirst("directory = ?",
				file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(File.separator)));
		if (directory == null) { return; }

		final Preset preset = directory.parent(Preset.class);
		final Playlist playlist = preset.parent(Playlist.class);

		String title;
		if ((preset.getString("title") != null) && !preset.getString("title").isEmpty())
		{
			title = preset.getString("title");
		} else
		{
			title = file.getName();
		}
		boolean unlisted = false, privatefile = false;
		switch (preset.getInteger("visibility"))
		{
			case 1:
				unlisted = true;
				break;
			case 2:
				privatefile = true;
				break;
		}

		final int dotPos = file.toString().lastIndexOf(".") + 1;
		final String extension = file.toString().substring(dotPos);

		// TODO MAYBE UPDATE PLAYLIST NUMBER; OR SOMETHING LIKE THAT!

		final Queue queue = Model.create("title", title, "file", file.getAbsolutePath(), "category", preset.getString("category"), "description",
				preset.getString("description"), "keywords", preset.getString("keywords"), "comment", preset.getInteger("comment"), "commentvote",
				preset.getInteger("commentvote"), "embed", preset.getBoolean("embed"), "mobile", preset.getBoolean("mobile"), "rate",
				preset.getBoolean("rate"), "vidoeresponse", preset.getInteger("videoresponse"), "monetize", preset.getBoolean("monetize"),
				"monetizeOverlay", preset.getBoolean("monetizeOverlay"), "monetizeTrueview", preset.getBoolean("monetizeTrueview"),
				"monetizeProduct", preset.getBoolean("monetizeProduct"), "enddir", preset.getString("enddir"), "account_id",
				preset.parent(Account.class).getLongId(), "mimetype", Mimetype.getMimetypeByExtension(extension), "unlisted", unlisted,
				"privatefile", privatefile);
		if (playlist != null)
		{
			playlist.add(queue);
		}
		queue.saveIt();

		EventBus.publish(Uploader.QUEUE_START, null);
	}

	@Override
	public void run()
	{

		final List<Directory> directories = Model.find("active = ?", true);
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
			final File file = new File(directory.getString("directory"));
			final FileWatcher fileWatcher = FileWatcher.create(file);
			fileWatcher.addFileEventsListener(fileEventsAdapter);
			fileWatcher.setOptions(watchingAttributes);
			fileWatcher.setFilter(fileMaskFilter);
			fileWatcher.start();
			fileWatcherList.add(fileWatcher);
		}
	}

	public void stopActions()
	{
		for (final FileWatcher fileWatcher : fileWatcherList)
		{
			fileWatcher.stop();
		}
	}
}
