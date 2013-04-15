/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */
package org.chaosfisch.youtubeuploader.services.impl;

import org.chaosfisch.exceptions.ErrorCode;

public enum MetadataCode implements ErrorCode {
	BAD_REQUEST(201), LOCATION_MISSING(202), REQUEST_IO_ERROR(203), DEAD_END(204)

	;

	private final int	number;

	private MetadataCode(final int number) {
		this.number = number;
	}

	@Override
	public int getNumber() {
		return number;
	}

}
