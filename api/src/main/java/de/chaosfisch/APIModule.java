/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch;

import dagger.Module;
import dagger.Provides;
import de.chaosfisch.data.account.AccountDAO;
import de.chaosfisch.data.category.CategoryDAO;
import de.chaosfisch.data.playlist.PlaylistDAO;
import de.chaosfisch.data.upload.UploadDAO;
import de.chaosfisch.youtube.account.IAccountService;
import de.chaosfisch.youtube.account.YouTubeAccountService;
import de.chaosfisch.youtube.category.ICategoryService;
import de.chaosfisch.youtube.category.YouTubeCategoryService;
import de.chaosfisch.youtube.playlist.IPlaylistService;
import de.chaosfisch.youtube.playlist.YouTubePlaylistService;
import de.chaosfisch.youtube.upload.IUploadService;
import de.chaosfisch.youtube.upload.YouTubeUploadService;
import de.chaosfisch.youtube.upload.metadata.IMetadataService;
import de.chaosfisch.youtube.upload.metadata.YouTubeMetadataService;
import org.sormula.Database;
import org.sormula.SormulaException;

import javax.inject.Singleton;

@Module(
		complete = false,
		library = true)
public class APIModule {

	@Provides
	@Singleton
	AccountDAO provideAccountDAO(final Database database) {
		try {
			return new AccountDAO(database);
		} catch (final SormulaException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Provides
	@Singleton
	PlaylistDAO providePlaylistDAO(final Database database) {
		try {
			return new PlaylistDAO(database);
		} catch (final SormulaException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Provides
	@Singleton
	UploadDAO provideUploadDAO(final Database database) {
		try {
			return new UploadDAO(database);
		} catch (final SormulaException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Provides
	@Singleton
	CategoryDAO provideCategoryDAO(final Database database) {
		try {
			return new CategoryDAO(database);
		} catch (final SormulaException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Provides
	@Singleton
	ICategoryService provideCategoryService(final CategoryDAO categoryDAO) {
		return new YouTubeCategoryService(categoryDAO);
	}

	@Provides
	@Singleton
	IAccountService provideAccountService(final AccountDAO accountDAO) {
		return new YouTubeAccountService(accountDAO);
	}

	@Provides
	@Singleton
	IPlaylistService providePlaylistService(final PlaylistDAO playlistDAO, final IAccountService accountService) {
		return new YouTubePlaylistService(playlistDAO, accountService.accountModelsProperty());
	}

	@Provides
	@Singleton
	IMetadataService provideMetadataService() {
		return new YouTubeMetadataService();
	}

	@Provides
	@Singleton
	IUploadService provideUploadService(final UploadDAO uploadDAO, final IMetadataService iMetadataService, final ICategoryService iCategoryService) {
		return new YouTubeUploadService(uploadDAO, iMetadataService, iCategoryService);
	}
}
