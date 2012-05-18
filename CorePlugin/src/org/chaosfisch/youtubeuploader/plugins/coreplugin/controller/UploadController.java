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
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.*;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.AccountService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PlaylistService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PresetService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.QueueService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.spi.AutoTitleGenerator;

import javax.swing.*;
import java.util.Date;
import java.util.List;

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

	public void deleteAccount(final Account account)
	{
		this.accountService.deleteAccountEntry(account);
	}

	public void deletePreset(final Preset preset)
	{

		this.presetListModel.removePresetEntry(preset);
		this.presetService.deletePresetEntry(preset);
	}

	public void savePreset(final Preset preset)
	{
		this.presetService.updatePresetEntry(preset);
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
		this.autoTitleGenerator.setPlaylist((Playlist) item);
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

	public void submitUpload(final Account account, final boolean rate, final String category, final short comment, final String description, final boolean embed, final String file,
	                         final boolean commentvote, final boolean mobile, final Playlist playlist, final String tags, final String title, final short videoresponse, final short visibility,
	                         final Date starttime)
	{
		final Queue queueEntity = new Queue();
		queueEntity.account = account;
		queueEntity.mimetype = Mimetype.getMimetypeByExtension(file);
		queueEntity.mobile = mobile;
		queueEntity.title = title;
		queueEntity.category = category;
		queueEntity.comment = comment;
		queueEntity.commentvote = commentvote;
		queueEntity.description = description;
		queueEntity.embed = embed;
		queueEntity.file = file;
		queueEntity.keywords = tags;
		queueEntity.rate = rate;
		queueEntity.videoresponse = videoresponse;
		queueEntity.playlist = playlist;
		queueEntity.locked = false;

		if (playlist != null) {
			playlist.number++;
			this.playlistService.updatePlaylist(playlist);
		}

		switch (visibility) {
			case 1:
				queueEntity.unlisted = true;
				break;
			case 2:
				queueEntity.privatefile = true;
				break;
		}

		if (starttime.after(new Date(System.currentTimeMillis() + 60 * 60 * 1000))) {
			queueEntity.started = starttime;
		}

		this.queueService.createQueue(queueEntity);
	}

	public PresetListModel getPresetListModel()
	{
		return this.presetListModel;
	}

	public PlaylistListModel getPlaylistListModel()
	{
		return this.playlistListModel;
	}

	public void synchronizePlaylists(final List<Account> accounts)
	{
		this.playlistService.synchronizePlaylists(accounts);
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = "playlistsSynchronized", referenceStrength = ReferenceStrength.STRONG)
	public void onPlaylistSynchronize(final String topic, final Object object)
	{
		this.changeAccount(this.accountListModel.getSelectedItem());
	}

	public void changeAccount(final Account account)
	{
		this.playlistListModel.removeAll();
		this.playlistListModel.addPlaylistEntryList(this.playlistService.getByAccount(account));
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
