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
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import de.chaosfisch.uploader.persistence.dao.transactional.Transactional;
import de.chaosfisch.uploader.persistence.dao.transactional.TransactionalIntercepter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class DaoModule extends AbstractModule {
	private static final String PERSISTENCE_UNIT_NAME = "persistenceUnit";

	private static final ThreadLocal<EntityManager> ENTITY_MANAGER_CACHE = new ThreadLocal<>();

	@Override
	protected void configure() {
		bind(IAccountDao.class).to(AccountDaoImpl.class).in(Singleton.class);
		bind(IPlaylistDao.class).to(PlaylistDaoImpl.class).in(Singleton.class);
		bind(ITemplateDao.class).to(TemplateDaoImpl.class).in(Singleton.class);
		bind(IUploadDao.class).to(UploadDaoImpl.class).in(Singleton.class);

		final TransactionalIntercepter transactionalIntercepter = new TransactionalIntercepter();
		requestInjection(transactionalIntercepter);
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(Transactional.class), transactionalIntercepter);
	}

	@Provides
	@Singleton
	public EntityManagerFactory provideEntityManagerFactory() {
		return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
	}

	@Provides
	@Singleton
	public EntityManager provideEntityManager(final EntityManagerFactory entityManagerFactory) {
		EntityManager entityManager = ENTITY_MANAGER_CACHE.get();
		if (null == entityManager) {
			ENTITY_MANAGER_CACHE.set(entityManager = entityManagerFactory.createEntityManager());
		}
		return entityManager;
	}
}
