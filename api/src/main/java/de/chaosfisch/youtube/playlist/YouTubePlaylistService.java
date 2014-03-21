/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.playlist;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import de.chaosfisch.data.MultiMapProperty;
import de.chaosfisch.data.playlist.IPlaylistDAO;
import de.chaosfisch.youtube.YouTubeFactory;
import de.chaosfisch.youtube.account.AccountModel;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class YouTubePlaylistService implements IPlaylistService {
	private static final Logger logger            = LoggerFactory.getLogger(YouTubePlaylistService.class);
	private static final String DEFAULT_THUMBNAIL = "https://i.ytimg.com/vi/default.jpg";
	private static final long   MAX_PLAYLISTS     = 50L;

	//TODO initial load map
	private final MultiMapProperty<AccountModel, PlaylistModel> modelMultiMapProperty = new MultiMapProperty<>();
	private final IPlaylistDAO playlistDAO;

	public YouTubePlaylistService(final IPlaylistDAO playlistDAO) {
		this.playlistDAO = playlistDAO;
	}

	List<PlaylistModel> parsePlaylistListResponse(final AccountModel account, final PlaylistListResponse response) {
		final ArrayList<PlaylistModel> list = new ArrayList<>(response.getItems()
																	  .size());
		for (final Playlist entry : response.getItems()) {
			//		final PlaylistModel playlist = playlistDAO.get(entry.getId());
			final PlaylistModel playlist = null;
			if (null == playlist) {
				list.add(_createNewPlaylist(account, entry));
			} else {
				list.add(_updateExistingPlaylist(account, entry, playlist));
			}
		}
		return list;
	}

	PlaylistModel _updateExistingPlaylist(final AccountModel accountModel, final Playlist entry, final PlaylistModel playlist) {
		setPlaylistModelInfos(accountModel, entry, playlist);
		store(playlist, accountModel);
		return playlist;
	}

	PlaylistModel _createNewPlaylist(final AccountModel accountModel, final Playlist entry) {
		final PlaylistModel playlist = new PlaylistModel();
		playlist.setYoutubeId(entry.getId());
		setPlaylistModelInfos(accountModel, entry, playlist);
		store(playlist, accountModel);
		return playlist;
	}

	private void setPlaylistModelInfos(final AccountModel accountModel, final Playlist entry, final PlaylistModel playlist) {
		playlist.setTitle(entry.getSnippet()
							   .getTitle());
		playlist.setItemCount(entry.getContentDetails()
								   .getItemCount());
		playlist.setAccountId(accountModel.getYoutubeId());
		playlist.setDescription(entry.getSnippet()
									 .getDescription());
		final String thumbnailUrl = entry.getSnippet()
										 .getThumbnails()
										 .getHigh()
										 .getUrl();
		playlist.setThumbnail(thumbnailUrl.equals(DEFAULT_THUMBNAIL) ? null : thumbnailUrl);
	}

	@Override
	public void addVideoToPlaylist(final PlaylistModel playlistModel, final String videoId) {

		final ResourceId resourceId = new ResourceId();
		resourceId.setKind("youtube#video");
		resourceId.setVideoId(videoId);

		final PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
		playlistItemSnippet.setPlaylistId(playlistModel.getYoutubeId());
		playlistItemSnippet.setResourceId(resourceId);

		final PlaylistItem playlistItem = new PlaylistItem();
		playlistItem.setSnippet(playlistItemSnippet);

//		YouTubeFactory.getYouTube(accountDAO.get(playlistModel.getYoutubeId()))
//					  .playlistItems()
//					  .insert("snippet,status", playlistItem)
//					  .execute();

		logger.debug("Video added to playlist!");
	}

	@Override
	public void addYoutubePlaylist(final PlaylistModel playlistModel) {
		logger.debug("Adding playlist {} to youtube.", playlistModel.getTitle());
		final PlaylistSnippet playlistSnippet = new PlaylistSnippet();
		playlistSnippet.setTitle(playlistModel.getTitle());
		playlistSnippet.setDescription(playlistModel.getDescription());

		final PlaylistStatus playlistStatus = new PlaylistStatus();
		playlistStatus.setPrivacyStatus(playlistModel.getPrivacyStatus() ? "private" : "public");

		final Playlist youTubePlaylist = new Playlist();
		youTubePlaylist.setSnippet(playlistSnippet);
		youTubePlaylist.setStatus(playlistStatus);

//		YouTubeFactory.getYouTube(accountDAO.get(playlistModel.getYoutubeId()))
//					  .playlists()
//					  .insert("snippet,status", youTubePlaylist)
//					  .execute();
		logger.info("Added playlist to youtube");

	}

	@Override
	public void synchronizePlaylists(final List<AccountModel> accountModels) throws IOException {
		logger.info("Synchronizing playlists.");
		for (final AccountModel accountModel : accountModels) {
			final YouTube.Playlists.List playlistsRequest = YouTubeFactory.getYouTube(accountModel)
																		  .playlists()
																		  .list("id,snippet,contentDetails")
																		  .setMaxResults(MAX_PLAYLISTS)
																		  .setMine(true);

			String nextPageToken = "";
			final List<PlaylistModel> playlists = new ArrayList<>((int) MAX_PLAYLISTS);
			do {
				playlistsRequest.setPageToken(nextPageToken);
				final PlaylistListResponse response = playlistsRequest.execute();
				playlists.addAll(parsePlaylistListResponse(accountModel, response));
				nextPageToken = response.getNextPageToken();
			} while (null != nextPageToken);

			logger.debug("Playlist synchronize okay.");
			final List<PlaylistModel> accountPlaylists = accountModel.getPlaylists();
			accountPlaylists.removeAll(playlists);
			for (final PlaylistModel playlist : accountPlaylists) {
				remove(playlist, accountModel);
			}
			accountModel.getPlaylists()
						.addAll(playlists);
			//		accountDAO.store(accountModel);
		}
		logger.info("Playlists synchronized");
	}

	@Override
	public ObservableList<PlaylistModel> playlistModelsProperty(final AccountModel accountModel) {
		if (!modelMultiMapProperty.containsKey(accountModel)) {
			modelMultiMapProperty.put(accountModel, null);
		}

		return modelMultiMapProperty.get(accountModel);
	}

	@Override
	public ObservableList<AccountModel> accountModelsProperty() {
		return modelMultiMapProperty.keys();
	}


	public PlaylistModel get(final String youtubeId) {
		//	return playlistDAO.get(youtubeId);
		return null;
	}

	public void store(final PlaylistModel playlistModel, final AccountModel accountModel) {
		modelMultiMapProperty.put(accountModel, playlistModel);
//		playlistDAO.store(playlistModel, accountModel);
	}

	public void remove(final PlaylistModel playlistModel, final AccountModel accountModel) {
		modelMultiMapProperty.remove(accountModel, playlistModel);
		//	playlistDAO.remove(playlistModel, accountModel);
	}
}
