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

package org.chaosfisch.youtubeuploader.plugins.directoryplugin.worker;

import com.google.inject.Inject;
import com.teamdev.filewatch.*;
import org.apache.log4j.Logger;
import org.bushe.swing.event.EventBus;
import org.chaosfisch.util.Mimetype;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Playlist;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Preset;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Queue;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PlaylistService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.QueueService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.Uploader;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.spi.AutoTitleGenerator;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.models.Directory;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.services.spi.DirectoryService;
import org.chaosfisch.youtubeuploader.util.logger.InjectLogger;

import java.io.File;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 02.01.12
 * Time: 20:29
 * To change this template use File | Settings | File Templates.
 */
public class DirectoryWorker extends Thread
{
	@Inject private       QueueService       queueService;
	@Inject private       DirectoryService   directoryService;
	@Inject private       PlaylistService    playlistService;
	@Inject private       AutoTitleGenerator autoTitleGenerator;
	@InjectLogger private Logger             logger;
	final Collection<FileWatcher> fileWatcherList = new ArrayList<FileWatcher>(50);
	final Collection<File>        inProgress      = new ArrayList<File>(10);

	@Override
	public void run()
	{

		final List<Directory> directories = directoryService.getActive();
		final FileEventsListener fileEventsAdapter = new FileEventsAdapter()
		{
			@Override public void fileAdded(final FileEvent.Added added)
			{
				if (!inProgress.contains(added.getFile())) {
					inProgress.add(added.getFile());
					addToUpload(added.getFile());
				}
			}

			@Override public void fileChanged(final FileEvent.Changed changed)
			{
				if (!inProgress.contains(changed.getFile())) {
					inProgress.add(changed.getFile());
					addToUpload(changed.getFile());
				}
			}
		};

		final Set<WatchingAttributes> watchingAttributes = EnumSet.allOf(WatchingAttributes.class);
		watchingAttributes.remove(WatchingAttributes.DirectoryName);
		watchingAttributes.remove(WatchingAttributes.Subtree);

		final FileEventFilter fileMaskFilter = new MediaFileFilter();

		for (final Directory directory : directories) {
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
		if (directory == null) {
			return;
		}

		final Preset preset = directory.preset;

		final Queue queue = new Queue();
		final Playlist playlist = preset.playlist;

		if (preset.autotitle) {
			autoTitleGenerator.setFileName(file.getName());
			autoTitleGenerator.setFormatString(preset.autotitleFormat);
			autoTitleGenerator.setPlaylist(preset.playlist);
			queue.title = autoTitleGenerator.gernerate();
		} else {
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

		switch (preset.visibility) {
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
		if (playlist != null) {
			playlist.number++;
			playlistService.update(playlist);
		}

		queueService.create(queue);
		EventBus.publish(Uploader.QUEUE_START, null);
	}

	public void stopActions()
	{
		for (final FileWatcher fileWatcher : fileWatcherList) {
			fileWatcher.stop();
		}
	}

	private static class MediaFileFilter implements FileEventFilter
	{

		private static final long WAIT_CHECKTIME = 750;

		public boolean accept(final FileEvent fileEvent)
		{
			final File file = fileEvent.getFile();
			final String[] extensions = Mimetype.EXTENSIONS;
			final int dotPos = file.toString().lastIndexOf(".") + 1;
			final String fileExtension = file.toString().substring(dotPos);

			boolean flag = false;
			for (final String extension : extensions) {
				//noinspection CallToStringEquals
				if (extension.equals(fileExtension)) {
					flag = true;
				}
			}
			if (!flag) {
				return false;
			}

			final long checkedAt = file.lastModified();
			final long fileSizeAt = file.length();
			try {
				Thread.sleep(MediaFileFilter.WAIT_CHECKTIME);
			} catch (InterruptedException ignored) {
				throw new RuntimeException("This shouldn't happen");
			}
			return !((file.lastModified() != checkedAt) || (fileSizeAt != file.length()));
		}
	}
}