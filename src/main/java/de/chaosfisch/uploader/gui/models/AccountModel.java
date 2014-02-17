/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.uploader.gui.models;

import de.chaosfisch.google.account.AccountType;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;

public class AccountModel {
	private final SimpleStringProperty name = new SimpleStringProperty();
	private final SimpleObjectProperty<AccountType> type = new SimpleObjectProperty<>();
	private final SimpleListProperty<PlaylistModel> playlists = new SimpleListProperty<>();

	public String getName() {
		return name.get();
	}

	public SimpleStringProperty nameProperty() {
		return name;
	}

	public void setName(final String name) {
		this.name.set(name);
	}

	public AccountType getType() {
		return type.get();
	}

	public SimpleObjectProperty<AccountType> typeProperty() {
		return type;
	}

	public void setType(final AccountType type) {
		this.type.set(type);
	}

	public ObservableList<PlaylistModel> getPlaylists() {
		return playlists.get();
	}

	public SimpleListProperty<PlaylistModel> playlistsProperty() {
		return playlists;
	}

	public void setPlaylists(final ObservableList<PlaylistModel> playlists) {
		this.playlists.set(playlists);
	}

	@Override
	public String toString() {
		return getName();
	}
}
