/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.uploader.project;

import javafx.beans.property.SimpleStringProperty;

public class ProjectModel {

	private final SimpleStringProperty name = new SimpleStringProperty();

	public ProjectModel(final String name) {
		this.name.set(name);
	}

	public SimpleStringProperty nameProperty() {
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}

	public String getName() {
		return name.get();
	}

	public void setName(final String name) {
		this.name.set(name);
	}
}
