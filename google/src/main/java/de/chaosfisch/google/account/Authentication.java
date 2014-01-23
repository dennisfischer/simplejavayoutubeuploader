/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.account;

public class Authentication {

	private final boolean valid;
	private final String header;

	public Authentication() {
		valid = false;
		header = null;
	}

	public Authentication(final String header) {
		valid = true;
		this.header = header;
	}

	public boolean isValid() {
		return valid;
	}

	public String getHeader() {
		return String.format("Bearer %s", header);
	}
}
