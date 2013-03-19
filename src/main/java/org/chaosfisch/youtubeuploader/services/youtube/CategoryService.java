/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.services.youtube;

import java.util.List;

import org.chaosfisch.google.atom.AtomCategory;

import com.google.common.util.concurrent.ListenableFuture;

public interface CategoryService {

	/**
	 * Loads all available categorys from youtube
	 * 
	 * @return List<AtomCategory> of loaded - valid - categories
	 */
	ListenableFuture<List<AtomCategory>> load();
}
