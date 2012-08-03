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
import org.jetbrains.annotations.Nullable;

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
			addElement(element);
		}

		@EventTopicSubscriber(topic = AccountService.ACCOUNT_REMOVED)
		public void onAccountRemoved(final String topic, final Account element)
		{
			removeElement(element);
		}

		@EventTopicSubscriber(topic = AccountService.ACCOUNT_UPDATED)
		public void onAccountUpdated(final String topic, final Account element)
		{
			final int index = getIndexOf(element);
			if (index != -1) {
				replaceRow(index, element);
			}
		}
	};
	private final GenericListModel<Preset>   presetListModel   = new GenericListModel<Preset>()
	{

		private static final long serialVersionUID = 5531366617857937110L;

		@EventTopicSubscriber(topic = PresetService.PRESET_ENTRY_ADDED)
		public void onPresetAdded(final String topic, final Preset preset)
		{
			addElement(preset);
		}

		@EventTopicSubscriber(topic = PresetService.PRESET_ENTRY_UPDATED)
		public void onPresetUpdated(final String topic, final Preset preset)
		{
			final int index = getIndexOf(preset);
			if (index != -1) {
				replaceRow(index, preset);
			}
		}

		@EventTopicSubscriber(topic = PresetService.PRESET_ENTRY_REMOVED)
		public void onPresetRemoved(final String topic, final Preset preset)
		{
			removeElement(preset);
		}
	};
	private final GenericListModel<Playlist> playlistListModel = new GenericListModel<Playlist>()
	{
		private static final long serialVersionUID = 8997201386145568022L;

		@EventTopicSubscriber(topic = PlaylistService.PLAYLIST_ENTRY_ADDED)
		public void onPlaylistAdded(final String topic, final Playlist playlist)
		{
			addElement(playlist);
		}

		@EventTopicSubscriber(topic = PlaylistService.PLAYLIST_ENTRY_REMOVED)
		public void onPlaylistRemoved(final String topic, final Playlist playlist)
		{
			removeElement(playlist);
		}

		@EventTopicSubscriber(topic = PlaylistService.PLAYLIST_ENTRY_UPDATED)
		public void onPlaylistUpdated(final String topic, final Playlist playlist)
		{
			final int index = getIndexOf(playlist);
			if (index != -1) {
				replaceRow(index, playlist);
			}
		}
	};
	private boolean autotitle;
	private final PlaceholderTableModel placeholderModel = new PlaceholderTableModel();

	@Inject
	public UploadController(final AccountService accountService,
	                        final PresetService presetService,
	                        final QueueService queueService,
	                        final AutoTitleGenerator autoTitleGenerator,
	                        final PlaylistService playlistService,
	                        final JFileChooser fileChooser)
	{
		this.accountService = accountService;
		this.presetService = presetService;
		this.queueService = queueService;
		this.autoTitleGenerator = autoTitleGenerator;
		this.playlistService = playlistService;

		final XStream xStream = new XStream(new DomDriver());
		importManager = new ImportManager(xStream, accountService, presetService, queueService, fileChooser);
		exportManager = new ExportManager(xStream, accountService, presetService, queueService, fileChooser);
		AnnotationProcessor.process(this);
	}

	public void deleteAccount(final Account account)
	{
		accountService.delete(account);
	}

	public void deletePreset(final Preset preset)
	{

		presetListModel.removeElement(preset);
		presetService.delete(preset);
	}

	public void savePreset(final Preset preset)
	{
		presetService.update(preset);
	}

	public void changeAutotitleCheckbox(final boolean selected)
	{
		autotitle = selected;
		updateAutotitle();
	}

	public void changeAutotitleFile(final Object item)
	{
		autoTitleGenerator.setFileName(item.toString());
		updateAutotitle();
	}

	public void changeAutotitlePlaylist(final Object item)
	{
		autoTitleGenerator.setPlaylist((Playlist) item);
		updateAutotitle();
	}

	public void changeAutotitleFormat(final String text)
	{
		autoTitleGenerator.setFormatString(text);
		updateAutotitle();
	}

	public void changeAutotitleNumber(final Object value)
	{
		autoTitleGenerator.setNumber(Integer.parseInt(value.toString()));
		updateAutotitle();
	}

	private void updateAutotitle()
	{
		if (autotitle) {
			//noinspection DuplicateStringLiteralInspection
			EventBus.publish(AutoTitleGenerator.AUTOTITLE_CHANGED, autoTitleGenerator.gernerate()); //NON-NLS
		}
	}

	public GenericListModel<Account> getAccountListModel()
	{
		return accountListModel;
	}

	@SuppressWarnings("TypeMayBeWeakened") public AccountService getAccountService()
	{
		return accountService;
	}

	public void submitUpload(final String filepath, final Account account, final String category)
	{
		submitUpload(filepath, account, category, (short) 0, new String(filepath.substring(0, filepath.lastIndexOf("."))), filepath, filepath, null, (short) 0, (short) 0, true, true, true, true, null,
		             null, null, false, false, false, false, (short) 0);
	}

	public void submitUpload(final String filepath, final Account account, final String category, final short visibility, final String title, final String description, final String tags)
	{
		submitUpload(filepath, account, category, visibility, title, description, tags, null, (short) 0, (short) 0, true, true, true, true, null, null, null, false, false, false, false, (short) 0);
	}

	public void submitUpload(final String filepath,
	                         final Account account,
	                         final String category,
	                         final short visibility,
	                         final String title,
	                         final String description,
	                         final String tags,
	                         @Nullable final Playlist playlist)
	{
		submitUpload(filepath, account, category, visibility, title, description, tags, playlist, (short) 0, (short) 0, true, true, true, true, null, null, null, false, false, false, false,
		             (short) 0);
	}

	public void submitUpload(final String filepath,
	                         final Account account,
	                         final String category,
	                         final short visibility,
	                         final String title,
	                         final String description,
	                         final String tags,
	                         @Nullable final Playlist playlist,
	                         final short comment,
	                         final short videoresponse,
	                         final boolean rate,
	                         final boolean embed,
	                         final boolean commentvote,
	                         final boolean mobile)
	{
		submitUpload(filepath, account, category, visibility, title, description, tags, playlist, comment, videoresponse, rate, embed, commentvote, mobile, null, null, null, false, false, false,
		             false, (short) 0);
	}

	public void submitUpload(final String filepath,
	                         final Account account,
	                         final String category,
	                         final short visibility,
	                         final String title,
	                         final String description,
	                         final String tags,
	                         @Nullable final Playlist playlist,
	                         final short comment,
	                         final short videoresponse,
	                         final boolean rate,
	                         final boolean embed,
	                         final boolean commentvote,
	                         final boolean mobile,
	                         @Nullable final Date starttime,
	                         @Nullable final Date releasetime,
	                         @Nullable final String enddir,
	                         final boolean monetize,
	                         final boolean monetizeOverlay,
	                         final boolean monetizeTrueview,
	                         final boolean monetizeProduct,
	                         final short license)
	{
		final Queue queue = new Queue();
		queue.account = account;
		queue.mimetype = Mimetype.getMimetypeByExtension(filepath);
		queue.mobile = mobile;
		queue.title = title;
		queue.category = category;
		queue.comment = comment;
		queue.commentvote = commentvote;
		queue.description = description;
		queue.embed = embed;
		queue.file = filepath;
		queue.keywords = tags;
		queue.rate = rate;
		queue.videoresponse = videoresponse;
		queue.playlist = playlist;
		queue.locked = false;
		queue.monetize = monetize;
		queue.monetizeOverlay = monetizeOverlay;
		queue.monetizeTrueview = monetizeTrueview;
		queue.monetizeProduct = monetizeProduct;
		queue.enddir = enddir;
		queue.license = license;

		if (playlist != null) {
			playlist.number++;
			playlistService.update(playlist);
		}

		switch (visibility) {
			case 1:
				queue.unlisted = true;
				break;
			case 2:
				queue.privatefile = true;
				break;
		}

		if ((starttime != null) && starttime.after(new Date(System.currentTimeMillis() + (300000)))) {
			queue.started = new Date(starttime.getTime());
		}

		if ((releasetime != null) && releasetime.after(new Date(System.currentTimeMillis() + (300000)))) {
			queue.release = new Date(releasetime.getTime());
		}

		queueService.create(queue);
	}

	public GenericListModel<Preset> getPresetListModel()
	{
		return presetListModel;
	}

	public ComboBoxModel getPlaylistListModel()
	{
		return playlistListModel;
	}

	public void synchronizePlaylists(final List<Account> accounts)
	{
		playlistService.synchronizePlaylists(accounts);
	}

	@EventTopicSubscriber(topic = "playlistsSynchronized", referenceStrength = ReferenceStrength.STRONG)
	public void onPlaylistSynchronize(final String topic, final Object object)
	{
		changeAccount((Account) accountListModel.getSelectedItem());
	}

	public void changeAccount(final Account account)
	{
		playlistListModel.removeAllElements();
		for (final Playlist playlist : playlistService.getByAccount(account)) {
			playlistListModel.addElement(playlist);
		}
	}

	public void importAccount()
	{
		importManager.importAccount();
	}

	public void importPreset()
	{
		importManager.importPreset();
	}

	public void importQueue()
	{
		importManager.importQueue();
	}

	public void exportAccount()
	{
		exportManager.exportAccount();
	}

	public void exportPreset()
	{
		exportManager.exportPreset();
	}

	public void exportQueue()
	{
		exportManager.exportQueue();
	}

	public PresetService getPresetService()
	{
		return presetService;
	}

	public void addPlaceholder(final String placeholder, final String replacement)
	{
	}

	public void deletePlaceholder(final Placeholder placeholder)
	{

	}

	public void savePlaceholder(final Placeholder placeholder)
	{
	}

	@SuppressWarnings("TypeMayBeWeakened") public PlaceholderTableModel getPlaceholderModel()
	{
		final Placeholder placeholder = new Placeholder();
		placeholder.placeholder = "WTF"; //NON-NLS
		placeholder.replacement = "OMG"; //NON-NLS
		placeholderModel.addRow(placeholder);
		placeholderModel.addRow(placeholder);
		placeholderModel.addRow(placeholder);
		return placeholderModel;
	}
}
