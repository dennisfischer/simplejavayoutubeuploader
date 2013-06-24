/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.youtubeuploader.controller.renderer;

import de.chaosfisch.google.account.Account;
import javafx.util.StringConverter;

public final class AccountStringConverter extends StringConverter<Account> {

	@Override
	public String toString(final Account account) {
		return account.getName();
	}

	@Override
	public Account fromString(final String s) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
}