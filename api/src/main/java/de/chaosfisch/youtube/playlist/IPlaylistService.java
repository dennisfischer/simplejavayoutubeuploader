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

import de.chaosfisch.youtube.account.AccountModel;
import javafx.beans.property.SimpleListProperty;

import java.io.IOException;
import java.util.List;

public interface IPlaylistService {

	List<PlaylistModel> getAll(AccountModel account);

	PlaylistModel get(String id);

	void insert(PlaylistModel playlist);

	void update(PlaylistModel playlist);

	void delete(PlaylistModel playlist);

	List<PlaylistModel> findByHidden(AccountModel account, boolean hidden);

	PlaylistModel findByPkey(String playlistKey);

	List<PlaylistModel> fetchUnhiddenByAccount(AccountModel account);

	/**
	 * Adds the specified video to the playlist.
	 * This change will be published on YouTube.
	 *
	 * @param playlist the playlist used
	 * @param videoId  for the added video
	 */
	void addVideoToPlaylist(PlaylistModel playlist, String videoId) throws IOException;

	/**
	 * Adds this playlist to the account specified inside the playlist object.
	 * This change will be published on YouTube.
	 *
	 * @param playlist the playlist to be added
	 */
	void addYoutubePlaylist(PlaylistModel playlist) throws IOException;

	/**
	 * Synchronizes given accounts and playlists with YouTube.
	 *
	 * @param accounts to be synced
	 */
	void synchronizePlaylists(List<AccountModel> accounts) throws IOException;

	SimpleListProperty<PlaylistModel> playlistModelsProperty(AccountModel accountModel);
}
