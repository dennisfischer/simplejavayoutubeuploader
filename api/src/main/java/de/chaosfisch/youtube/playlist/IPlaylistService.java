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
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.List;

public interface IPlaylistService {

	/**
	 * Adds the specified video to the playlist.
	 * This change will be published on YouTube.
	 *
	 * @param playlistModel the playlist used
	 * @param videoId       for the added video
	 */
	void addVideoToPlaylist(PlaylistModel playlistModel, String videoId);

	/**
	 * Adds this playlist to the account specified inside the playlist object.
	 * This change will be published on YouTube.
	 *
	 * @param playlistModel the playlist to be added
	 */
	void addYoutubePlaylist(PlaylistModel playlistModel);

	/**
	 * Synchronizes given accounts and playlists with YouTube.
	 *
	 * @param accountModels to be synced
	 */
	void synchronizePlaylists(List<AccountModel> accountModels) throws IOException;

	/**
	 * Returns a list property of PlaylistModels
	 *
	 * @param accountModel owning the PlaylistModels
	 * @return ObservableList<PlaylistModel> playlistModels
	 */
	ObservableList<PlaylistModel> playlistModelsProperty(AccountModel accountModel);

	ObservableList<AccountModel> accountModelsProperty();
}
