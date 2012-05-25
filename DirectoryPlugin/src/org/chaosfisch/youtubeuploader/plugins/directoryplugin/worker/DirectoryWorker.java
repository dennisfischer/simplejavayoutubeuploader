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
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
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
import java.io.FileFilter;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 02.01.12
 * Time: 20:29
 * To change this template use File | Settings | File Templates.
 */
public class DirectoryWorker extends Thread
{
	private               boolean               stop;
	private               long                  intervall;
	private               FileAlterationMonitor fileAlterationMonitor;
	@Inject private       QueueService          queueService;
	@Inject private       DirectoryService      directoryService;
	@Inject private       PlaylistService       playlistService;
	@Inject private       AutoTitleGenerator    autoTitleGenerator;
	@InjectLogger private Logger                logger;

	public DirectoryWorker()
	{
	}

	@Override
	public void run()
	{
		this.loadDirectoryWorker();
	}

	private void loadDirectoryWorker()
	{
		final FileAlterationObserver[] fileAlterationObservers = this.initObservers();
		this.fileAlterationMonitor = new FileAlterationMonitor(this.intervall, fileAlterationObservers);

		try {
			this.fileAlterationMonitor.start();
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		this.stop = false;
		while (!this.stop) {
			try {
				Thread.sleep(this.intervall);
			} catch (InterruptedException ignored) {
			}
		}
	}

	private FileAlterationObserver[] initObservers()
	{
		final List<Directory> directories = this.directoryService.getActive();
		final FileAlterationObserver[] fileAlterationObservers = new FileAlterationObserver[20];
		final FileFilter mediaFileFilter = new MediaFileFilter();
		final FileAlterationListener fileAlterationListener = new MediaFileAlternationListener();
		int i = 0;
		for (final Directory directory : directories) {
			this.logger.debug(directory.directory);
			final FileAlterationObserver fileAlterationObserver = new FileAlterationObserver(directory.directory, mediaFileFilter);
			fileAlterationObserver.addListener(fileAlterationListener);
			fileAlterationObservers[i] = fileAlterationObserver;
			i++;
		}
		return fileAlterationObservers;
	}

	public void setStop(final boolean stop)
	{
		this.stop = stop;
		try {
			this.fileAlterationMonitor.stop();
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	public boolean isStop()
	{
		return this.stop;
	}

	public void setIntervall(final long intervall)
	{
		this.intervall = intervall;
	}

	private class MediaFileAlternationListener extends FileAlterationListenerAdaptor
	{
		@Override public void onFileChange(final File file)
		{
			this.addToUpload(DirectoryWorker.this.directoryService.findByFile(file), file);
		}

		@Override public void onFileCreate(final File file)
		{
			this.addToUpload(DirectoryWorker.this.directoryService.findByFile(file), file);
		}

		private void addToUpload(final Directory entry, final File file)
		{
			final Preset preset = entry.preset;

			final Queue queue = new Queue();
			final Playlist playlist = preset.playlist;

			if (preset.autotitle) {
				DirectoryWorker.this.autoTitleGenerator.setFileName(file.getName());
				DirectoryWorker.this.autoTitleGenerator.setFormatString(preset.autotitleFormat);
				DirectoryWorker.this.autoTitleGenerator.setPlaylist(preset.playlist);
				queue.title = DirectoryWorker.this.autoTitleGenerator.gernerate();
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
				DirectoryWorker.this.playlistService.updatePlaylist(playlist);
			}

			DirectoryWorker.this.queueService.createQueue(queue);
			EventBus.publish(Uploader.QUEUE_START, null);
		}
	}

	private static class MediaFileFilter extends FileFileFilter
	{

		@Override
		public boolean accept(final File file)
		{
			final String[] extensions = Mimetype.EXTENSIONS;
			final int dotPos = file.toString().lastIndexOf(".") + 1;
			final String fileExtension = file.toString().substring(dotPos);

			boolean flag = false;
			for (final String extension : extensions) {
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
				Thread.sleep(10000);
			} catch (InterruptedException ignored) {

			}
			return !(file.lastModified() != checkedAt || fileSizeAt != file.length());
		}
	}
}