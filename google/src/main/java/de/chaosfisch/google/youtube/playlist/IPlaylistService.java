/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.playlist;

import de.chaosfisch.google.account.Account;

import java.util.List;

public interface IPlaylistService {

	List<Playlist> getAll(Account account);

	Playlist get(String id);

	void insert(Playlist playlist);

	void update(Playlist playlist);

	void delete(Playlist playlist);

	List<Playlist> findByHidden(Account account, boolean hidden);

	Playlist findByPkey(String playlistKey);

	List<Playlist> fetchUnhiddenByAccount(Account account);

	/**
	 * Adds the specified video to the playlist.
	 * This change will be published on YouTube.
	 *
	 * @param playlist the playlist used
	 * @param videoId  for the added video
	 */
	void addVideoToPlaylist(Playlist playlist, String videoId) throws PlaylistIOException;

	/**
	 * Adds this playlist to the account specified inside the playlist object.
	 * This change will be published on YouTube.
	 *
	 * @param playlist the playlist to be added
	 */
	void addYoutubePlaylist(Playlist playlist) throws PlaylistIOException;

	/**
	 * Synchronizes given accounts and playlists with YouTube.
	 *
	 * @param accounts to be synced
	 */
	void synchronizePlaylists(List<Account> accounts) throws PlaylistIOException;
}
