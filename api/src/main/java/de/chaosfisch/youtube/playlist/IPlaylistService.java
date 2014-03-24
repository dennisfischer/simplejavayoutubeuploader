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
import javafx.collections.ObservableSet;

import java.io.IOException;

public interface IPlaylistService {

	/**
	 * Adds the specified video to the playlist.
	 * This change will be published on YouTube.
	 *
	 * @param playlistModel the playlist used
	 * @param videoId       for the added video
	 */
	void addVideoToPlaylist(PlaylistModel playlistModel, AccountModel accountModel, String videoId);

	/**
	 * Adds this playlist to the account specified inside the playlist object.
	 * This change will be published on YouTube.
	 *
	 * @param playlistModel the playlist to be added
	 */
	void addYoutubePlaylist(PlaylistModel playlistModel, AccountModel accountModel);

	/**
	 * Synchronizes playlists with YouTube.
	 */
	void refresh() throws IOException;

	/**
	 * Returns a list property of PlaylistModels
	 *
	 * @param accountModel owning the PlaylistModels
	 * @return ObservableList<PlaylistModel> playlistModels
	 */
	ObservableSet<PlaylistModel> playlistModelsProperty(AccountModel accountModel);

	void store(PlaylistModel playlistModel, AccountModel accountModel);

	void remove(PlaylistModel playlistModel, AccountModel accountModel);
}
