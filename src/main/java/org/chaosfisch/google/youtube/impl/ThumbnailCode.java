/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.google.youtube.impl;

import org.chaosfisch.exceptions.ErrorCode;

public enum ThumbnailCode implements ErrorCode {
	FILE_NOT_FOUND(101), UPLOAD_RESPONSE(102), UPLOAD_JSON(103);
	private final int number;

	ThumbnailCode(final int number) {
		this.number = number;
	}

	@Override
	public int getNumber() {
		return number;
	}
}
