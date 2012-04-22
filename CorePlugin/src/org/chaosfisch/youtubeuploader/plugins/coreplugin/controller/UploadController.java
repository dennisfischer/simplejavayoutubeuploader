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

/*
 * DefaultController.java
 *
 * Created on January 22, 2007, 8:42 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.chaosfisch.youtubeuploader.plugins.coreplugin.controller;

import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.bushe.swing.event.annotation.ReferenceStrength;
import org.chaosfisch.util.Mimetype;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.AccountListModel;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.PlaylistListModel;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.PresetListModel;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.entities.AccountEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.entities.PlaylistEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.entities.PresetEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.entities.QueueEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.AccountService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PlaylistService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PresetService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.QueueService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.spi.AutoTitleGenerator;

import javax.swing.*;
import java.util.Collection;
import java.util.Date;

public class UploadController
{

	private final AccountService     accountService;
	private final QueueService       queueService;
	private final PresetService      presetService;
	private final PlaylistService    playlistService;
	private final AutoTitleGenerator autoTitleGenerator;
	private final ImportManager      importManager;
	private final ExportManager      exportManager;
	private final AccountListModel  accountListModel  = new AccountListModel();
	private final PresetListModel   presetListModel   = new PresetListModel();
	private final PlaylistListModel playlistListModel = new PlaylistListModel();
	private       boolean           autotitle         = false;

	@Inject
	public UploadController(final AccountService accountService, final PresetService presetService, final QueueService queueService, final AutoTitleGenerator autoTitleGenerator,
	                        final PlaylistService playlistService, final JFileChooser fileChooser)
	{
		this.accountService = accountService;
		this.presetService = presetService;
		this.queueService = queueService;
		this.autoTitleGenerator = autoTitleGenerator;
		this.playlistService = playlistService;

		final XStream xStream = new XStream(new DomDriver());
		this.importManager = new ImportManager(xStream, accountService, presetService, queueService, fileChooser);
		this.exportManager = new ExportManager(xStream, accountService, presetService, queueService, fileChooser);
		AnnotationProcessor.process(this);
	}

	public void deleteAccount(final AccountEntry accountEntry)
	{
		this.accountService.deleteAccountEntry(accountEntry);
	}

	public void deletePreset(final PresetEntry presetEntry)
	{

		this.presetListModel.removePresetEntry(presetEntry);
		this.presetService.deletePresetEntry(presetEntry);
	}

	public void savePreset(final PresetEntry presetEntry)
	{
		this.presetService.updatePresetEntry(presetEntry);
	}

	public void changeAutotitleCheckbox(final boolean selected)
	{
		this.autotitle = selected;
		this.updateAutotitle();
	}

	public void changeAutotitleFile(final Object item)
	{
		this.autoTitleGenerator.setFileName(item.toString());
		this.updateAutotitle();
	}

	public void changeAutotitlePlaylist(final Object item)
	{
		this.autoTitleGenerator.setPlaylist((PlaylistEntry) item);
		this.updateAutotitle();
	}

	public void changeAutotitleFormat(final String text)
	{
		this.autoTitleGenerator.setFormatString(text);
		this.updateAutotitle();
	}

	public void changeAutotitleNumber(final Object value)
	{
		this.autoTitleGenerator.setNumber(Integer.parseInt(value.toString()));
		this.updateAutotitle();
	}

	private void updateAutotitle()
	{
		if (this.autotitle) {
			//noinspection DuplicateStringLiteralInspection
			EventBus.publish("autoTitleChanged", this.autoTitleGenerator.gernerate()); //NON-NLS
		}
	}

	public AccountListModel getAccountListModel()
	{
		return this.accountListModel;
	}

	public AccountService getAccountService()
	{
		return this.accountService;
	}

	public void submitUpload(final AccountEntry accountEntry, final boolean rate, final String category, final short comment, final String description, final boolean embed, final String file,
	                         final boolean commentvote, final boolean mobile, final PlaylistEntry playlistEntry, final String tags, final String title, final short videoresponse,
	                         final short visibility, final Date starttime)
	{
		final QueueEntry queueEntity = new QueueEntry();
		queueEntity.setAccount(accountEntry);
		queueEntity.setMimetype(Mimetype.getMimetypeByExtension(file));
		queueEntity.setMobile(mobile);
		queueEntity.setTitle(title);
		queueEntity.setCategory(category);
		queueEntity.setComment(comment);
		queueEntity.setCommentvote(commentvote);
		queueEntity.setDescription(description);
		queueEntity.setEmbed(embed);
		queueEntity.setFile(file);
		queueEntity.setKeywords(tags);
		queueEntity.setRate(rate);
		queueEntity.setVideoresponse(videoresponse);
		queueEntity.setPlaylist(playlistEntry);
		queueEntity.setLocked(false);

		if (playlistEntry != null) {
			playlistEntry.setNumber(playlistEntry.getNumber() + 1);
			this.playlistService.updatePlaylist(playlistEntry);
		}

		switch (visibility) {
			case 1:
				queueEntity.setUnlisted(true);
				break;
			case 2:
				queueEntity.setPrivatefile(true);
				break;
		}

		if (starttime.after(new Date(System.currentTimeMillis() + 60 * 60 * 1000))) {
			queueEntity.setStarted(starttime);
		}

		this.queueService.createQueueEntry(queueEntity);
	}

	public PresetListModel getPresetListModel()
	{
		return this.presetListModel;
	}

	public PlaylistListModel getPlaylistListModel()
	{
		return this.playlistListModel;
	}

	public void synchronizePlaylists(final Collection<AccountEntry> accountEntries)
	{
		this.playlistService.synchronizePlaylists(accountEntries);
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = "playlistsSynchronized", referenceStrength = ReferenceStrength.STRONG)
	public void onPlaylistSynchronize(final String topic, final Object object)
	{
		this.changeAccount(this.accountListModel.getSelectedItem());
	}

	public void changeAccount(final AccountEntry accountEntry)
	{
		this.playlistListModel.removeAll();
		this.playlistListModel.addPlaylistEntryList(this.playlistService.getAllPlaylistByAccount(accountEntry));
	}

	public void importAccount()
	{
		this.importManager.importAccount();
	}

	public void importPreset()
	{
		this.importManager.importPreset();
	}

	public void importQueue()
	{
		this.importManager.importQueue();
	}

	public void exportAccount()
	{
		this.exportManager.exportAccount();
	}

	public void exportPreset()
	{
		this.exportManager.exportPreset();
	}

	public void exportQueue()
	{
		this.exportManager.exportQueue();
	}

	public PresetService getPresetService()
	{
		return this.presetService;
	}
}
