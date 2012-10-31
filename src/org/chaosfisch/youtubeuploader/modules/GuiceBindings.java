/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.modules;

import java.sql.SQLException;

import javafx.stage.FileChooser;

import javax.sql.DataSource;

import org.chaosfisch.google.auth.GDataRequestSigner;
import org.chaosfisch.google.auth.RequestSigner;
import org.chaosfisch.util.AuthTokenHelper;
import org.chaosfisch.util.io.RequestHelper;
import org.chaosfisch.youtubeuploader.APIData;
import org.chaosfisch.youtubeuploader.services.uploader.Uploader;
import org.chaosfisch.youtubeuploader.services.youtube.impl.CategoryServiceImpl;
import org.chaosfisch.youtubeuploader.services.youtube.impl.MetadataServiceImpl;
import org.chaosfisch.youtubeuploader.services.youtube.impl.PlaylistServiceImpl;
import org.chaosfisch.youtubeuploader.services.youtube.spi.CategoryService;
import org.chaosfisch.youtubeuploader.services.youtube.spi.MetadataService;
import org.chaosfisch.youtubeuploader.services.youtube.spi.PlaylistService;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.mchange.v2.c3p0.DataSources;

public class GuiceBindings extends AbstractModule
{

	@Override
	protected void configure()
	{
		bind(CategoryService.class).to(CategoryServiceImpl.class).in(Singleton.class);
		bind(PlaylistService.class).to(PlaylistServiceImpl.class).in(Singleton.class);
		bind(MetadataService.class).to(MetadataServiceImpl.class).in(Singleton.class);
		bind(FileChooser.class).in(Singleton.class);
		bind(RequestSigner.class).to(GDataRequestSigner.class).in(Singleton.class);
		bind(Uploader.class).in(Singleton.class);
		bind(AuthTokenHelper.class).in(Singleton.class);

		requestStaticInjection(RequestHelper.class);

		bind(String.class).annotatedWith(Names.named("GDATA_VERSION")).toInstance("2");
		bind(String.class).annotatedWith(Names.named("DEVELOPER_KEY")).toInstance(APIData.DEVELOPER_KEY);

		try
		{
			bind(DataSource.class).toInstance(
					DataSources.pooledDataSource(DataSources.unpooledDataSource("jdbc:h2:~SimpleJavaYoutubeUploader/youtubeuploader", "", "")));
		} catch (final SQLException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
}
