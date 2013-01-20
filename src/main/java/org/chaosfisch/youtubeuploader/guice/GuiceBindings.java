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

import java.sql.SQLException;
import java.util.Properties;

import javafx.stage.FileChooser;

import javax.sql.DataSource;

import org.chaosfisch.google.auth.GDataRequestSigner;
import org.chaosfisch.google.auth.RequestSigner;
import org.chaosfisch.util.EventBusUtil;
import org.chaosfisch.util.GoogleAuthUtil;
import org.chaosfisch.util.io.Throttle;
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
import org.chaosfisch.youtubeuploader.services.youtube.uploader.Uploader;
import org.chaosfisch.youtubeuploader.view.models.UploadViewModel;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.mchange.v2.c3p0.DataSources;

public class GuiceBindings extends AbstractModule {

	@Override
	protected void configure() {
		bind(CategoryService.class).to(CategoryServiceImpl.class).in(Singleton.class);
		bind(PlaylistService.class).to(PlaylistServiceImpl.class).in(Singleton.class);
		bind(MetadataService.class).to(MetadataServiceImpl.class).in(Singleton.class);
		bind(EnddirService.class).to(EnddirServiceImpl.class).in(Singleton.class);
		bind(ResumeableManager.class).to(ResumeableManagerImpl.class);
		bind(FileChooser.class).in(Singleton.class);
		bind(RequestSigner.class).to(GDataRequestSigner.class).in(Singleton.class);
		bind(Uploader.class).in(Singleton.class);
		bind(GoogleAuthUtil.class).in(Singleton.class);
		bind(Throttle.class).in(Singleton.class);
		bind(UploadController.class).in(Singleton.class);
		bind(UploadViewModel.class).in(Singleton.class);
		bind(EventBus.class).in(Singleton.class);

		requestStaticInjection(EventBusUtil.class);

		bind(String.class).annotatedWith(Names.named("GDATA_VERSION")).toInstance("2");
		bind(String.class).annotatedWith(Names.named("DEVELOPER_KEY")).toInstance(ApplicationData.DEVELOPER_KEY);

		try {
			final Properties p = new Properties(System.getProperties());
			p.put("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
			p.put("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "SEVERE");
			System.setProperties(p);
			bind(DataSource.class).toInstance(
					DataSources.pooledDataSource(DataSources.unpooledDataSource("jdbc:h2:" + System.getProperty("user.home")
							+ "/SimpleJavaYoutubeUploader/youtubeuploader", "username", "")));
		} catch (final SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
