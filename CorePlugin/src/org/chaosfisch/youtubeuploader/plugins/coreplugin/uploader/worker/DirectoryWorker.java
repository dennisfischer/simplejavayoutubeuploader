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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.worker;

import com.google.inject.Inject;
import org.chaosfisch.util.BetterSwingWorker;
import org.chaosfisch.youtubeuploader.db.DirectoryEntry;
import org.chaosfisch.youtubeuploader.db.PresetEntry;
import org.chaosfisch.youtubeuploader.db.QueueEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.Mimetype;
import org.chaosfisch.youtubeuploader.services.DirectoryService;

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

	private       long             lastCheck;
	private final long             intervall;
	private final DirectoryService directoryService;
	private       boolean          stop;

	@Inject
	public DirectoryWorker(final DirectoryService directoryService, final long intervall)
	{
		this.intervall = intervall;
		this.directoryService = directoryService;
		this.lastCheck = System.currentTimeMillis();
	}

	@Override
	protected void background()
	{
		while (!this.stop) {
			try {
				Thread.sleep(this.intervall);
			} catch (InterruptedException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
			this.checkForChanges();
			this.lastCheck = System.currentTimeMillis();
		}
	}

	@Override
	protected void onDone()
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	private void checkForChanges()
	{
		final List directories = this.directoryService.getAllActive();
		for (final Object object : directories) {
			final DirectoryEntry directoryEntry = (DirectoryEntry) object;
			final File[] files = this.checkDirectory(directoryEntry);
			if (files != null) {
				for (final File file : files) {
					this.addToUpload(directoryEntry, file);
				}
			}
		}
	}

	private void addToUpload(final DirectoryEntry entry, final File file)
	{
		final PresetEntry presetEntry = entry.getPreset();

		final QueueEntry queueEntry = new QueueEntry();

		/* AUTOTITLE MISSING !!!! */
		queueEntry.setTitle(file.getName());
		queueEntry.setFile(file.getAbsolutePath());

		queueEntry.setAccount(entry.getAccount());
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
	}

	private File[] checkDirectory(final DirectoryEntry entry)
	{
		File[] listFiles = null;
		final File directory = new File(entry.getDirectory());
		if (!directory.exists() || !directory.isDirectory()) {
			return listFiles;
		}
		final FileFilter filter = new FileFilter()
		{

			@Override
			public boolean accept(final File file)
			{
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
				if (file.lastModified() > DirectoryWorker.this.lastCheck) {
					final long checkedAt = file.lastModified();
					final long fileSizeAt = file.length();
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					}
					return !(file.lastModified() != checkedAt || fileSizeAt != file.length());
				}
				return false;
			}
		};
		listFiles = directory.listFiles(filter);

		return listFiles;
	}

	public void setStop(final boolean stop)
	{
		this.stop = stop;
	}

	public boolean isStop()
	{
		return this.stop;
	}
}