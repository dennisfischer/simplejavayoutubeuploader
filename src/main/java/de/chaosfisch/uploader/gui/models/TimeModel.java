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

public class TimeModel {

	private final SimpleStringProperty time = new SimpleStringProperty();

	public String getTime() {
		return time.get();
	}

	public void setTime(final String time) {
		this.time.set(time);
	}

	public SimpleStringProperty timeProperty() {
		return time;
	}

	@Override
	public String toString() {
		return getTime();
	}
}
