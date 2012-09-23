/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Dennis Fischer
 ******************************************************************************/

package org.chaosfisch.youtubeuploader.dao.spi;

import java.util.List;

import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Playlist;

public interface PlaylistDao extends CRUDDao<Playlist>
{
	/**
	 * Event: Before Playlist-object is added
	 */
	String	PLAYLIST_PRE_ADDED		= "playlistPreAdded";

	/**
	 * Event: Before Playlist-object is removed
	 */
	String	PLAYLIST_PRE_REMOVED	= "playlistPreRemoved";

	/**
	 * Event: Before Playlist-object is updated
	 */
	String	PLAYLIST_PRE_UPDATED	= "playlistPreUpdated";

	/**
	 * Event: After Playlist-object was added
	 */
	String	PLAYLIST_POST_ADDED		= "playlistPostAdded";

	/**
	 * Event: After Playlist-object was removed
	 */
	String	PLAYLIST_POST_REMOVED	= "playlistPostRemoved";

	/**
	 * Event: After Playlist-object was updated
	 */
	String	PLAYLIST_POST_UPDATED	= "playlistPostUpdated";

	/**
	 * Searches for playlists containing the given Account
	 * 
	 * @param account
	 *            the account to search with
	 * @return List<Playlist> the found playlists
	 */
	List<Playlist> getByAccount(Account account);
}
