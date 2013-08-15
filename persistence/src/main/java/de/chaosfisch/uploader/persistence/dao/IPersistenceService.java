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
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.uploader.template.Template;

public interface IPersistenceService {
	String PERSISTENCE_FOLDER = "PERSISTENCE_FOLDER";

	void saveToStorage();

	void loadFromStorage();

	class Data {
		Upload[]   uploads   = new Upload[0];
		Template[] templates = new Template[0];
		Playlist[] playlists = new Playlist[0];
		Account[]  accounts  = new Account[0];
		int version;
	}
}
