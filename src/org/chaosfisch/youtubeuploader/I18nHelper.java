/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Dennis Fischer
 ******************************************************************************/

package org.chaosfisch.youtubeuploader;

import java.util.ResourceBundle;

public class I18nHelper
{
	/**
	 * The default language properties file resource bundle
	 */
	static final ResourceBundle	resourceBundle	= ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.resources.application");

	/**
	 * Translates key to user language
	 * 
	 * @param key
	 *            the language key
	 * @return translated key
	 */
	public static String message(final String key)
	{
		return resourceBundle.getString(key);
	}

	/**
	 * Returns the class resourceBundle
	 * 
	 * @return the resourceBundle
	 */
	public static ResourceBundle getResourceBundle()
	{
		return resourceBundle;
	}
}
