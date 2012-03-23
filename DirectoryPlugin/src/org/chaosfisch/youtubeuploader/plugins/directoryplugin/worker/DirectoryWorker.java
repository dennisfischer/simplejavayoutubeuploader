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
import org.chaosfisch.util.BetterSwingWorker;
import org.chaosfisch.util.Mimetype;
import org.chaosfisch.youtubeuploader.db.PresetEntry;
import org.chaosfisch.youtubeuploader.db.QueueEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.AutoTitleGenerator;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.db.DirectoryEntry;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.services.DirectoryService;
import org.chaosfisch.youtubeuploader.services.QueueService;

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
public class DirectoryWorker extends BetterSwingWorker
{
	private         boolean               stop;
	private final   long                  intervall;
	private         FileAlterationMonitor fileAlterationMonitor;
	@Inject private QueueService          queueService;
	@Inject private DirectoryService      directoryService;
	@Inject private AutoTitleGenerator    autoTitleGenerator;

	public DirectoryWorker(final long intervall)
	{
		this.intervall = intervall;
	}

	@Override
	protected void background()
	{

		final FileAlterationObserver[] fileAlterationObservers = this.initObservers();
		this.fileAlterationMonitor = new FileAlterationMonitor(this.intervall, fileAlterationObservers);

		try {
			this.fileAlterationMonitor.start();
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		while (!this.stop) {
			try {
				Thread.sleep(this.intervall);
			} catch (InterruptedException ignored) {
			}
		}
	}

	private FileAlterationObserver[] initObservers()
	{
		final List directories = this.directoryService.getAllActive();
		final FileAlterationObserver[] fileAlterationObservers = new FileAlterationObserver[20];
		final FileFilter mediaFileFilter = new MediaFileFilter();
		final FileAlterationListener fileAlterationListener = new MediaFileAlternationListener();
		for (final Object object : directories) {
			final FileAlterationObserver fileAlterationObserver = new FileAlterationObserver(((DirectoryEntry) object).getDirectory(), mediaFileFilter);
			fileAlterationObserver.addListener(fileAlterationListener);
			fileAlterationObservers[fileAlterationObservers.length + 1] = fileAlterationObserver;
		}
		return fileAlterationObservers;
	}

	@Override
	protected void onDone()
	{
		try {
			this.fileAlterationMonitor.stop();
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	public void setStop(final boolean stop)
	{
		this.stop = stop;
	}

	public boolean isStop()
	{
		return this.stop;
	}

	private class MediaFileAlternationListener extends FileAlterationListenerAdaptor
	{
		@Override public void onFileChange(final File file)
		{
			this.addToUpload(DirectoryWorker.this.directoryService.getByFileDirectory(file), file);
		}

		private void addToUpload(final DirectoryEntry entry, final File file)
		{
			final PresetEntry presetEntry = entry.getPreset();

			final QueueEntry queueEntry = new QueueEntry();

			if (presetEntry.isAutotitle()) {
				DirectoryWorker.this.autoTitleGenerator.setFileName(file.getName());
				DirectoryWorker.this.autoTitleGenerator.setFormatString(presetEntry.getAutotitleFormat());
				DirectoryWorker.this.autoTitleGenerator.setPlaylist(presetEntry.getPlaylist());
				queueEntry.setTitle(DirectoryWorker.this.autoTitleGenerator.gernerate());
			} else {
				queueEntry.setTitle(file.getName());
			}
			queueEntry.setFile(file.getAbsolutePath());

			queueEntry.setAccount(presetEntry.getAccount());
			queueEntry.setCategory(presetEntry.getCategory());
			queueEntry.setDescription(presetEntry.getDescription());
			queueEntry.setKeywords(presetEntry.getKeywords());
			queueEntry.setComment(presetEntry.getComment());
			queueEntry.setCommentvote(presetEntry.isCommentvote());
			queueEntry.setEmbed(presetEntry.isEmbed());
			queueEntry.setMobile(presetEntry.isMobile());
			queueEntry.setRate(presetEntry.isRate());
			queueEntry.setVideoresponse(presetEntry.getVideoresponse());

			switch (presetEntry.getVisibility()) {
				case 1:
					queueEntry.setUnlisted(true);
					break;
				case 2:
					queueEntry.setPrivatefile(true);
					break;
			}

			final int dotPos = file.toString().lastIndexOf(".") + 1;
			final String extension = file.toString().substring(dotPos);
			queueEntry.setMimetype(Mimetype.getMimetypeByExtension(extension));
			DirectoryWorker.this.queueService.createQueueEntry(queueEntry);
		}
	}

	private static class MediaFileFilter extends FileFileFilter

	{
		final FileFilter filter = new FileFilter()
		{

			public long lastCheck;

			@Override
			public boolean accept(final File file)
			{
				final String[] extensions = Mimetype.EXTENSIONS;
				final int dotPos = file.toString().lastIndexOf(".") + 1;
				final String fileExtension = file.toString().substring(dotPos);

				this.lastCheck = System.currentTimeMillis();

				boolean flag = false;
				for (final String extension : extensions) {
					if (extension.equals(fileExtension)) {
						flag = true;
					}
				}
				if (!flag) {
					return false;
				}
				if (file.lastModified() > this.lastCheck) {
					final long checkedAt = file.lastModified();
					final long fileSizeAt = file.length();
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return !(file.lastModified() != checkedAt || fileSizeAt != file.length());
				}
				return false;
			}
		};
	}
}