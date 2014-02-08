/*
 * Copyright (c) 2014 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.gui;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import de.chaosfisch.uploader.gui.controller.UploadController;
import de.chaosfisch.uploader.gui.renderer.DialogHelper;
import javafx.stage.FileChooser;

class GUIModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(FileChooser.class).in(Singleton.class);
		bind(DialogHelper.class).in(Singleton.class);
		bind(UploadController.class).in(Singleton.class);
	}
}
