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
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bushe.swing.event.EventBus;
import org.chaosfisch.util.Mimetype;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Directory;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.models.Preset;
import org.chaosfisch.youtubeuploader.models.Queue;
import org.chaosfisch.youtubeuploader.services.uploader.Uploader;
import org.javalite.activejdbc.Model;

public class DirectoryWorker extends Thread
{

	final Collection<File>				inProgress		= new ArrayList<File>(10);
	final WatchService					watcher;
	final MediaFileFilter				mediaFileFilter	= new MediaFileFilter();
	private final Collection<WatchKey>	watchKeys		= new ArrayList<WatchKey>(20);

	public DirectoryWorker() throws IOException
	{
		watcher = FileSystems.getDefault().newWatchService();
	}

	private static class MediaFileFilter
	{

		private static final long	WAIT_CHECKTIME	= 750;

		public boolean accept(final Path path)
		{
			final File file = path.toFile();
			if (!file.isFile()) { return false; }
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
			{}
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

		for (final Directory directory : directories)
		{
			final Path dir = Paths.get(directory.getString("directory"));
			try
			{
				final WatchKey key = dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
						StandardWatchEventKinds.ENTRY_MODIFY);
				watchKeys.add(key);
			} catch (final IOException ex)
			{
				ex.printStackTrace();
			}
		}

		for (;;)
		{
			WatchKey key;
			try
			{
				key = watcher.take();
			} catch (final InterruptedException x)
			{
				return;
			}

			for (final WatchEvent<?> event : key.pollEvents())
			{
				final WatchEvent.Kind<?> kind = event.kind();

				if (kind == StandardWatchEventKinds.OVERFLOW)
				{
					continue;
				} else if ((kind == StandardWatchEventKinds.ENTRY_CREATE) || (kind == StandardWatchEventKinds.ENTRY_MODIFY))
				{

					final WatchEvent<Path> ev = (WatchEvent<Path>) event;
					final Path filename = ev.context();

					if (!inProgress.contains(filename.toFile()) && mediaFileFilter.accept(filename))
					{
						inProgress.add(filename.toFile());
						addToUpload(filename.toFile());
					}
				}
			}

			final boolean valid = key.reset();
			if (!valid)
			{
				break;
			}
		}

	}

	public void stopActions()
	{
		for (final WatchKey key : watchKeys)
		{
			key.cancel();
		}
	}
}
