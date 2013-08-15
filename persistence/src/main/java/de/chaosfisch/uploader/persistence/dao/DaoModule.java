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
 * Time: 21:36
 */
package de.chaosfisch.uploader.persistence.dao;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class DaoModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(IPersistenceService.class).to(PersistenceService.class).in(Singleton.class);
		bind(IAccountDao.class).to(AccountDaoImpl.class).in(Singleton.class);
		bind(IPlaylistDao.class).to(PlaylistDaoImpl.class).in(Singleton.class);
		bind(ITemplateDao.class).to(TemplateDaoImpl.class).in(Singleton.class);
		bind(IUploadDao.class).to(UploadDaoImpl.class).in(Singleton.class);
	}
}
