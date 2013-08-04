/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.persistence.dao.converter;

import javax.persistence.AttributeConverter;
import java.io.File;

public class FileToStringConverter implements AttributeConverter<File, String> {
	@Override
	public String convertToDatabaseColumn(final File attribute) {
		return null == attribute ? null : attribute.getAbsolutePath();
	}

	@Override
	public File convertToEntityAttribute(final String dbData) {
		return null == dbData ? null : new File(dbData);
	}
}
