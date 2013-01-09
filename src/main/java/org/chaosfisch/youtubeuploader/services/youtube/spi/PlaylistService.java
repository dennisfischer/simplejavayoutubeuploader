/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.services.youtube.spi;

import java.util.List;

import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Playlist;

public interface PlaylistService {
	String	PLAYLISTS_SYNCHRONIZED	= "playlistsSynchronized";
	
	void addLatestVideoToPlaylist(Playlist playlist, String videoId);
	
	void addYoutubePlaylist(Playlist playlist);
	
	void synchronizePlaylists(List<Account> accounts);
}
