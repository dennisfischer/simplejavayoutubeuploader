/*
 * Copyright (c) 2014 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.playlist;

import com.google.api.services.youtube.model.*;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import de.chaosfisch.google.YouTubeProvider;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.account.IAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractPlaylistService implements IPlaylistService {
	private static final Logger logger = LoggerFactory.getLogger(AbstractPlaylistService.class);
	private static final String DEFAULT_THUMBNAIL = "https://i.ytimg.com/vi/default.jpg";
	private static final long MAX_PLAYLISTS = 50L;
	private final IAccountService accountService;
	private final YouTubeProvider youTubeProvider;

	@Inject
	protected AbstractPlaylistService(final IAccountService accountService, final YouTubeProvider youTubeProvider) {
		this.accountService = accountService;
		this.youTubeProvider = youTubeProvider;
	}

	@Override
	public void addVideoToPlaylist(final Playlist playlist, final String videoId) throws PlaylistIOException {
		try {

			final ResourceId resourceId = new ResourceId();
			resourceId.setKind("youtube#video");
			resourceId.setVideoId(videoId);

			final PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
			playlistItemSnippet.setPlaylistId(playlist.getPkey());
			playlistItemSnippet.setResourceId(resourceId);

			final PlaylistItem playlistItem = new PlaylistItem();
			playlistItem.setSnippet(playlistItemSnippet);

			youTubeProvider.setAccount(playlist.getAccount())
					.get()
					.playlistItems()
					.insert("snippet,status", playlistItem)
					.execute();

			logger.debug("Video added to playlist!");
		} catch (final Exception e) {
			throw new PlaylistIOException(e);
		}
	}

	@Override
	public void addYoutubePlaylist(final Playlist playlist) throws PlaylistIOException {
		logger.debug("Adding playlist {} to youtube.", playlist.getTitle());
		try {
			final PlaylistSnippet playlistSnippet = new PlaylistSnippet();
			playlistSnippet.setTitle(playlist.getTitle());
			playlistSnippet.setDescription(playlist.getSummary());

			final PlaylistStatus playlistStatus = new PlaylistStatus();
			playlistStatus.setPrivacyStatus(playlist.isPrivate_() ? "private" : "public");

			final com.google.api.services.youtube.model.Playlist youTubePlaylist = new com.google.api.services.youtube.model.Playlist();
			youTubePlaylist.setSnippet(playlistSnippet);
			youTubePlaylist.setStatus(playlistStatus);

			youTubeProvider.setAccount(playlist.getAccount())
					.get()
					.playlists()
					.insert("snippet,status", youTubePlaylist)
					.execute();
			logger.info("Added playlist to youtube");
		} catch (final Exception e) {
			throw new PlaylistIOException(e);
		}
	}

	@Override
	public void synchronizePlaylists(final List<Account> accounts) throws PlaylistIOException {
		logger.info("Synchronizing playlists.");
		for (final Account account : accounts) {
			try {
				final PlaylistListResponse response = youTubeProvider.setAccount(account)
						.get()
						.playlists()
						.list("id,snippet,contentDetails")
						.setMaxResults(MAX_PLAYLISTS)
						.setMine(true)
						.execute();

				logger.debug("Playlist synchronize okay.");

				final List<Playlist> playlists = parsePlaylistListResponse(account, response);
				final List<Playlist> accountPlaylists = account.getPlaylists();
				accountPlaylists.removeAll(playlists);
				for (final Playlist playlist : accountPlaylists) {
					delete(playlist);
				}
				account.setPlaylists(playlists);
				accountService.update(account);
			} catch (final Exception e) {
				logger.error("Playlist sync exception", e);
				throw new PlaylistIOException(e);
			}
		}
		logger.info("Playlists synchronized");
	}

	List<Playlist> parsePlaylistListResponse(final Account account, final PlaylistListResponse response) {
		final List<Playlist> list = Lists.newArrayListWithExpectedSize(response.getItems().size());
		for (final com.google.api.services.youtube.model.Playlist entry : response.getItems()) {
			final Playlist playlist = findByPkey(entry.getId());
			if (null == playlist) {
				list.add(_createNewPlaylist(account, entry));
			} else {
				list.add(_updateExistingPlaylist(account, entry, playlist));
			}
		}
		return list;
	}

	Playlist _createNewPlaylist(final Account account, final com.google.api.services.youtube.model.Playlist entry) {
		final Playlist playlist = new Playlist(entry.getSnippet().getTitle(), account);
		playlist.setPkey(entry.getId());
		playlist.setNumber(entry.getContentDetails().getItemCount());
		playlist.setSummary(entry.getSnippet().getDescription());
		final String thumbnailUrl = entry.getSnippet().getThumbnails().getHigh().getUrl();
		playlist.setThumbnail(thumbnailUrl.equals(DEFAULT_THUMBNAIL) ? null : thumbnailUrl);
		playlist.setHidden(false);
		insert(playlist);
		return playlist;
	}

	Playlist _updateExistingPlaylist(final Account account, final com.google.api.services.youtube.model.Playlist entry, final Playlist playlist) {
		playlist.setTitle(entry.getSnippet().getTitle());
		playlist.setNumber(entry.getContentDetails().getItemCount());
		playlist.setSummary(entry.getSnippet().getDescription());
		final String thumbnailUrl = entry.getSnippet().getThumbnails().getHigh().getUrl();
		playlist.setThumbnail(thumbnailUrl.equals(DEFAULT_THUMBNAIL) ? null : thumbnailUrl);
		playlist.setAccount(account);
		update(playlist);
		return playlist;
	}
}
