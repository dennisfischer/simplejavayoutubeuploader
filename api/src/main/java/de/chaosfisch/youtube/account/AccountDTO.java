/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.account;

import de.chaosfisch.youtube.playlist.PlaylistDTO;

import java.io.Serializable;
import java.util.List;

public class AccountDTO implements Serializable {
	private static final long serialVersionUID = -1970017113691482215L;
	private final String                                         youtubeId;
	private final String                                         name;
	private final String                                         email;
	private final String                                         refreshToken;
	private final AccountType                                    type;
	private final List<PlaylistDTO>                              playlists;
	private final List<String>                                   fields;
	private final List<PersistentCookieStore.SerializableCookie> serializableCookies;

	public AccountDTO(final String youtubeId, final String name, final String email, final String refreshToken, final AccountType type, final List<PlaylistDTO> playlists, final List<String> fields, final List<PersistentCookieStore.SerializableCookie> serializableCookies) {
		this.youtubeId = youtubeId;
		this.name = name;
		this.email = email;
		this.refreshToken = refreshToken;
		this.type = type;
		this.playlists = playlists;
		this.fields = fields;
		this.serializableCookies = serializableCookies;
	}

	public String getYoutubeId() {
		return youtubeId;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public AccountType getType() {
		return type;
	}

	public List<PlaylistDTO> getPlaylists() {
		return playlists;
	}

	public List<String> getFields() {
		return fields;
	}

	public List<PersistentCookieStore.SerializableCookie> getSerializableCookies() {
		return serializableCookies;
	}
}
