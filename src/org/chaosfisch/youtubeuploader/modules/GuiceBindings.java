/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.modules;

import javafx.stage.FileChooser;

import org.chaosfisch.youtubeuploader.services.PlaylistService;
import org.chaosfisch.youtubeuploader.services.PlaylistServiceImpl;
import org.chaosfisch.youtubeuploader.services.youtube.impl.CategoryServiceImpl;
import org.chaosfisch.youtubeuploader.services.youtube.spi.CategoryService;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class GuiceBindings extends AbstractModule
{

	@Override
	protected void configure()
	{
		bind(CategoryService.class).to(CategoryServiceImpl.class).in(Singleton.class);
		bind(PlaylistService.class).to(PlaylistServiceImpl.class).in(Singleton.class);
		bind(FileChooser.class).in(Singleton.class);
		// bind(Uploader.class).in(Singleton.class);
	}
}
