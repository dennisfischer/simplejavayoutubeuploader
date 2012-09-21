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

package org.chaosfisch.youtubeuploader.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.JFileChooser;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.bushe.swing.event.annotation.ReferenceStrength;
import org.chaosfisch.util.Mimetype;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.GenericListModel;
import org.chaosfisch.youtubeuploader.models.Placeholder;
import org.chaosfisch.youtubeuploader.models.PlaceholderTableModel;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.models.Preset;
import org.chaosfisch.youtubeuploader.models.Queue;
import org.chaosfisch.youtubeuploader.services.spi.AccountService;
import org.chaosfisch.youtubeuploader.services.spi.PlaceholderService;
import org.chaosfisch.youtubeuploader.services.spi.PlaylistService;
import org.chaosfisch.youtubeuploader.services.spi.PresetService;
import org.chaosfisch.youtubeuploader.services.spi.QueueService;

import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class UploadController
{

	/**
	 * Injected services
	 */
	private final AccountService     accountService;
	private final QueueService       queueService;
	private final PresetService      presetService;
	private final PlaylistService    playlistService;
	private final PlaceholderService placeholderService;

	private final ImportManager importManager;
	private final ExportManager exportManager;
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

		@EventTopicSubscriber(topic = PresetService.PRESET_ADDED)
		public void onPresetAdded(final String topic, final Preset preset)
		{
			addElement(preset);
		}

		@EventTopicSubscriber(topic = PresetService.PRESET_UPDATED)
		public void onPresetUpdated(final String topic, final Preset preset)
		{
			final int index = getIndexOf(preset);
			if (index != -1) {
				replaceRow(index, preset);
			}
		}

		@EventTopicSubscriber(topic = PresetService.PRESET_REMOVED)
		public void onPresetRemoved(final String topic, final Preset preset)
		{
			removeElement(preset);
		}
	};
	private final GenericListModel<Playlist> playlistListModel = new GenericListModel<Playlist>()
	{
		private static final long serialVersionUID = 8997201386145568022L;

		@EventTopicSubscriber(topic = PlaylistService.PLAYLIST_ADDED)
		public void onPlaylistAdded(final String topic, final Playlist playlist)
		{
			addElement(playlist);
		}

		@EventTopicSubscriber(topic = PlaylistService.PLAYLIST_REMOVED)
		public void onPlaylistRemoved(final String topic, final Playlist playlist)
		{
			removeElement(playlist);
		}

		@EventTopicSubscriber(topic = PlaylistService.PLAYLIST_UPDATED)
		public void onPlaylistUpdated(final String topic, final Playlist playlist)
		{
			final int index = getIndexOf(playlist);
			if (index != -1) {
				replaceRow(index, playlist);
			}
		}
	};

	private final PlaceholderTableModel placeholderModel = new PlaceholderTableModel();

	private boolean autotitle;

	@Inject
	public UploadController(final AccountService accountService, final PresetService presetService, final QueueService queueService, final PlaylistService playlistService, final PlaceholderService placeholderService, final JFileChooser fileChooser)
	{
		this.accountService = accountService;
		this.presetService = presetService;
		this.queueService = queueService;
		this.playlistService = playlistService;
		this.placeholderService = placeholderService;

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

	public GenericListModel<Account> getAccountListModel()
	{
		return accountListModel;
	}

	public AccountService getAccountService()
	{
		return accountService;
	}

	public void submitUpload(final String filepath, final Account account, final String category)
	{
		submitUpload(filepath, account, category, (short) 0, new String(filepath.substring(0, filepath.lastIndexOf("."))), filepath, filepath, null, 0, (short) 0, (short) 0, true, true, true, true, null, null, null, false, false, false, false,
		             (short) 0);
	}

	public void submitUpload(final String filepath, final Account account, final String category, final short visibility, final String title, final String description, final String tags)
	{
		submitUpload(filepath, account, category, visibility, title, description, tags, null, 0, (short) 0, (short) 0, true, true, true, true, null, null, null, false, false, false, false, (short) 0);
	}

	public void submitUpload(final String filepath, final Account account, final String category, final short visibility, final String title, final String description, final String tags, final Playlist playlist)
	{
		submitUpload(filepath, account, category, visibility, title, description, tags, playlist, 0, (short) 0, (short) 0, true, true, true, true, null, null, null, false, false, false, false, (short) 0);
	}

	public void submitUpload(final String filepath,
	                         final Account account,
	                         final String category,
	                         final short visibility,
	                         final String title,
	                         final String description,
	                         final String tags,
	                         final Playlist playlist,
	                         final int number,
	                         final short comment,
	                         final short videoresponse,
	                         final boolean rate,
	                         final boolean embed,
	                         final boolean commentvote,
	                         final boolean mobile)
	{
		submitUpload(filepath, account, category, visibility, title, description, tags, playlist, number, comment, videoresponse, rate, embed, commentvote, mobile, null, null, null, false, false, false, false, (short) 0);
	}

	public void submitUpload(final String filepath,
	                         final Account account,
	                         final String category,
	                         final short visibility,
	                         final String title,
	                         final String description,
	                         final String tags,
	                          final Playlist playlist,
	                         final int number,
	                         final short comment,
	                         final short videoresponse,
	                         final boolean rate,
	                         final boolean embed,
	                         final boolean commentvote,
	                         final boolean mobile,
	                         final Date starttime,
	                          final Date releasetime,
	                          final String enddir,
	                         final boolean monetize,
	                         final boolean monetizeOverlay,
	                         final boolean monetizeTrueview,
	                         final boolean monetizeProduct,
	                         final short license)
	{
		submitUpload(filepath, account, category, visibility, title, description, tags, playlist, number, comment, videoresponse, commentvote, rate, embed, mobile, starttime, releasetime, enddir, monetize, monetizeOverlay, monetizeTrueview,
		             monetizeProduct, license, false, (short) 0, (short) 0, false, false, false, false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
	}

	public void submitUpload(final String filepath,
	                         final Account account,
	                         final String category,
	                         final short visibility,
	                         final String title,
	                         final String description,
	                         final String tags,
	                          final Playlist playlist,
	                         final int number,
	                         final short comment,
	                         final short videoresponse,
	                         final boolean rate,
	                         final boolean embed,
	                         final boolean commentvote,
	                         final boolean mobile,
	                          final Date starttime,
	                          final Date releasetime,
	                          final String enddir,
	                         final boolean monetize,
	                         final boolean monetizeOverlay,
	                         final boolean monetizeTrueview,
	                         final boolean monetizeProduct,
	                         final short license,
	                         final boolean claim,
	                         final short claimtype,
	                         final short claimpolicy,
	                         final boolean partnerOverlay,
	                         final boolean partnerTrueview,
	                         final boolean partnerInstream,
	                         final boolean partnerProduct,
	                          final String asset,
	                          final String webTitle,
	                          final String webID,
	                          final String webDescription,
	                          final String webNotes,
	                          final String tvTMSID,
	                          final String tvISAN,
	                          final String tvEIDR,
	                          final String showTitle,
	                          final String episodeTitle,
	                          final String seasonNb,
	                          final String episodeNb,
	                          final String tvID,
	                          final String tvNotes,
	                          final String movieTitle,
	                          final String movieDescription,
	                          final String movieTMSID,
	                          final String movieISAN,
	                          final String movieEIDR,
	                          final String movieID,
	                          final String movieNotes,
	                          final String thumbnail)
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
			final Calendar calendar = Calendar.getInstance();
			calendar.setTime(releasetime);
			final int unroundedMinutes = calendar.get(Calendar.MINUTE);
			final int mod = unroundedMinutes % 30;
			calendar.add(Calendar.MINUTE, (mod < 16) ? -mod : (30 - mod));

			queue.release = calendar.getTime();
		}

		//Partnerfeatures
		queue.claim = claim;
		queue.claimtype = claimtype;
		queue.claimpolicy = claimpolicy;
		queue.partnerOverlay = partnerOverlay;
		queue.partnerTrueview = partnerTrueview;
		queue.partnerProduct = partnerProduct;
		queue.partnerInstream = partnerInstream;
		queue.asset = asset;
		queue.webTitle = webTitle;
		queue.webDescription = webDescription;
		queue.webID = webID;
		queue.webNotes = webNotes;
		queue.tvTMSID = tvTMSID;
		queue.tvISAN = tvISAN;
		queue.tvEIDR = tvEIDR;
		queue.showTitle = showTitle;
		queue.episodeTitle = episodeTitle;
		queue.seasonNb = seasonNb;
		queue.episodeNb = episodeNb;
		queue.tvID = tvID;
		queue.tvNotes = tvNotes;
		queue.movieTitle = movieTitle;
		queue.movieDescription = movieDescription;
		queue.movieTMSID = movieTMSID;
		queue.movieISAN = movieISAN;
		queue.movieEIDR = movieEIDR;
		queue.movieID = movieID;
		queue.movieNotes = movieNotes;

		queue.number = number;

		if ((thumbnail != null) && !thumbnail.isEmpty()) {
			queue.thumbnail = true;
			queue.thumbnailimage = thumbnail;
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
		final Placeholder placeholderObject = new Placeholder();
		placeholderObject.placeholder = placeholder;
		placeholderObject.replacement = replacement;
		placeholderService.create(placeholderObject);
	}

	public void deletePlaceholder(final Placeholder placeholder)
	{
		placeholderService.delete(placeholder);
	}

	public void savePlaceholder(final Placeholder placeholder)
	{
		placeholderService.update(placeholder);
	}

	public PlaceholderTableModel getPlaceholderModel()
	{
		return placeholderModel;
	}

	public PlaceholderService getPlaceholderService()
	{
		return placeholderService;
	}
}
