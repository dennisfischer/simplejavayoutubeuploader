/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.google.playlist;

import com.google.api.services.youtube.model.*;
import de.chaosfisch.google.account.AccountModel;
import de.chaosfisch.google.account.IAccountService;
import de.chaosfisch.google.auth.GoogleAuthProvider;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class PlaylistService implements IPlaylistService {
	private static final Logger logger            = LoggerFactory.getLogger(PlaylistService.class);
	private static final String DEFAULT_THUMBNAIL = "https://i.ytimg.com/vi/default.jpg";
	private static final long   MAX_PLAYLISTS     = 50L;

	private final SimpleMapProperty<AccountModel, SimpleListProperty<PlaylistModel>> playlistModels = new SimpleMapProperty<>(FXCollections
																																	  .observableHashMap());
	private final IAccountService    accountService;
	private final GoogleAuthProvider googleAuthProvider;

	public PlaylistService(final IAccountService accountService, final GoogleAuthProvider googleAuthProvider) {
		this.accountService = accountService;
		this.googleAuthProvider = googleAuthProvider;
	}


	@Override
	public void addVideoToPlaylist(final PlaylistModel playlist, final String videoId) throws IOException {

		final ResourceId resourceId = new ResourceId();
		resourceId.setKind("youtube#video");
		resourceId.setVideoId(videoId);

		final PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
		playlistItemSnippet.setPlaylistId(playlist.getYoutubeId());
		playlistItemSnippet.setResourceId(resourceId);

		final PlaylistItem playlistItem = new PlaylistItem();
		playlistItem.setSnippet(playlistItemSnippet);

		googleAuthProvider.getYouTubeService(playlist.getAccount())
				.playlistItems()
				.insert("snippet,status", playlistItem)
				.execute();

		logger.debug("Video added to playlist!");

	}

	@Override
	public void addYoutubePlaylist(final PlaylistModel playlist) throws IOException {
		logger.debug("Adding playlist {} to youtube.", playlist.getTitle());
		final PlaylistSnippet playlistSnippet = new PlaylistSnippet();
		playlistSnippet.setTitle(playlist.getTitle());
		playlistSnippet.setDescription(playlist.getDescription());

		final PlaylistStatus playlistStatus = new PlaylistStatus();
		playlistStatus.setPrivacyStatus(playlist.getPrivacyStatus() ? "private" : "public");

		final Playlist youTubePlaylist = new Playlist();
		youTubePlaylist.setSnippet(playlistSnippet);
		youTubePlaylist.setStatus(playlistStatus);

		googleAuthProvider.getYouTubeService(playlist.getAccount())
				.playlists()
				.insert("snippet,status", youTubePlaylist)
				.execute();
		logger.info("Added playlist to youtube");

	}

	@Override
	public void synchronizePlaylists(final List<AccountModel> accounts) throws IOException {
		logger.info("Synchronizing playlists.");
		for (final AccountModel account : accounts) {
			final PlaylistListResponse response = googleAuthProvider.getYouTubeService(account)
					.playlists()
					.list("id,snippet,contentDetails")
					.setMaxResults(MAX_PLAYLISTS)
					.setMine(true)
					.execute();

			logger.debug("Playlist synchronize okay.");

			final List<PlaylistModel> playlists = parsePlaylistListResponse(account, response);
			final List<PlaylistModel> accountPlaylists = account.getPlaylists();
			accountPlaylists.removeAll(playlists);
			for (final PlaylistModel playlist : accountPlaylists) {
				delete(playlist);
			}
			account.getPlaylists().addAll(playlists);
			accountService.update(account);
		}
		logger.info("Playlists synchronized");
	}

	List<PlaylistModel> parsePlaylistListResponse(final AccountModel account, final PlaylistListResponse response) {
		final ArrayList<PlaylistModel> list = new ArrayList<>(response.getItems()
																	  .size());
		for (final Playlist entry : response.getItems()) {
			final PlaylistModel playlist = findByPkey(entry.getId());
			if (null == playlist) {
				list.add(_createNewPlaylist(account, entry));
			} else {
				list.add(_updateExistingPlaylist(account, entry, playlist));
			}
		}
		return list;
	}

	PlaylistModel _createNewPlaylist(final AccountModel account, final Playlist entry) {
		final PlaylistModel playlist = new PlaylistModel();
		playlist.setYoutubeId(entry.getId());
		setPlaylistModelInfos(account, entry, playlist);
		insert(playlist);
		return playlist;
	}

	private void setPlaylistModelInfos(final AccountModel account, final Playlist entry, final PlaylistModel playlist) {
		playlist.setTitle(entry.getSnippet().getTitle());
		playlist.setItemCount(entry.getContentDetails().getItemCount());
		playlist.setAccount(account);
		playlist.setDescription(entry.getSnippet().getDescription());
		final String thumbnailUrl = entry.getSnippet()
				.getThumbnails()
				.getHigh()
				.getUrl();
		playlist.setThumbnail(thumbnailUrl.equals(DEFAULT_THUMBNAIL) ? null : thumbnailUrl);
	}

	PlaylistModel _updateExistingPlaylist(final AccountModel account, final Playlist entry, final PlaylistModel playlist) {
		setPlaylistModelInfos(account, entry, playlist);
		update(playlist);
		return playlist;
	}
}
