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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.youtube.playlist.Playlist;
import de.chaosfisch.google.youtube.playlist.events.PlaylistAdded;
import de.chaosfisch.google.youtube.playlist.events.PlaylistRemoved;
import de.chaosfisch.google.youtube.playlist.events.PlaylistUpdated;
import de.chaosfisch.uploader.persistence.dao.transactional.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public class PlaylistDaoImpl implements IPlaylistDao {

	private final ArrayListMultimap<Account, Playlist> playlists = ArrayListMultimap.create();

	@Inject
	protected EntityManager entityManager;
	@Inject
	protected EventBus      eventBus;

	@Override
	public List<Playlist> getAll(final Account account) {
		final List<Playlist> result = entityManager.createQuery("SELECT p FROM playlist p WHERE p.account = :account", Playlist.class)
				.setParameter("account", account)
				.getResultList();

		for (final Playlist playlist : result) {
			addOrUpdatePlaylist(playlist);
		}
		return playlists.get(account);
	}

	@Override
	public Playlist get(final int id) {
		final Playlist playlist = entityManager.find(Playlist.class, id);
		addOrUpdatePlaylist(playlist);
		return getFromPlaylistList(playlist);
	}

	@Override
	public List<Playlist> fetchByHidden(final Account account, final boolean hidden) {
		final List<Playlist> result = entityManager.createQuery("SELECT p FROM playlist p WHERE p.hidden = :hidden AND p.account = :account", Playlist.class)
				.setParameter("hidden", hidden)
				.setParameter("account", account)
				.getResultList();

		final ArrayList<Playlist> tmp = new ArrayList<>(result.size());
		for (final Playlist playlist : result) {
			addOrUpdatePlaylist(playlist);
			tmp.add(getFromPlaylistList(playlist));
		}
		return tmp;
	}

	@Override
	public Playlist fetchByPKey(final String playlistKey) {
		final Playlist playlist = entityManager.createQuery("SELECT p FROM playlist p WHERE p.pkey = :pkey", Playlist.class)
				.setParameter("pkey", playlistKey)
				.getSingleResult();
		addOrUpdatePlaylist(playlist);
		return getFromPlaylistList(playlist);
	}

	@Override
	@Transactional
	public void insert(final Playlist playlist) {
		entityManager.persist(playlist);
		playlists.put(playlist.getAccount(), playlist);
		eventBus.post(new PlaylistAdded(playlist));
	}

	@Override
	@Transactional
	public void update(final Playlist playlist) {
		entityManager.merge(playlist);
		eventBus.post(new PlaylistUpdated(playlist));
	}

	@Override
	@Transactional
	public void delete(final Playlist playlist) {
		entityManager.remove(playlist);
		playlists.remove(playlist.getAccount(), playlist);
		eventBus.post(new PlaylistRemoved(playlist));
	}

	private void addOrUpdatePlaylist(final Playlist playlist) {
		if (playlists.containsEntry(playlist.getAccount(), playlist)) {
			refreshPlaylist(playlist);
		} else {
			playlists.put(playlist.getAccount(), playlist);
		}
	}

	private void refreshPlaylist(final Playlist playlist) {
		entityManager.refresh(getFromPlaylistList(playlist));
	}

	private Playlist getFromPlaylistList(final Playlist playlist) {
		return playlists.get(playlist.getAccount()).get(playlists.get(playlist.getAccount()).indexOf(playlist));
	}
}
