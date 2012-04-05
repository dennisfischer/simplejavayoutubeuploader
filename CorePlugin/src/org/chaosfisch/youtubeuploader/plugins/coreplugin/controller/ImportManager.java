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

import com.google.gdata.util.AuthenticationException;
import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.entities.AccountEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.entities.PresetEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.entities.QueueEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.olddb.OldAccountEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.olddb.OldPresetEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.AccountService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PresetService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.QueueService;

import javax.swing.*;
import java.io.*;
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
	}

	public void importAccount()
	{
		final File file = this.showFileOpenDialog();
		if (file == null) {
			return;
		}
		InputStreamReader inputStreamReader = null;
		try {
			inputStreamReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (FileNotFoundException e) {
			return;
		}
		@SuppressWarnings("unchecked") final List accounts = (List) this.xStream.fromXML(inputStreamReader);

		for (final Object account : accounts) {
			final AccountEntry accountEntry = (AccountEntry) account;
			try {
				accountEntry.getYoutubeServiceManager().authenticate();
				this.accountService.createAccountEntry((AccountEntry) account);
			} catch (AuthenticationException ignored) {
			}
		}
	}

	public void importPreset()
	{
		final File file = this.showFileOpenDialog();
		if (file == null) {
			return;
		}
		InputStreamReader inputStreamReader = null;
		try {
			inputStreamReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (FileNotFoundException e) {
			return;
		}
		@SuppressWarnings("unchecked") final List presets = (List) this.xStream.fromXML(inputStreamReader);

		for (final Object preset : presets) {
			this.presetService.createPresetEntry((PresetEntry) preset);
		}
	}

	public void importQueue()
	{
		final File file = this.showFileOpenDialog();
		if (file == null) {
			return;
		}
		InputStreamReader inputStreamReader = null;
		try {
			inputStreamReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (FileNotFoundException e) {
			return;
		}
		@SuppressWarnings("unchecked") final List entries = (List) this.xStream.fromXML(inputStreamReader);
		for (final Object entry : entries) {
			this.queueService.createQueueEntry((QueueEntry) entry);
		}
	}

	public void importOldAccount()
	{
		final File file = this.showFileOpenDialog();
		if (file == null) {
			return;
		}
		InputStreamReader inputStreamReader = null;
		try {
			inputStreamReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (FileNotFoundException e) {
			return;
		}
		//noinspection DuplicateStringLiteralInspection
		this.xStream.alias("entry", OldAccountEntry.class); //NON-NLS
		@SuppressWarnings("unchecked") final List accounts = (List) this.xStream.fromXML(inputStreamReader);

		for (final Object account : accounts) {
			final OldAccountEntry oldAccountEntry = (OldAccountEntry) account;
			final AccountEntry accountEntity = new AccountEntry();

			accountEntity.setName(oldAccountEntry.getName());
			accountEntity.setPassword(oldAccountEntry.getPassword());
			accountEntity.setSecret(oldAccountEntry.getSecret());
			try {
				accountEntity.getYoutubeServiceManager().authenticate();
				this.accountService.createAccountEntry(accountEntity);
			} catch (AuthenticationException ignored) {
			}
		}
	}

	public void importOldPreset()
	{
		final File file = this.showFileOpenDialog();
		if (file == null) {
			return;
		}
		InputStreamReader inputStreamReader = null;
		try {
			inputStreamReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (FileNotFoundException e) {
			return;
		}
		//noinspection DuplicateStringLiteralInspection
		this.xStream.alias("entry", OldPresetEntry.class); //NON-NLS
		@SuppressWarnings("unchecked") final List presets = (List) this.xStream.fromXML(inputStreamReader);

		for (final Object preset : presets) {
			final OldPresetEntry oldPresetEntry = (OldPresetEntry) preset;
			final PresetEntry presetEntity = new PresetEntry();

			presetEntity.setName(oldPresetEntry.getName());
			presetEntity.setAutotitle(oldPresetEntry.isAutotitle());
			presetEntity.setAutotitleFormat(oldPresetEntry.getAutotitle_format());
			presetEntity.setCategory(oldPresetEntry.getCategory());
			presetEntity.setComment((short) oldPresetEntry.getComment());
			presetEntity.setCommentvote(oldPresetEntry.isCommentVote());
			presetEntity.setDefaultDir(oldPresetEntry.getDefault_dir());
			presetEntity.setDescription(oldPresetEntry.getDescription());
			presetEntity.setEmbed(oldPresetEntry.isEmbed());
			presetEntity.setKeywords(oldPresetEntry.getKeywords());
			presetEntity.setMobile(oldPresetEntry.isMobile());
			presetEntity.setNumberModifier((short) oldPresetEntry.getNumber_modifier());
			presetEntity.setRate(oldPresetEntry.isRate());
			presetEntity.setVideoresponse((short) oldPresetEntry.getVideoResponse());
			presetEntity.setVisibility((short) oldPresetEntry.getVisibility());

			this.presetService.createPresetEntry(presetEntity);
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
