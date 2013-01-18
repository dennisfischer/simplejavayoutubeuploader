/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.services.youtube.spi;

import java.util.List;
import java.util.Locale;

import org.chaosfisch.google.atom.AtomCategory;

public interface CategoryService {
	/**
	 * The default category url
	 */
	String	CATEGORY_URL	= "http://gdata.youtube.com/schemas/2007/categories.cat?hl=" + Locale.getDefault().getLanguage();

	/**
	 * Loads all available categorys from youtube
	 * 
	 * @return List<AtomCategory> of loaded - valid - categories
	 */
	List<AtomCategory> load();
}
