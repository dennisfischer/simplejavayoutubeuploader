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
import de.chaosfisch.google.account.IAccountService;
import de.chaosfisch.google.youtube.playlist.IPlaylistService;
import de.chaosfisch.google.youtube.upload.IUploadService;
import de.chaosfisch.uploader.persistence.dao.DaoModule;
import de.chaosfisch.uploader.template.ITemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class PersistenceModule extends AbstractModule {

	private static final Logger logger = LoggerFactory.getLogger(PersistenceModule.class);
	private final IConfiguration config;

	public PersistenceModule(final IConfiguration config) {
		this.config = config;
	}

	@Override
	protected void configure() {
		install(new DaoModule());
		bind(IAccountService.class).to(AccountServiceImpl.class).in(Singleton.class);
		bind(IUploadService.class).to(UploadServiceImpl.class).in(Singleton.class);
		bind(IPlaylistService.class).to(PlaylistServiceImpl.class).in(Singleton.class);
		bind(ITemplateService.class).to(TemplateServiceImpl.class).in(Singleton.class);

		final File schema = new File(config.getTempDir() + config.getSchemaName());
		try (final InputStream inputStream = getClass().getResourceAsStream(config.getSchemaLocation() + config.getSchemaName())) {
			Files.copy(inputStream, Paths.get(schema.toURI()), StandardCopyOption.REPLACE_EXISTING);
		} catch (final IOException e) {
			logger.error("Couldn't init database", e);
			System.exit(1);
		}
	}
}
