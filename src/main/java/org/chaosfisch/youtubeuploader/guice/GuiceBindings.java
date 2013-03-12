/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.guice;

import java.sql.DriverManager;
import java.util.concurrent.Executors;

import javafx.stage.FileChooser;

import org.chaosfisch.google.auth.GDataRequestSigner;
import org.chaosfisch.google.auth.GoogleAuthUtil;
import org.chaosfisch.io.Throttle;
import org.chaosfisch.io.http.RequestSigner;
import org.chaosfisch.util.EventBusUtil;
import org.chaosfisch.youtubeuploader.ApplicationData;
import org.chaosfisch.youtubeuploader.controller.UploadController;
import org.chaosfisch.youtubeuploader.services.youtube.impl.CategoryServiceImpl;
import org.chaosfisch.youtubeuploader.services.youtube.impl.EnddirServiceImpl;
import org.chaosfisch.youtubeuploader.services.youtube.impl.MetadataServiceImpl;
import org.chaosfisch.youtubeuploader.services.youtube.impl.PlaylistServiceImpl;
import org.chaosfisch.youtubeuploader.services.youtube.impl.ResumeableManagerImpl;
import org.chaosfisch.youtubeuploader.services.youtube.spi.CategoryService;
import org.chaosfisch.youtubeuploader.services.youtube.spi.EnddirService;
import org.chaosfisch.youtubeuploader.services.youtube.spi.MetadataService;
import org.chaosfisch.youtubeuploader.services.youtube.spi.PlaylistService;
import org.chaosfisch.youtubeuploader.services.youtube.spi.ResumeableManager;
import org.chaosfisch.youtubeuploader.services.youtube.thumbnail.impl.ThumbnailServiceImpl;
import org.chaosfisch.youtubeuploader.services.youtube.thumbnail.spi.ThumbnailService;
import org.chaosfisch.youtubeuploader.services.youtube.uploader.Uploader;
import org.chaosfisch.youtubeuploader.view.models.UploadViewModel;
import org.jooq.SQLDialect;
import org.jooq.impl.Executor;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

public class GuiceBindings extends AbstractModule {
	private final String	dbName;

	public GuiceBindings(final String dbName) {
		this.dbName = dbName;
	}

	@Override
	protected void configure() {
		bind(CategoryService.class).to(CategoryServiceImpl.class).in(Singleton.class);
		bind(PlaylistService.class).to(PlaylistServiceImpl.class).in(Singleton.class);
		bind(MetadataService.class).to(MetadataServiceImpl.class).in(Singleton.class);
		bind(EnddirService.class).to(EnddirServiceImpl.class).in(Singleton.class);
		bind(ThumbnailService.class).to(ThumbnailServiceImpl.class).in(Singleton.class);
		bind(ResumeableManager.class).to(ResumeableManagerImpl.class);
		bind(FileChooser.class).in(Singleton.class);
		bind(RequestSigner.class).to(GDataRequestSigner.class).in(Singleton.class);
		bind(Uploader.class).in(Singleton.class);
		bind(GoogleAuthUtil.class).in(Singleton.class);
		bind(Throttle.class).in(Singleton.class);
		bind(UploadController.class).in(Singleton.class);
		bind(UploadViewModel.class).in(Singleton.class);
		bind(EventBus.class).in(Singleton.class);

		bind(ListeningExecutorService.class).annotatedWith(Names.named(ApplicationData.SERVICE_EXECUTOR)).toInstance(
			MoreExecutors.listeningDecorator(Executors.newCachedThreadPool()));

		requestStaticInjection(EventBusUtil.class);

		try {
			// ;INIT=RUNSCRIPT FROM '~/create.sql'\\;RUNSCRIPT FROM
			// '~/populate.sql'"
			final String url = "jdbc:h2:" + System.getProperty("user.home") + "/SimpleJavaYoutubeUploader/" + dbName;
			bind(Executor.class).toInstance(new Executor(DriverManager.getConnection(url, "username", ""), SQLDialect.H2));
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
