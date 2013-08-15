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

import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.youtube.playlist.Playlist;

import java.util.List;

public interface IPlaylistDao {
	List<Playlist> getAll(Account account);

	Playlist get(String id);

	void insert(Playlist playlist);

	void update(Playlist playlist);

	void delete(Playlist playlist);

	List<Playlist> fetchByHidden(Account account, boolean hidden);

	Playlist fetchByPKey(String playlistKey);
}
