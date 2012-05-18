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
		xStream.alias("org.chaosfisch.youtubeuploader.db.Preset", Preset.class); //NON-NLS
		xStream.alias("org.chaosfisch.youtubeuploader.db.Account", Account.class); //NON-NLS
		xStream.alias("org.chaosfisch.youtubeuploader.db.Queue", Queue.class); //NON-NLS
	}

	public void importAccount()
	{
		final File file = this.showFileOpenDialog();
		if (file == null) {
			return;
		}
		final Object object = this.readObjectFromXMLFile(file, "UTF-8"); //NON-NLS
		if (object instanceof List<?>) {
			final List<?> accounts = (List<?>) object;
			for (final Object account : accounts) {
				final Account accountEntry = (Account) account;
				//accountEntry.getYoutubeServiceManager().authenticate();
				this.accountService.createAccountEntry(accountEntry);
			}
		}
	}

	public void importPreset()
	{
		final File file = this.showFileOpenDialog();
		if (file == null) {
			return;
		}
		final Object object = this.readObjectFromXMLFile(file, "UTF-8"); //NON-NLS
		if (object instanceof List<?>) {
			final List<?> presets = (List<?>) object;
			for (final Object preset : presets) {
				this.presetService.createPresetEntry((Preset) preset);
			}
		}
	}

	public void importQueue()
	{
		final File file = this.showFileOpenDialog();
		if (file == null) {
			return;
		}
		final Object object = this.readObjectFromXMLFile(file, "UTF-8"); //NON-NLS
		if (object instanceof List<?>) {
			final List<?> entries = (List<?>) object;
			for (final Object entry : entries) {
				this.queueService.createQueue((Queue) entry);
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
				return this.xStream.fromXML(inputStreamReader);
			} finally {
				try {
					inputStreamReader.close();
					bufferedInputStream.close();
					fileInputStream.close();
				} catch (IOException ignored) {
				}
			}
		} catch (FileNotFoundException ex) {
			return new Object();
		}
	}

	private File showFileOpenDialog()
	{
		this.fileChooser.setAcceptAllFileFilterUsed(true);
		this.fileChooser.setDragEnabled(true);
		this.fileChooser.setMultiSelectionEnabled(true);
		this.fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		final int result = this.fileChooser.showOpenDialog(null);

		if (result == JFileChooser.APPROVE_OPTION) {
			return this.fileChooser.getSelectedFile();
		}
		return null;
	}
}
