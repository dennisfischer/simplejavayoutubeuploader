/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.upload.metadata;

public class MetaBadRequestException extends Exception {
	private final String atomData;
	private final int    statusCode;

	public MetaBadRequestException(final String atomData, final int statusCode) {
		this.atomData = atomData;
		this.statusCode = statusCode;
	}

	public String getAtomData() {
		return atomData;
	}

	public int getStatusCode() {
		return statusCode;
	}
}
