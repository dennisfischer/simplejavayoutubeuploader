/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

import javax.swing.JFileChooser;

import org.chaosfisch.youtubeuploader.dao.spi.AccountDao;
import org.chaosfisch.youtubeuploader.dao.spi.PresetDao;
import org.chaosfisch.youtubeuploader.dao.spi.QueueDao;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Preset;
import org.chaosfisch.youtubeuploader.models.Queue;

import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;

/**
 * Created by IntelliJ IDEA. User: Dennis Date: 24.01.12 Time: 22:20 To change
 * this template use File | Settings | File Templates.
 */
class ImportManager
{
	private final AccountDao	accountService;
	private final JFileChooser	fileChooser;
	private final PresetDao		presetService;
	private final QueueDao		queueService;
	private final XStream		xStream;

	@Inject
	public ImportManager(final XStream xStream, final AccountDao accountService, final PresetDao presetService, final QueueDao queueService,
			final JFileChooser fileChooser)
	{
		this.xStream = xStream;
		this.accountService = accountService;
		this.presetService = presetService;
		this.queueService = queueService;
		this.fileChooser = fileChooser;
		xStream.alias("entries", List.class); // NON-NLS
		xStream.alias("org.chaosfisch.youtubeuploader.db.PresetEntry", Preset.class); // NON-NLS
		xStream.alias("org.chaosfisch.youtubeuploader.db.AccountEntry", Account.class); // NON-NLS
		xStream.alias("org.chaosfisch.youtubeuploader.db.QueueEntry", Queue.class); // NON-NLS
	}

	public void importAccount()
	{
		final File file = showFileOpenDialog();
		if (file == null) { return; }
		final Object object = readObjectFromXMLFile(file, "UTF-8"); // NON-NLS
		if (object instanceof List<?>)
		{
			final Iterable<?> accounts = (Iterable<?>) object;
			for (final Object account : accounts)
			{
				final Account accountEntry = (Account) account;
				// accountEntry.getYoutubeServiceManager().authenticate();
				accountService.create(accountEntry);
			}
		}
	}

	public void importPreset()
	{
		final File file = showFileOpenDialog();
		if (file == null) { return; }
		final Object object = readObjectFromXMLFile(file, "UTF-8"); // NON-NLS
		if (object instanceof List<?>)
		{
			final Iterable<?> presets = (Iterable<?>) object;
			for (final Object preset : presets)
			{
				presetService.create((Preset) preset);
			}
		}
	}

	public void importQueue()
	{
		final File file = showFileOpenDialog();
		if (file == null) { return; }
		final Iterable<Queue> queues = (Iterable<Queue>) readObjectFromXMLFile(file, "UTF-8"); // NON-NLS
		for (final Queue queue : queues)
		{
			if (queue.account != null)
			{
				final Account findObject = new Account();
				findObject.name = queue.account.name;
				queue.account = accountService.find(findObject);
			}
			final File queueFileTest = new File(queue.file);
			if ((queue.account == null) || !queueFileTest.exists())
			{
				queue.locked = true;
			}
			queueService.create(queue);
		}
	}

	private Object readObjectFromXMLFile(final File file, final String charset)
	{
		try
		{
			final FileInputStream fileInputStream = new FileInputStream(file);
			final BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
			final InputStreamReader inputStreamReader = new InputStreamReader(bufferedInputStream, Charset.forName(charset));
			try
			{
				return xStream.fromXML(inputStreamReader);
			} finally
			{
				try
				{
					inputStreamReader.close();
					bufferedInputStream.close();
					fileInputStream.close();
				} catch (final IOException ignored)
				{
					throw new RuntimeException("This shouldn't happen");
				}
			}
		} catch (final FileNotFoundException ignored)
		{
			return new Object();
		}
	}

	private File showFileOpenDialog()
	{
		fileChooser.setAcceptAllFileFilterUsed(true);
		fileChooser.setDragEnabled(true);
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		final int result = fileChooser.showOpenDialog(null);

		if (result == JFileChooser.APPROVE_OPTION) { return fileChooser.getSelectedFile(); }
		return null;
	}
}