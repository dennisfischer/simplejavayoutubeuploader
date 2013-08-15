/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.persistence.dao;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.youtube.playlist.Playlist;
import de.chaosfisch.google.youtube.playlist.events.PlaylistAdded;
import de.chaosfisch.google.youtube.playlist.events.PlaylistRemoved;
import de.chaosfisch.google.youtube.playlist.events.PlaylistUpdated;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlaylistDaoImpl implements IPlaylistDao {

	private final ArrayList<Playlist> playlists = new ArrayList<>(10);

	@Inject
	protected EventBus eventBus;

	@Override
	public List<Playlist> getAll(final Account account) {
		final List<Playlist> result = new ArrayList<>(playlists.size());
		for (final Playlist playlist : playlists) {
			if (playlist.getAccount().equals(account)) {
				result.add(playlist);
			}
		}
		return result;
	}

	@Override
	public Playlist get(final String id) {
		for (final Playlist playlist : playlists) {
			if (playlist.getId().equals(id)) {
				return playlist;
			}
		}
		return null;
	}

	@Override
	public List<Playlist> fetchByHidden(final Account account, final boolean hidden) {
		final List<Playlist> result = new ArrayList<>(playlists.size());
		for (final Playlist playlist : playlists) {
			if (playlist.isHidden() == hidden && playlist.getAccount().equals(account)) {
				result.add(playlist);
			}
		}
		return result;
	}

	@Override
	public Playlist fetchByPKey(final String playlistKey) {
		for (final Playlist playlist : playlists) {
			if (playlist.getPkey().equals(playlistKey)) {
				return playlist;
			}
		}
		return null;
	}

	@Override
	public void insert(final Playlist playlist) {
		playlist.setId(UUID.randomUUID().toString());
		playlists.add(playlist);
		eventBus.post(new PlaylistAdded(playlist));
	}

	@Override
	public void update(final Playlist playlist) {
		eventBus.post(new PlaylistUpdated(playlist));
	}

	@Override
	public void delete(final Playlist playlist) {
		playlists.remove(playlist);
		eventBus.post(new PlaylistRemoved(playlist));
	}
}
