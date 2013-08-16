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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public interface IPersistenceService {
	String PERSISTENCE_FOLDER = "PERSISTENCE_FOLDER";

	void saveToStorage();

	void loadFromStorage();

	class Data implements Serializable {
		private static final long serialVersionUID = -7729985568529356434L;
		List<Upload>   uploads   = new ArrayList<>(0);
		List<Template> templates = new ArrayList<>(0);
		List<Playlist> playlists = new ArrayList<>(0);
		List<Account>  accounts  = new ArrayList<>(0);
		int version;
	}
}
