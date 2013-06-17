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
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import javafx.stage.FileChooser;
import org.chaosfisch.google.auth.ClientLogin;
import org.chaosfisch.google.auth.GDataRequestSigner;
import org.chaosfisch.google.auth.IGoogleLogin;
import org.chaosfisch.google.youtube.MetadataService;
import org.chaosfisch.google.youtube.PlaylistService;
import org.chaosfisch.google.youtube.ResumeableManager;
import org.chaosfisch.google.youtube.ThumbnailService;
import org.chaosfisch.google.youtube.impl.MetadataServiceImpl;
import org.chaosfisch.google.youtube.impl.PlaylistServiceImpl;
import org.chaosfisch.google.youtube.impl.ResumeableManagerImpl;
import org.chaosfisch.google.youtube.impl.ThumbnailServiceImpl;
import org.chaosfisch.google.youtube.upload.Uploader;
import org.chaosfisch.http.RequestModule;
import org.chaosfisch.http.RequestSigner;
import org.chaosfisch.serialization.SerializationModule;
import org.chaosfisch.services.EnddirService;
import org.chaosfisch.services.impl.EnddirServiceImpl;
import org.chaosfisch.slf4j.Log;
import org.chaosfisch.slf4j.SLF4JModule;
import org.chaosfisch.util.EventBusUtil;
import org.chaosfisch.util.TextUtil;
import org.chaosfisch.util.io.Throttle;
import org.chaosfisch.youtubeuploader.ApplicationData;
import org.chaosfisch.youtubeuploader.controller.UploadController;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultConnectionProvider;
import org.slf4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.DriverManager;
import java.util.ResourceBundle;

public class GuiceBindings extends AbstractModule {
	private final String dbName;
	@Log
	private       Logger logger;

	public GuiceBindings(final String dbName) {
		this.dbName = dbName;
	}

