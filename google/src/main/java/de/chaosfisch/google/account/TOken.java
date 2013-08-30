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

import java.util.concurrent.TimeUnit;

class Token {
	private final String token;
	private final long   livetime;

	public Token(final String token, final long livetime) {
		this(token, livetime, TimeUnit.SECONDS);
	}

	private Token(final String token, final long livetime, final TimeUnit timeUnit) {
		this.token = token;
		this.livetime = System.currentTimeMillis() + timeUnit.toMillis(livetime);
	}

	public String getToken() {
		return token;
	}

	public boolean isValid() {
		return livetime > System.currentTimeMillis();
	}
}
