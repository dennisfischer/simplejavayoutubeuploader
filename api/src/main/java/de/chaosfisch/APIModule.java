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
import de.chaosfisch.data.account.IAccountDAO;
import de.chaosfisch.data.account.cookies.CookieDAO;
import de.chaosfisch.data.account.fields.FieldDAO;
import de.chaosfisch.data.category.CategoryDAO;
import de.chaosfisch.data.category.ICategoryDAO;
import de.chaosfisch.data.playlist.IPlaylistDAO;
import de.chaosfisch.data.playlist.PlaylistDAO;
import de.chaosfisch.data.upload.IUploadDAO;
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
import org.apache.commons.dbutils.QueryRunner;

import javax.inject.Singleton;
import javax.sql.DataSource;

@Module(
		complete = false,
		library = true
)
public class APIModule {

	@Provides
	@Singleton
	QueryRunner proivdeQueryRunner(final DataSource dataSource) {
		return new QueryRunner(dataSource);
	}

	@Provides
	@Singleton
	IAccountDAO provideAccountDAO(final QueryRunner queryRunner) {
		return new AccountDAO(queryRunner, new CookieDAO(queryRunner), new FieldDAO(queryRunner));
	}

	@Provides
	@Singleton
	IPlaylistDAO providePlaylistDAO(final QueryRunner queryRunner) {
		return new PlaylistDAO(queryRunner);
	}

	@Provides
	@Singleton
	IUploadDAO provideUploadDAO(final QueryRunner queryRunner, final IAccountDAO accountDAO, final IPlaylistDAO playlistDAO) {
		return new UploadDAO(queryRunner, accountDAO, playlistDAO);
	}

	@Provides
	@Singleton
	ICategoryDAO provideCategoryDAO(final QueryRunner queryRunner) {
		return new CategoryDAO(queryRunner);
	}

	@Provides
	@Singleton
	ICategoryService provideCategoryService(final ICategoryDAO categoryDAO) {
		return new YouTubeCategoryService(categoryDAO);
	}

	@Provides
	@Singleton
	IAccountService provideAccountService(final IAccountDAO accountDAO) {
		return new YouTubeAccountService(accountDAO);
	}

	@Provides
	@Singleton
	IPlaylistService providePlaylistService(final IPlaylistDAO playlistDAO, final IAccountService accountService) {
		return new YouTubePlaylistService(playlistDAO, accountService.accountModelsProperty());
	}

	@Provides
	@Singleton
	IMetadataService provideMetadataService() {
		return new YouTubeMetadataService();
	}

	@Provides
	@Singleton
	IUploadService provideUploadService(final IUploadDAO uploadDAO, final IMetadataService iMetadataService) {
		return new YouTubeUploadService(uploadDAO, iMetadataService);
	}
}
