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
import de.chaosfisch.data.ObjectDataStore;
import de.chaosfisch.uploader.ApplicationData;
import de.chaosfisch.uploader.gui.account.AccountPresenter;
import de.chaosfisch.uploader.gui.account.AccountView;
import de.chaosfisch.uploader.gui.account.entry.EntryView;
import de.chaosfisch.uploader.gui.edit.EditView;
import de.chaosfisch.uploader.gui.edit.left.EditLeftPresenter;
import de.chaosfisch.uploader.gui.edit.monetization.EditMonetizationView;
import de.chaosfisch.uploader.gui.edit.partner.EditPartnerView;
import de.chaosfisch.uploader.gui.edit.right.EditRightPresenter;
import de.chaosfisch.uploader.gui.main.MainPresenter;
import de.chaosfisch.uploader.gui.project.ProjectPresenter;
import de.chaosfisch.uploader.gui.upload.UploadPresenter;
import de.chaosfisch.uploader.gui.upload.UploadView;
import de.chaosfisch.youtube.category.CategoryModel;
import de.chaosfisch.youtube.category.ICategoryService;
import de.chaosfisch.youtube.category.YouTubeCategoryService;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import javax.inject.Singleton;
import java.io.File;

@Module(
		injects = {MainPresenter.class, EditRightPresenter.class, ProjectPresenter.class, UploadPresenter.class, EditLeftPresenter.class, AccountPresenter.class}
)
public class GUIModule {

	@Provides
	AccountView provideAccountView() {
		return new AccountView();
	}

	@Provides
	EntryView provideEntryView() {
		return new EntryView();
	}

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
	DataModel provideDataModel(final ICategoryService iCategoryService) {
		return new DataModel(iCategoryService);
	}

	@Provides
	@Singleton
	DB provideDB() {
		final DB db = DBMaker.newFileDB(new File(String.format("%s/%s/database.mapdb", ApplicationData.DATA_DIR, ApplicationData.VERSION)))
				.closeOnJvmShutdown()
				.compressionEnable()
				.make();
		db.compact();
		return db;
	}

	@Provides
	@Singleton
	ICategoryService provideCategoryService(final DB db) {
		return new YouTubeCategoryService(new ObjectDataStore<>(db, db.getHashMap("categories"), CategoryModel.class));
	}
}
