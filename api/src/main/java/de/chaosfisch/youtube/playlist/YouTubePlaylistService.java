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
import de.chaosfisch.data.playlist.PlaylistDAO;
import de.chaosfisch.data.playlist.PlaylistDTO;
import de.chaosfisch.youtube.YouTubeFactory;
import de.chaosfisch.youtube.account.AccountModel;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sormula.SormulaException;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class YouTubePlaylistService implements IPlaylistService {

	private static final Logger LOGGER            = LoggerFactory.getLogger(YouTubePlaylistService.class);
	private static final String DEFAULT_THUMBNAIL = "https://i.ytimg.com/vi/default.jpg";
	private static final long   MAX_PLAYLISTS     = 50L;

	private final PlaylistDAO playlistDAO;
	private final SimpleMapProperty<AccountModel, ObservableSet<PlaylistModel>> playlistModelSimpleMapProperty = new SimpleMapProperty<>(
			FXCollections.observableHashMap());

	@Inject
	public YouTubePlaylistService(final PlaylistDAO playlistDAO, final SimpleListProperty<AccountModel> accountModels) {
		this.playlistDAO = playlistDAO;

		accountModels.forEach(this::initPlaylistList);
		accountModels.get().addListener(new ListChangeListener<AccountModel>() {
			@Override
			public void onChanged(final Change<? extends AccountModel> change) {
				while (change.next()) {

					if (change.wasAdded()) {
						change.getAddedSubList().forEach(YouTubePlaylistService.this::initPlaylistList);
						try {
							refresh();
						} catch (final IOException e) {
							LOGGER.error("Failed playlist refresh", e);
						}
					} else if (change.wasRemoved()) {
						change.getRemoved().forEach(a -> playlistModelSimpleMapProperty.remove(a.getYoutubeId()));
					}
				}
			}
		});
	}

	private ObservableSet<PlaylistModel> initPlaylistList(final AccountModel a) {
		try {
			return playlistModelSimpleMapProperty.putIfAbsent(a, FXCollections.observableSet(
																	  playlistDAO.selectAllCustom("WHERE accountId = ?", a.getYoutubeId())
																				 .stream()
																				 .map(this::fromDTO)
																				 .collect(Collectors.toSet())
															  )
			);
		} catch (SormulaException e) {
			e.printStackTrace();
		}
		return FXCollections.emptyObservableSet();
	}

	List<PlaylistModel> parsePlaylistListResponse(final AccountModel account, final PlaylistListResponse response) {
		final ArrayList<PlaylistModel> list = new ArrayList<>(response.getItems().size());
		for (final Playlist entry : response.getItems()) {
			try {
				final PlaylistDTO playlistDTO = playlistDAO.select(entry.getId());

				if (null == playlistDTO) {
					list.add(_createNewPlaylist(account, entry));
				} else {
					final PlaylistModel playlist = fromDTO(playlistDTO);
					list.add(_updateExistingPlaylist(account, entry, playlist));
				}
			} catch (SormulaException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	PlaylistModel _updateExistingPlaylist(final AccountModel accountModel, final Playlist entry, final PlaylistModel playlist) {
		return updateAndStorePlaylist(accountModel, entry, playlist);
	}

	PlaylistModel _createNewPlaylist(final AccountModel accountModel, final Playlist entry) {
		final PlaylistModel playlist = new PlaylistModel();
		playlist.setYoutubeId(entry.getId());
		return updateAndStorePlaylist(accountModel, entry, playlist);
	}

	private PlaylistModel updateAndStorePlaylist(final AccountModel accountModel, final Playlist entry, final PlaylistModel playlist) {
		setPlaylistModelInfos(accountModel, entry, playlist);
		return playlist;
	}

	private void setPlaylistModelInfos(final AccountModel accountModel, final Playlist entry, final PlaylistModel playlist) {
		playlist.setTitle(entry.getSnippet().getTitle());
		playlist.setItemCount(entry.getContentDetails().getItemCount());
		playlist.setAccountId(accountModel.getYoutubeId());
		playlist.setDescription(entry.getSnippet().getDescription());
		final String thumbnailUrl = entry.getSnippet().getThumbnails().getHigh().getUrl();
		playlist.setThumbnail(thumbnailUrl.equals(DEFAULT_THUMBNAIL) ? null : thumbnailUrl);
	}

	@Override
	public void addVideoToPlaylist(final PlaylistModel playlistModel, final AccountModel accountModel, final String videoId) {

		final ResourceId resourceId = new ResourceId();
		resourceId.setKind("youtube#video");
		resourceId.setVideoId(videoId);

		final PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
		playlistItemSnippet.setPlaylistId(playlistModel.getYoutubeId());
		playlistItemSnippet.setResourceId(resourceId);

		final PlaylistItem playlistItem = new PlaylistItem();
		playlistItem.setSnippet(playlistItemSnippet);

		try {
			YouTubeFactory.getYouTube(accountModel).playlistItems().insert("snippet,status", playlistItem).execute();
			LOGGER.debug("Video added to playlist!");
		} catch (final IOException e) {
			LOGGER.error("Failed adding video to playlist.");
		}
	}

	@Override
	public void addYoutubePlaylist(final PlaylistModel playlistModel, final AccountModel accountModel) {
		LOGGER.debug("Adding playlist {} to youtube.", playlistModel.getTitle());
		final PlaylistSnippet playlistSnippet = new PlaylistSnippet();
		playlistSnippet.setTitle(playlistModel.getTitle());
		playlistSnippet.setDescription(playlistModel.getDescription());

		final PlaylistStatus playlistStatus = new PlaylistStatus();
		playlistStatus.setPrivacyStatus(playlistModel.getPrivacyStatus() ? "private" : "public");

		final Playlist youTubePlaylist = new Playlist();
		youTubePlaylist.setSnippet(playlistSnippet);
		youTubePlaylist.setStatus(playlistStatus);

		try {
			YouTubeFactory.getYouTube(accountModel).playlists().insert("snippet,status", youTubePlaylist).execute();
			LOGGER.info("Added playlist to youtube");
		} catch (final IOException e) {
			LOGGER.error("Failed adding playlist to youtube");
		}
	}

	@Override
	public void refresh() throws IOException {
		LOGGER.info("Synchronizing playlists.");
		for (final AccountModel accountModel : playlistModelSimpleMapProperty.keySet()) {
			final YouTube.Playlists.List playlistsRequest = YouTubeFactory.getYouTube(accountModel)
																		  .playlists()
																		  .list("id,snippet,contentDetails")
																		  .setMaxResults(MAX_PLAYLISTS)
																		  .setMine(true);
			String nextPageToken = "";
			do {
				playlistsRequest.setPageToken(nextPageToken);
				final PlaylistListResponse response = playlistsRequest.execute();
				parsePlaylistListResponse(accountModel, response).forEach(playlistModel -> store(playlistModel, accountModel));
				playlistDAO.clearOld(accountModel.getYoutubeId());
				nextPageToken = response.getNextPageToken();
			} while (null != nextPageToken);
		}
		LOGGER.info("Playlists synchronized");
	}

	@Override
	public ObservableSet<PlaylistModel> playlistModelsProperty(final AccountModel accountModel) {
		return playlistModelSimpleMapProperty.get(accountModel);
	}

	@Override
	public void store(final PlaylistModel playlistModel, final AccountModel accountModel) {
		final ObservableSet<PlaylistModel> playlistModels = playlistModelSimpleMapProperty.get(accountModel);
		if (!playlistModels.contains(accountModel)) {
			playlistModels.add(playlistModel);
		}
		final PlaylistDTO playlistDTO = toDTO(playlistModel);
		try {
			if (0 == playlistDAO.update(playlistDTO)) {
				playlistDAO.insert(playlistDTO);
			}
		} catch (SormulaException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void remove(final PlaylistModel playlistModel, final AccountModel accountModel) {
		playlistModelSimpleMapProperty.get(accountModel).remove(playlistModel);
		try {
			playlistDAO.delete(toDTO(playlistModel));
		} catch (SormulaException e) {
			e.printStackTrace();
		}
	}

	public PlaylistModel get(final String youtubeId) {
		try {
			return fromDTO(playlistDAO.select(youtubeId));
		} catch (SormulaException e) {
			e.printStackTrace();
		}
		return null;
	}

	private PlaylistModel fromDTO(final PlaylistDTO playlistDTO) {
		final PlaylistModel playlistModel = new PlaylistModel();
		playlistModel.setYoutubeId(playlistDTO.getPlaylistId());
		playlistModel.setTitle(playlistDTO.getTitle());
		playlistModel.setThumbnail(playlistDTO.getThumbnail());
		playlistModel.setPrivacyStatus(playlistDTO.isPrivacyStatus());
		playlistModel.setItemCount(playlistDTO.getItemCount());
		playlistModel.setDescription(playlistDTO.getDescription());
		playlistModel.setAccountId(playlistDTO.getAccountId());
		return playlistModel;
	}

	private PlaylistDTO toDTO(final PlaylistModel playlistModel) {
		return new PlaylistDTO(playlistModel.getYoutubeId(), playlistModel.getTitle(), playlistModel.getThumbnail(), playlistModel.getPrivacyStatus(),
							   playlistModel.getItemCount(), playlistModel.getDescription(), playlistModel.getAccountId());
	}
}