	@Override
	protected void configure() {
		install(new SLF4JModule());
		install(new RequestModule());
		install(new SerializationModule());
		bind(ResourceBundle.class).annotatedWith(Names.named("i18n-resources"))
				.toInstance(ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.resources.application"));

		mapCommands();
		mapServices();
		mapUtil();

		bind(Uploader.class).in(Singleton.class);
		bind(UploadController.class).in(Singleton.class);

		mapDatabase();
	}

	private void mapDatabase() {
		try {
			final File schema = new File(ApplicationData.HOME + "/SimpleJavaYoutubeUploader/schema.sql");
			try (final InputStream inputStream = getClass().getResourceAsStream("/schema.sql")) {
				Files.copy(inputStream, Paths.get(schema.toURI()), StandardCopyOption.REPLACE_EXISTING);
			}
			final String url = "jdbc:h2:~/SimpleJavaYoutubeUploader/" + dbName + ";INIT=RUNSCRIPT FROM '~/SimpleJavaYoutubeUploader/schema.sql'";

			final DefaultConfiguration configuration = new DefaultConfiguration();
			configuration.set(SQLDialect.H2);
			configuration.set(new DefaultConnectionProvider(DriverManager.getConnection(url, "username", "")));
			bind(Configuration.class).toInstance(configuration);

			DSL.using(configuration)
					.execute("CREATE TRIGGER IF NOT EXISTS ACCOUNT_I AFTER INSERT ON ACCOUNT FOR EACH ROW CALL \"org.chaosfisch.youtubeuploader.db.triggers.AccountTrigger\";\r\n" + "CREATE TRIGGER IF NOT EXISTS ACCOUNT_U AFTER UPDATE ON ACCOUNT FOR EACH ROW CALL \"org.chaosfisch.youtubeuploader.db.triggers.AccountTrigger\";\r\n" + "CREATE TRIGGER IF NOT EXISTS ACCOUNT_D AFTER DELETE ON ACCOUNT FOR EACH ROW CALL \"org.chaosfisch.youtubeuploader.db.triggers.AccountTrigger\";" + "CREATE TRIGGER IF NOT EXISTS PLAYLIST_I AFTER INSERT ON PLAYLIST FOR EACH ROW CALL \"org.chaosfisch.youtubeuploader.db.triggers.PlaylistTrigger\";\r\n" + "CREATE TRIGGER IF NOT EXISTS PLAYLIST_U AFTER UPDATE ON PLAYLIST FOR EACH ROW CALL \"org.chaosfisch.youtubeuploader.db.triggers.PlaylistTrigger\";\r\n" + "CREATE TRIGGER IF NOT EXISTS PLAYLIST_D AFTER DELETE ON PLAYLIST FOR EACH ROW CALL \"org.chaosfisch.youtubeuploader.db.triggers.PlaylistTrigger\";" + "CREATE TRIGGER IF NOT EXISTS TEMPLATE_I AFTER INSERT ON TEMPLATE FOR EACH ROW CALL \"org.chaosfisch.youtubeuploader.db.triggers.TemplateTrigger\";\r\n" + "CREATE TRIGGER IF NOT EXISTS TEMPLATE_U AFTER UPDATE ON TEMPLATE FOR EACH ROW CALL \"org.chaosfisch.youtubeuploader.db.triggers.TemplateTrigger\";\r\n" + "CREATE TRIGGER IF NOT EXISTS TEMPLATE_D AFTER DELETE ON TEMPLATE FOR EACH ROW CALL \"org.chaosfisch.youtubeuploader.db.triggers.TemplateTrigger\";" + "CREATE TRIGGER IF NOT EXISTS UPLOAD_I AFTER INSERT ON UPLOAD FOR EACH ROW CALL \"org.chaosfisch.youtubeuploader.db.triggers.UploadTrigger\";\r\n" + "CREATE TRIGGER IF NOT EXISTS UPLOAD_U AFTER UPDATE ON UPLOAD FOR EACH ROW CALL \"org.chaosfisch.youtubeuploader.db.triggers.UploadTrigger\";\r\n" + "CREATE TRIGGER IF NOT EXISTS UPLOAD_D AFTER DELETE ON UPLOAD FOR EACH ROW CALL \"org.chaosfisch.youtubeuploader.db.triggers.UploadTrigger\";");
		} catch (final Throwable e) {
			logger.error("Couldn't init database", e);
			System.exit(1);
		}
	}

	private void mapUtil() {
		bind(FileChooser.class).in(Singleton.class);
		bind(RequestSigner.class).to(GDataRequestSigner.class).in(Singleton.class);
		bind(IGoogleLogin.class).to(ClientLogin.class).in(Singleton.class);
		bind(Throttle.class).in(Singleton.class);

		final EventBus eventBus = new EventBus();
		bind(EventBus.class).toInstance(eventBus);
		bindListener(Matchers.any(), new TypeListener() {
			@Override
			public <I> void hear(@SuppressWarnings("unused") final TypeLiteral<I> type, final TypeEncounter<I> encounter) {
				encounter.register(new InjectionListener<I>() {
					@Override
					public void afterInjection(final I injectee) {
						eventBus.register(injectee);
					}
				});
			}
		});

		requestStaticInjection(EventBusUtil.class);
		requestStaticInjection(TextUtil.class);
	}

	private void mapServices() {
		bind(PlaylistService.class).to(PlaylistServiceImpl.class).in(Singleton.class);
		bind(MetadataService.class).to(MetadataServiceImpl.class).in(Singleton.class);
		bind(EnddirService.class).to(EnddirServiceImpl.class).in(Singleton.class);
		bind(ThumbnailService.class).to(ThumbnailServiceImpl.class).in(Singleton.class);
		bind(ResumeableManager.class).to(ResumeableManagerImpl.class);
		bind(IGoogleLogin.class).to(ClientLogin.class).in(Singleton.class);
	}

	private void mapCommands() {
		bind(ICommandProvider.class).to(CommandProvider.class);
	}

}
