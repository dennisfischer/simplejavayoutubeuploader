/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.persistence;

import com.google.inject.Inject;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.auth.IGoogleRequestSigner;
import de.chaosfisch.google.youtube.playlist.AbstractPlaylistService;
import de.chaosfisch.google.youtube.playlist.Playlist;
import de.chaosfisch.http.RequestBuilderFactory;
import de.chaosfisch.serialization.IXmlSerializer;
import de.chaosfisch.uploader.persistence.dao.IPlaylistDao;

import java.util.List;

public class PlaylistServiceImpl extends AbstractPlaylistService {

	private final IPlaylistDao playlistDao;

	@Inject
	public PlaylistServiceImpl(final IGoogleRequestSigner requestSigner, final RequestBuilderFactory requestBuilderFactory, final IXmlSerializer xmlSerializer, final IPlaylistDao playlistDao) {
		super(requestSigner, requestBuilderFactory, xmlSerializer);
		this.playlistDao = playlistDao;
	}

	@Override
	public List<Playlist> getAll(final Account account) {
		return playlistDao.getAll(account);
	}

	@Override
	public Playlist get(final int id) {
		return playlistDao.get(id);
	}

	@Override
	public void insert(final Playlist playlist) {
		playlistDao.insert(playlist);
	}

	@Override
	public void update(final Playlist playlist) {
		playlistDao.update(playlist);
	}

	@Override
	public void delete(final Playlist playlist) {
		playlistDao.delete(playlist);
	}

	@Override
	public void cleanByAccount(final Account account) {
		playlistDao.cleanByAccount(account);
	}

	@Override
	public List<Playlist> findByHidden(final Account account, final boolean hidden) {
		return playlistDao.fetchByHidden(account, hidden);
	}

	@Override
	public Playlist findByPkey(final String playlistKey) {
		return playlistDao.fetchByPKey(playlistKey);
	}

	@Override
	public List<Playlist> fetchUnhiddenByAccount(final Account account) {
		return findByHidden(account, false);
	}
}
