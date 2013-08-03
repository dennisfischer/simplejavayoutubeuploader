/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.validation;

import java.io.File;

public class FileSizeValidator implements Validator<File> {

	private final int size;

	public FileSizeValidator(final int size) {
		this.size = size;
	}

	@Override
	public boolean validate(final File object) {
		return null == object || object.exists() && object.length() <= size;
	}
}
