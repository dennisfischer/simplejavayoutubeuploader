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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.controller;

import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Account;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Preset;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Queue;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.AccountService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PresetService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.QueueService;

import javax.swing.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 24.01.12
 * Time: 22:20
 * To change this template use File | Settings | File Templates.
 */
class ImportManager
{
	private final XStream        xStream;
	private final AccountService accountService;
	private final PresetService  presetService;
	private final QueueService   queueService;
	private final JFileChooser   fileChooser;

	@SuppressWarnings("DuplicateStringLiteralInspection") @Inject
	public ImportManager(final XStream xStream, final AccountService accountService, final PresetService presetService, final QueueService queueService, final JFileChooser fileChooser)
	{
		this.xStream = xStream;
		this.accountService = accountService;
		this.presetService = presetService;
		this.queueService = queueService;
		this.fileChooser = fileChooser;
		xStream.alias("entries", List.class); //NON-NLS
		xStream.alias("org.chaosfisch.youtubeuploader.db.PresetEntry", Preset.class); //NON-NLS
		xStream.alias("org.chaosfisch.youtubeuploader.db.AccountEntry", Account.class); //NON-NLS
		xStream.alias("org.chaosfisch.youtubeuploader.db.QueueEntry", Queue.class); //NON-NLS
	}

	public void importAccount()
	{
		final File file = showFileOpenDialog();
		if (file == null) {
			return;
		}
		final Object object = readObjectFromXMLFile(file, "UTF-8"); //NON-NLS
		if (object instanceof List<?>) {
			final Iterable<?> accounts = (Iterable<?>) object;
			for (final Object account : accounts) {
				final Account accountEntry = (Account) account;
				//accountEntry.getYoutubeServiceManager().authenticate();
				accountService.create(accountEntry);
			}
		}
	}

	public void importPreset()
	{
		final File file = showFileOpenDialog();
		if (file == null) {
			return;
		}
		final Object object = readObjectFromXMLFile(file, "UTF-8"); //NON-NLS
		if (object instanceof List<?>) {
			final Iterable<?> presets = (Iterable<?>) object;
			for (final Object preset : presets) {
				presetService.create((Preset) preset);
			}
		}
	}

	public void importQueue()
	{
		final File file = showFileOpenDialog();
		if (file == null) {
			return;
		}
		final Object object = readObjectFromXMLFile(file, "UTF-8"); //NON-NLS
		if (object instanceof List<?>) {
			final Iterable<?> entries = (Iterable<?>) object;
			for (final Object entry : entries) {
				queueService.create((Queue) entry);
			}
		}
	}

	private Object readObjectFromXMLFile(final File file, final String charset)
	{
		try {
			final FileInputStream fileInputStream = new FileInputStream(file);
			final BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
			final InputStreamReader inputStreamReader = new InputStreamReader(bufferedInputStream, Charset.forName(charset));
			try {
				return xStream.fromXML(inputStreamReader);
			} finally {
				try {
					inputStreamReader.close();
					bufferedInputStream.close();
					fileInputStream.close();
				} catch (IOException ignored) {
					throw new RuntimeException("This shouldn't happen");
				}
			}
		} catch (FileNotFoundException ignored) {
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

		if (result == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile();
		}
		return null;
	}
}
