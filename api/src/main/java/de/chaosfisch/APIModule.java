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
import de.chaosfisch.data.upload.metadata.IMetadataDAO;
import de.chaosfisch.data.upload.metadata.MetadataDAO;
import de.chaosfisch.data.upload.monetization.IMonetizationDAO;
import de.chaosfisch.data.upload.monetization.MonetizationDAO;
import de.chaosfisch.data.upload.permission.IPermissionDAO;
import de.chaosfisch.data.upload.permission.PermissionDAO;
import de.chaosfisch.data.upload.social.ISocialDAO;
import de.chaosfisch.data.upload.social.SocialDAO;
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

import javax.inject.Singleton;

@Module(
		complete = false,
		library = true
)
public class APIModule {

	@Provides
	@Singleton
	IAccountDAO provideAccountDAO() {
		return new AccountDAO(new CookieDAO(), new FieldDAO());
	}

	@Provides
	@Singleton
	IPlaylistDAO providePlaylistDAO() {
		return new PlaylistDAO();
	}

	@Provides
	@Singleton
	ISocialDAO provideSocialDAO() {
		return new SocialDAO();
	}

	@Provides
	@Singleton
	IMonetizationDAO provideMonetizationDAO() {
		return new MonetizationDAO();
	}

	@Provides
	@Singleton
	IMetadataDAO provideMetadataDAO() {
		return new MetadataDAO();
	}

	@Provides
	@Singleton
	IPermissionDAO providePermissionDAO() {
		return new PermissionDAO();
	}

	@Provides
	@Singleton
	IUploadDAO provideUploadDAO(final IPlaylistDAO playlistDAO, final ISocialDAO socialDAO, final IPermissionDAO permissionDAO, final IMetadataDAO metadataDAO, final IMonetizationDAO monetizationDAO) {
		return new UploadDAO(playlistDAO, socialDAO, permissionDAO, metadataDAO, monetizationDAO);
	}

	@Provides
	@Singleton
	ICategoryDAO provideCategoryDAO() {
		return new CategoryDAO();
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
	IUploadService provideUploadService(final IUploadDAO uploadDAO, final IMetadataService iMetadataService, final ICategoryService iCategoryService) {
		return new YouTubeUploadService(uploadDAO, iMetadataService, iCategoryService);
	}
}
