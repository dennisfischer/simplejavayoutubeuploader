/*
 * Copyright (c) 2014 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.util;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.util.ResourceBundle;

public final class TextUtil {

	@Inject
	@Named("i18n-resources")

	private static ResourceBundle resources;

	private TextUtil() {
	}

	public static String getString(final String key) {
		if (null == resources) {
			return "resourceBundle not bound!";
		}
		return resources.getString(key);
	}
}
