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

import java.util.List;
import java.util.Map;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import org.chaosfisch.youtubeuploader.db.dao.PlaylistDao;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Account;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist;
import org.chaosfisch.youtubeuploader.services.PlaylistService;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class RefreshPlaylistsCommand extends Service<Map<Account, List<Playlist>>> {

	@Inject
	private PlaylistDao		playlistDao;
	@Inject
	private PlaylistService	playlistService;

	public Account[]		accounts;

	@Override
	protected Task<Map<Account, List<Playlist>>> createTask() {
		return new Task<Map<Account, List<Playlist>>>() {
			@Override
			protected Map<Account, List<Playlist>> call() throws Exception {
				Preconditions.checkNotNull(accounts);
				return playlistService.synchronizePlaylists(accounts);
			}
		};
	}
}
