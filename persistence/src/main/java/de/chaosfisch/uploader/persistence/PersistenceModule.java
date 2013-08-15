/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

/*
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 23.06.13
 * Time: 16:16
 */
package de.chaosfisch.uploader.persistence;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.persist.jpa.JpaPersistModule;
import de.chaosfisch.google.account.IAccountService;
import de.chaosfisch.google.youtube.playlist.IPlaylistService;
import de.chaosfisch.google.youtube.upload.IUploadService;
import de.chaosfisch.uploader.persistence.dao.DaoModule;
import de.chaosfisch.uploader.template.ITemplateService;

public class PersistenceModule extends AbstractModule {
	private static final String PERSISTENCE_UNIT_NAME = "persistenceUnit";

	@Override
	protected void configure() {
		install(new JpaPersistModule(PERSISTENCE_UNIT_NAME));
		bind(MyInitializer.class).asEagerSingleton();
		install(new DaoModule());
		bind(IAccountService.class).to(AccountServiceImpl.class).in(Singleton.class);
		bind(IUploadService.class).to(UploadServiceImpl.class).in(Singleton.class);
		bind(IPlaylistService.class).to(PlaylistServiceImpl.class).in(Singleton.class);
		bind(ITemplateService.class).to(TemplateServiceImpl.class).in(Singleton.class);
	}
}
