/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader.command;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.chaosfisch.google.account.Account;
import org.chaosfisch.google.youtube.Playlist;
import org.chaosfisch.google.youtube.PlaylistService;
import org.chaosfisch.youtubeuploader.db.dao.PlaylistDao;

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
