/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.uploader.gui;

import dagger.Module;
import dagger.Provides;
import de.chaosfisch.uploader.gui.edit.EditView;
import de.chaosfisch.uploader.gui.edit.monetization.EditMonetizationView;
import de.chaosfisch.uploader.gui.edit.partner.EditPartnerView;
import de.chaosfisch.uploader.gui.edit.right.EditRightPresenter;
import de.chaosfisch.uploader.gui.main.MainPresenter;
import de.chaosfisch.uploader.gui.project.ProjectPresenter;
import de.chaosfisch.uploader.gui.upload.UploadPresenter;
import de.chaosfisch.uploader.gui.upload.UploadView;

import javax.inject.Singleton;

@Module(
		injects = {MainPresenter.class, EditRightPresenter.class, ProjectPresenter.class, UploadPresenter.class}
)
public class GUIModule {
	@Provides
	EditView provideEditView() {
		return new EditView();
	}

	@Provides
	UploadView provideUploadsView() {
		return new UploadView();
	}

	@Provides
	EditMonetizationView provideEditMonetizationView() {
		return new EditMonetizationView();
	}

	@Provides
	EditPartnerView provideEditPartnerView() {
		return new EditPartnerView();
	}

	@Provides
	@Singleton
	DataModel provideDataModel() {
		return new DataModel();
	}
}
