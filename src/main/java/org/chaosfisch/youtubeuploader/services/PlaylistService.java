/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.services;

import java.util.List;
import java.util.Map;

import org.chaosfisch.exceptions.SystemException;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Account;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist;

public interface PlaylistService {
	String addLatestVideoToPlaylist(Playlist playlist, String videoId) throws SystemException;

	String addYoutubePlaylist(Playlist playlist) throws SystemException;

	Map<Account, List<Playlist>> synchronizePlaylists(List<Account> accounts) throws SystemException;
}
