/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.google.youtube;

import com.google.common.collect.Multimap;
import de.chaosfisch.exceptions.SystemException;
import org.chaosfisch.google.account.Account;
import org.chaosfisch.google.atom.Feed;

public interface PlaylistService {
	/**
	 * Adds the specified video to the playlist.
	 * This change will be published on YouTube.
	 *
	 * @param playlist
	 * 		the playlist used
	 * @param videoId
	 * 		for the added video
	 *
	 * @return the received response feed from YouTube
	 *
	 * @throws SystemException
	 * 		if request fails
	 */
	Feed addVideoToPlaylist(Playlist playlist, String videoId) throws SystemException;

	/**
	 * Adds this playlist to the account specified inside the playlist object.
	 * This change will be published on YouTube.
	 *
	 * @param playlist
	 * 		the playlist to be added
	 *
	 * @return the received response feed from YouTube
	 *
	 * @throws SystemException
	 * 		if request fails
	 */
	Feed addYoutubePlaylist(Playlist playlist) throws SystemException;

	/**
	 * Synchronizes given accounts and playlists with YouTube.
	 *
	 * @param accounts
	 * 		to be synced
	 *
	 * @return Multimap containing all done changes
	 *
	 * @throws SystemException
	 * 		if request fails
	 */
	Multimap<Account, Playlist> synchronizePlaylists(Account[] accounts) throws SystemException;
}
