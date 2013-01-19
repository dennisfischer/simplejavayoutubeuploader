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

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.validation.ValidatorAdapter;

public class FileSizeValidator extends ValidatorAdapter<Model> {

	private final String	stringToValidate;
	private final int		size;

	public FileSizeValidator(final String stringToValidate, final int size) {
		this.stringToValidate = stringToValidate;
		this.size = size;
	}

	@Override
	public void validate(final Model m) {
		final File file = new File(m.getString(stringToValidate));
		if (file.exists() && file.length() > size) {
			m.addValidator(this, "file_size_error");
		}
	}

}
