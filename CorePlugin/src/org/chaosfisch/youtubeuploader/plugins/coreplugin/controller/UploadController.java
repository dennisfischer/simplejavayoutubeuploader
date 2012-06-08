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
	private final GenericListModel<Account>  accountListModel  = new GenericListModel<Account>()
	{
		private static final long serialVersionUID = 7368872570202779563L;

		@EventTopicSubscriber(topic = AccountService.ACCOUNT_ADDED)
		public void onAccountAdded(final String topic, final Account element)
		{
			this.addElement(element);
		}

		@EventTopicSubscriber(topic = AccountService.ACCOUNT_REMOVED)
		public void onAccountRemoved(final String topic, final Account element)
		{
			this.removeElement(element);
		}

		@EventTopicSubscriber(topic = AccountService.ACCOUNT_UPDATED)
		public void onAccountUpdated(final String topic, final Account element)
		{
			final int index = this.getIndexOf(element);
			if (index != -1) {
				this.removeElementAt(index);
				this.insertElementAt(element, index);
			}
		}
	};
	private final GenericListModel<Preset>   presetListModel   = new GenericListModel<Preset>()
	{

		private static final long serialVersionUID = 5531366617857937110L;

		@EventTopicSubscriber(topic = PresetService.PRESET_ENTRY_ADDED)
		public void onPresetAdded(final String topic, final Preset preset)
		{
			this.addElement(preset);
		}

		@EventTopicSubscriber(topic = PresetService.PRESET_ENTRY_UPDATED)
		public void onPresetUpdated(final String topic, final Preset preset)
		{
			final int index = this.getIndexOf(preset);
			if (index != -1) {
				this.removeElementAt(index);
				this.insertElementAt(preset, index);
			}
		}

		@EventTopicSubscriber(topic = PresetService.PRESET_ENTRY_REMOVED)
		public void onPresetRemoved(final String topic, final Preset preset)
		{
			this.removeElement(preset);
		}
	};
	private final GenericListModel<Playlist> playlistListModel = new GenericListModel<Playlist>()
	{
		private static final long serialVersionUID = 8997201386145568022L;

		@EventTopicSubscriber(topic = PlaylistService.PLAYLIST_ENTRY_ADDED)
		public void onPlaylistAdded(final String topic, final Playlist playlist)
		{
			this.addElement(playlist);
		}

		@EventTopicSubscriber(topic = PlaylistService.PLAYLIST_ENTRY_REMOVED)
		public void onPlaylistRemoved(final String topic, final Playlist playlist)
		{
			this.removeElement(playlist);
		}

		@EventTopicSubscriber(topic = PlaylistService.PLAYLIST_ENTRY_UPDATED)
		public void onPlaylistUpdated(final String topic, final Playlist playlist)
		{
			final int index = this.getIndexOf(playlist);
			if (index != -1) {
				this.removeElementAt(index);
				this.insertElementAt(playlist, index);
			}
		}
	};
	private boolean autotitle;

	@Inject
	public UploadController(final AccountService accountService, final PresetService presetService, final QueueService queueService, final AutoTitleGenerator autoTitleGenerator, final PlaylistService playlistService, final JFileChooser fileChooser)
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
		this.accountService.delete(account);
	}

	public void deletePreset(final Preset preset)
	{

		this.presetListModel.removeElement(preset);
		this.presetService.delete(preset);
	}

	public void savePreset(final Preset preset)
	{
		this.presetService.update(preset);
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
			EventBus.publish(AutoTitleGenerator.AUTOTITLE_CHANGED, this.autoTitleGenerator.gernerate()); //NON-NLS
		}
	}

	public GenericListModel<Account> getAccountListModel()
	{
		return this.accountListModel;
	}

	public AccountService getAccountService()
	{
		return this.accountService;
	}

	public void submitUpload(final Account account, final boolean rate, final String category, final short comment, final String description, final boolean embed, final String file, final boolean commentvote, final boolean mobile,
							 final Playlist playlist, final String tags, final String title, final short videoresponse, final short visibility, final Date starttime)
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
			this.playlistService.update(playlist);
		}

		switch (visibility) {
			case 1:
				queueEntity.unlisted = true;
				break;
			case 2:
				queueEntity.privatefile = true;
				break;
		}

		if (starttime.after(new Date(System.currentTimeMillis() + (300000)))) {
			queueEntity.started = new Date(starttime.getTime());
		}

		this.queueService.create(queueEntity);
	}

	public GenericListModel<Preset> getPresetListModel()
	{
		return this.presetListModel;
	}

	public ComboBoxModel<Playlist> getPlaylistListModel()
	{
		return this.playlistListModel;
	}

	public void synchronizePlaylists(final List<Account> accounts)
	{
		this.playlistService.synchronizePlaylists(accounts);
	}

	@EventTopicSubscriber(topic = "playlistsSynchronized", referenceStrength = ReferenceStrength.STRONG)
	public void onPlaylistSynchronize(final String topic, final Object object)
	{
		this.changeAccount((Account) this.accountListModel.getSelectedItem());
	}

	public void changeAccount(final Account account)
	{
		this.playlistListModel.removeAllElements();
		for (final Playlist playlist : this.playlistService.getByAccount(account)) {
			this.playlistListModel.addElement(playlist);
		}
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
