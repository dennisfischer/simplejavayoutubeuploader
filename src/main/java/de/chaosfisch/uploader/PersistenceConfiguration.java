/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader;

import de.chaosfisch.uploader.persistence.IConfiguration;

public class PersistenceConfiguration implements IConfiguration {

	private final String home;
	private final String tempDir;
	private final String schemaName;
	private final String schemaLocation;

	public PersistenceConfiguration(final String home, final String tempDir, final String schemaName, final String schemaLocation) {
		this.home = home;
		this.tempDir = tempDir;
		this.schemaName = schemaName;
		this.schemaLocation = schemaLocation;
	}

	@Override
	public String getHome() {
		return home;
	}

	@Override
	public String getTempDir() {
		return tempDir;
	}

	@Override
	public String getSchemaName() {
		return schemaName;
	}

	@Override
	public String getSchemaLocation() {
		return schemaLocation;
	}
}
