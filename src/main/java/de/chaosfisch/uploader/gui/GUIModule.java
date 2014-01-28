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

import dagger.Module;
import dagger.Provides;
import de.chaosfisch.uploader.gui.edit.EditView;
import de.chaosfisch.uploader.gui.main.MainPresenter;
import de.chaosfisch.uploader.gui.uploads.UploadsView;

@Module(
		injects = MainPresenter.class
)
public class GUIModule {
	@Provides
	EditView provideEditView() {
		return new EditView();
	}

	@Provides
	UploadsView provideUploadsView() {
		return new UploadsView();
	}
}
