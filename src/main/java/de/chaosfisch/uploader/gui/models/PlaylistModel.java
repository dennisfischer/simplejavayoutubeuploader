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

import javafx.beans.property.SimpleStringProperty;

public class PlaylistModel {

	private final SimpleStringProperty title = new SimpleStringProperty();

	public String getTitle() {
		return title.get();
	}

	public SimpleStringProperty titleProperty() {
		return title;
	}

	public void setTitle(final String title) {
		this.title.set(title);
	}

	@Override
	public String toString() {
		return getTitle();
	}
}
