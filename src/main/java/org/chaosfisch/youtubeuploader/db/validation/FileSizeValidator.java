/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader.db.validation;

import java.io.File;

public class FileSizeValidator implements Validator<String> {

	private final int size;

	public FileSizeValidator(final int size) {
		this.size = size;
	}

	@Override
	public boolean validate(final String object) {
		if (null != object && !object.isEmpty()) {
			final File file = new File(object);
			return file.exists() && file.length() <= size;
		}
		return true;
	}
}
