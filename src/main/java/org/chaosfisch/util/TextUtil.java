/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.util;

import java.util.ResourceBundle;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class TextUtil {

	@Inject
	@Named("i18n-resources")
	static ResourceBundle	resources;

	public static String getString(final String key) {
		return resources.getString(key);
	}

}
