/*
 * Copyright (c) 2014 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.upload.metadata.permissions;

import de.chaosfisch.util.TextUtil;

public enum ClaimOption {
	MONETIZE("claimoptions.monetize"), TRACK("claimoptions.track"), BLOCK("claimoptions.block");

	private final String i18n;

	ClaimOption(final String i18n) {
		this.i18n = i18n;
	}

	@Override
	public String toString() {
		return TextUtil.getString(i18n);
	}
}
