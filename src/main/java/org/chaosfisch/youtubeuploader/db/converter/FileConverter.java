/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader.db.converter;

import org.jooq.Converter;

import java.io.File;

public class FileConverter implements Converter<String, File> {

	private static final long serialVersionUID = 1335558703543045626L;

	@Override
	public File from(final String databaseObject) {
		if (databaseObject == null) {
			return null;
		}
		return new File(databaseObject);
	}

	@Override
	public String to(final File userObject) {
		return userObject == null ? null : userObject.getAbsolutePath();
	}

	@Override
	public Class<String> fromType() {
		return String.class;
	}

	@Override
	public Class<File> toType() {
		return File.class;
	}
}
