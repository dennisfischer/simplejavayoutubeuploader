/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader.guice;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import javafx.stage.FileChooser;
import org.chaosfisch.google.auth.GDataRequestSigner;
import org.chaosfisch.google.auth.GoogleAuthUtil;
import org.chaosfisch.io.Throttle;
import org.chaosfisch.io.http.RequestSigner;
import org.chaosfisch.util.EventBusUtil;
import org.chaosfisch.util.TextUtil;
import org.chaosfisch.youtubeuploader.controller.UploadController;
import org.chaosfisch.youtubeuploader.services.*;
import org.chaosfisch.youtubeuploader.services.impl.*;
import org.chaosfisch.youtubeuploader.services.uploader.Uploader;
import org.chaosfisch.youtubeuploader.vo.UploadViewModel;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultConnectionProvider;

import java.sql.DriverManager;
import java.util.ResourceBundle;

public class GuiceBindings extends AbstractModule {
	private final String dbName;

	public GuiceBindings(final String dbName) {
		this.dbName = dbName;
	}

	@Override
	protected void configure() {
		bind(ResourceBundle.class).annotatedWith(Names.named("i18n-resources"))
				.toInstance(ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.resources.application"));

		mapCommands();
		mapServices();
		mapUtil();

		bind(Uploader.class).in(Singleton.class);
		bind(UploadController.class).in(Singleton.class);
		bind(UploadViewModel.class).in(Singleton.class);

		mapDatabase();
	}

	private void mapDatabase() {
		try {
			// ;INIT=RUNSCRIPT FROM '~/create.sql'\\;RUNSCRIPT FROM
			// '~/populate.sql'"
			final String url = "jdbc:h2:" + System.getProperty("user.home") + "/SimpleJavaYoutubeUploader/" + dbName;

			final DefaultConfiguration configuration = new DefaultConfiguration();
			configuration.set(SQLDialect.H2);
			configuration.set(new DefaultConnectionProvider(DriverManager.getConnection(url, "username", "")));
			bind(Configuration.class).toInstance(configuration);

			DSL.using(configuration)
			   .execute("CREATE TRIGGER IF NOT EXISTS ACCOUNT_I AFTER INSERT ON ACCOUNT FOR EACH ROW CALL \"org.chaosfisch.youtubeuploader.db.triggers.AccountTrigger\";\r\n" + "CREATE TRIGGER IF NOT EXISTS ACCOUNT_U AFTER UPDATE ON ACCOUNT FOR EACH ROW CALL \"org.chaosfisch.youtubeuploader.db.triggers.AccountTrigger\";\r\n" + "CREATE TRIGGER IF NOT EXISTS ACCOUNT_D AFTER DELETE ON ACCOUNT FOR EACH ROW CALL \"org.chaosfisch.youtubeuploader.db.triggers.AccountTrigger\";" + "CREATE TRIGGER IF NOT EXISTS PLAYLIST_I AFTER INSERT ON PLAYLIST FOR EACH ROW CALL \"org.chaosfisch.youtubeuploader.db.triggers.PlaylistTrigger\";\r\n" + "CREATE TRIGGER IF NOT EXISTS PLAYLIST_U AFTER UPDATE ON PLAYLIST FOR EACH ROW CALL \"org.chaosfisch.youtubeuploader.db.triggers.PlaylistTrigger\";\r\n" + "CREATE TRIGGER IF NOT EXISTS PLAYLIST_D AFTER DELETE ON PLAYLIST FOR EACH ROW CALL \"org.chaosfisch.youtubeuploader.db.triggers.PlaylistTrigger\";" + "CREATE TRIGGER IF NOT EXISTS TEMPLATE_I AFTER INSERT ON TEMPLATE FOR EACH ROW CALL \"org.chaosfisch.youtubeuploader.db.triggers.TemplateTrigger\";\r\n" + "CREATE TRIGGER IF NOT EXISTS TEMPLATE_U AFTER UPDATE ON TEMPLATE FOR EACH ROW CALL \"org.chaosfisch.youtubeuploader.db.triggers.TemplateTrigger\";\r\n" + "CREATE TRIGGER IF NOT EXISTS TEMPLATE_D AFTER DELETE ON TEMPLATE FOR EACH ROW CALL \"org.chaosfisch.youtubeuploader.db.triggers.TemplateTrigger\";" + "CREATE TRIGGER IF NOT EXISTS UPLOAD_I AFTER INSERT ON UPLOAD FOR EACH ROW CALL \"org.chaosfisch.youtubeuploader.db.triggers.UploadTrigger\";\r\n" + "CREATE TRIGGER IF NOT EXISTS UPLOAD_U AFTER UPDATE ON UPLOAD FOR EACH ROW CALL \"org.chaosfisch.youtubeuploader.db.triggers.UploadTrigger\";\r\n" + "CREATE TRIGGER IF NOT EXISTS UPLOAD_D AFTER DELETE ON UPLOAD FOR EACH ROW CALL \"org.chaosfisch.youtubeuploader.db.triggers.UploadTrigger\";");
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void mapUtil() {
		bind(FileChooser.class).in(Singleton.class);
		bind(RequestSigner.class).to(GDataRequestSigner.class).in(Singleton.class);
		bind(GoogleAuthUtil.class).in(Singleton.class);
		bind(Throttle.class).in(Singleton.class);
		bind(EventBus.class).in(Singleton.class);
		requestStaticInjection(EventBusUtil.class);
		requestStaticInjection(TextUtil.class);
	}

	private void mapServices() {
		bind(PlaylistService.class).to(PlaylistServiceImpl.class).in(Singleton.class);
		bind(MetadataService.class).to(MetadataServiceImpl.class).in(Singleton.class);
		bind(EnddirService.class).to(EnddirServiceImpl.class).in(Singleton.class);
		bind(ThumbnailService.class).to(ThumbnailServiceImpl.class).in(Singleton.class);
		bind(ResumeableManager.class).to(ResumeableManagerImpl.class);
	}

	private void mapCommands() {
		bind(ICommandProvider.class).to(CommandProvider.class);
	}

}
