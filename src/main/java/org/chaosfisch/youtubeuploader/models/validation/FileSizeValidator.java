/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.models.validation;

import java.io.File;

public class FileSizeValidator {

	private final int	size;

	public FileSizeValidator(final String stringToValidate, final int size) {
		this.size = size;
	}

	public void validate(final String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			return;
		}

		final File file = new File(fileName);
		if (file.exists() && file.length() > size) {
			// TODO m.addValidator(this, "file_size_error");
		}
	}
}
