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

import com.google.common.base.Charsets;

public class ByteLengthValidator implements Validator<String> {
	private final int min;
	private final int max;

	public ByteLengthValidator(final int min, final int max) {
		this.min = min;
		this.max = max;
	}

	@Override
	public boolean validate(final String object) {
		return null == object || object.getBytes(Charsets.UTF_8).length >= min && object.getBytes(Charsets.UTF_8).length <= max;
	}

}
