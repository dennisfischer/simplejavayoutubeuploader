/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.youtubeuploader.db.validation;

import de.chaosfisch.google.youtube.upload.metadata.TagParser;

public class TagValidator implements Validator<String> {

	@Override
	public boolean validate(final String object) {
		return null == object || TagParser.isValid(object);
	}
}
