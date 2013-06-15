/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.util.http;

import java.io.IOException;

public class HttpIOException extends IOException {

	private final int    statusCode;
	private final String location;
	private final String statusLine;

	public HttpIOException(final int statusCode, final String location, final String statusLine) {
		this(statusCode, location, statusLine, null);
	}

	public HttpIOException(final int statusCode, final String location, final String statusLine, final Throwable throwable) {
		super(String.format("IOException with %d at %s: %s", statusCode, location, statusLine), throwable);
		this.statusCode = statusCode;
		this.location = location;
		this.statusLine = statusLine;
	}

	/** @return int status code */
	public int getStatusCode() {
		return statusCode;
	}

	/** @return String current location */
	public String getLocation() {
		return location;
	}

	/** @return String recevied status line */
	public String getStatusLine() {
		return statusLine;
	}
}
