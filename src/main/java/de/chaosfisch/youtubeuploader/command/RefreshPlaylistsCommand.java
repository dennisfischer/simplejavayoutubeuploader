/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.youtubeuploader.command;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.youtube.playlist.Playlist;
import de.chaosfisch.youtubeuploader.db.dao.PlaylistDao;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class RefreshPlaylistsCommand extends Service<Multimap<Account, Playlist>> {

	@Inject
	private PlaylistDao     playlistDao;
	@Inject
	private PlaylistService playlistService;

	public Account[] accounts;

	@Override
	protected Task<Multimap<Account, Playlist>> createTask() {
		return new Task<Multimap<Account, Playlist>>() {
			@Override
			protected Multimap<Account, Playlist> call() throws Exception {
				Preconditions.checkNotNull(accounts);
				return playlistService.synchronizePlaylists(accounts);
			}
		};
	}
}
