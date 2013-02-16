/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.sql.DataSource;

import jfxtras.labs.dialogs.MonologFX;
import jfxtras.labs.dialogs.MonologFXButton;

import org.chaosfisch.util.LogfileCommitter;
import org.chaosfisch.youtubeuploader.guice.GuiceBindings;
import org.chaosfisch.youtubeuploader.guice.GuiceControllerFactory;
import org.chaosfisch.youtubeuploader.models.Setting;
import org.chaosfisch.youtubeuploader.models.Template;
import org.chaosfisch.youtubeuploader.models.Upload;
import org.chaosfisch.youtubeuploader.services.youtube.uploader.Uploader;
import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.javafx.PlatformUtil;

public class SimpleJavaYoutubeUploader extends Application {

	/**
	 * The application DI injector
	 */
	Injector		injector;
	final Logger	logger	= LoggerFactory.getLogger(SimpleJavaYoutubeUploader.class);

	public static void main(final String[] args) {
		launch(args);
	}

	private void initUpdater() {
		new ApplicationUpdater();
	}

	private void initSavedir() {
		String userHome = System.getProperty("user.home");
		if (PlatformUtil.isMac()) {
			userHome += "/Library/Application Support/";
		}
		System.setProperty("user.home", userHome);

	}

	private void initLocale() {
		final Locale[] availableLocales = { Locale.GERMANY, Locale.GERMAN, Locale.ENGLISH };
		if (!Arrays.asList(availableLocales).contains(Locale.getDefault())) {
			Locale.setDefault(Locale.ENGLISH);
		}
	}

	private void initLogger() {
		System.setOut(new PrintStream(System.out) {
			@Override
			public void print(final String s) {
				logger.info(s);
			}
		});
		System.setErr(new PrintStream(System.err) {
			@Override
			public void print(final String s) {
				logger.error(s);
			}
		});
	}

	private Uploader	uploader;

	protected void initApplication(final Stage primaryStage) throws IOException {
		initLogger();
		initLocale();
		initSavedir();
		initUpdater();
		injector = Guice.createInjector(new GuiceBindings());
		initDatabase();
		updateDatabase();

		uploader = injector.getInstance(Uploader.class);

		final FXMLLoader fxLoader = new FXMLLoader(getClass().getResource(
				"/org/chaosfisch/youtubeuploader/view/SimpleJavaYoutubeUploader.fxml"), I18nHelper.getResourceBundle());
		fxLoader.setControllerFactory(new GuiceControllerFactory(injector));
		fxLoader.load();
		final Scene scene = new Scene((Parent) fxLoader.getRoot(), 1000, 640);
		scene.getStylesheets().add(getClass().getResource("/org/chaosfisch/youtubeuploader/resources/style.css").toExternalForm());
		primaryStage.setTitle(I18nHelper.message("application.title"));
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/org/chaosfisch/youtubeuploader/resources/images/film.png")));
		primaryStage.setScene(scene);
		primaryStage.setMinHeight(640);
		primaryStage.setMinWidth(1000);
		primaryStage.setHeight(640);
		primaryStage.setWidth(1000);
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(final WindowEvent event) {
				final MonologFX dialog = new MonologFX(MonologFX.Type.QUESTION);
				final MonologFXButton yesButton = new MonologFXButton();
				yesButton.setType(MonologFXButton.Type.YES);
				yesButton.setLabel("Yes");
				final MonologFXButton noButton = new MonologFXButton();
				noButton.setType(MonologFXButton.Type.NO);
				noButton.setLabel("No");
				dialog.addButton(yesButton);
				dialog.addButton(noButton);
				dialog.setTitleText(I18nHelper.message("dialog.exitapplication.title"));
				dialog.setMessage(I18nHelper.message("dialog.exitapplication.message"));
				if (dialog.showDialog() == MonologFXButton.Type.NO) {
					event.consume();
				}
			}
		});
		primaryStage.show();
	}

	private void updateDatabase() {
		boolean updated = false;
		if (!Setting.getMetaModel().getColumnMetadata().containsKey("key")) {
			Base.openTransaction();
			Base.exec("DROP TABLE SETTINGS");
			Base.exec("DROP TABLE PLACEHOLDERS");
			Base.exec("CREATE TABLE IF NOT EXISTS SETTINGS(id INTEGER NOT NULL auto_increment PRIMARY KEY, `KEY` VARCHAR(255) NOT NULL UNIQUE, VALUE VARCHAR(255), created_at DATETIME, updated_at DATETIME);");
			Base.commitTransaction();
			updated = true;
		}
		if (!Template.getMetaModel().getColumnMetadata().containsKey("thumbnail")) {
			Base.openTransaction();
			Base.exec("ALTER TABLE TEMPLATES ADD thumbnail VARCHAR(255)");
			Base.commitTransaction();
			updated = true;
		}
		if (!Upload.getMetaModel().getColumnMetadata().containsKey("thumbnail")) {
			Base.openTransaction();
			Base.exec("ALTER TABLE UPLOADS ADD thumbnail VARCHAR(255)");
			Base.commitTransaction();
			updated = true;
		}

		if (!Upload.getMetaModel().getColumnMetadata().containsKey("facebook")) {
			Base.openTransaction();
			Base.exec("ALTER TABLE UPLOADS ADD facebook BOOLEAN");
			Base.exec("ALTER TABLE UPLOADS ADD twitter BOOLEAN");
			Base.exec("ALTER TABLE UPLOADS ADD message VARCHAR(5000)");
			Base.commitTransaction();
			updated = true;
		}

		if (!Template.getMetaModel().getColumnMetadata().containsKey("facebook")) {
			Base.openTransaction();
			Base.exec("ALTER TABLE TEMPLATES ADD facebook BOOLEAN");
			Base.exec("ALTER TABLE TEMPLATES ADD twitter BOOLEAN");
			Base.exec("ALTER TABLE TEMPLATES ADD message VARCHAR(5000)");
			Base.commitTransaction();
			updated = true;
		}

		if (!Upload.getMetaModel().getColumnMetadata().containsKey("instreamdefaults")) {
			Base.openTransaction();
			Base.exec("ALTER TABLE UPLOADS ADD instreamDefaults BOOLEAN");
			Base.exec("ALTER TABLE UPLOADS ADD CLAIM BOOLEAN");
			Base.exec("ALTER TABLE UPLOADS ADD OVERLAY BOOLEAN");
			Base.exec("ALTER TABLE UPLOADS ADD TRUEVIEW BOOLEAN");
			Base.exec("ALTER TABLE UPLOADS ADD INSTREAM BOOLEAN");
			Base.exec("ALTER TABLE UPLOADS ADD PRODUCT BOOLEAN");
			Base.exec("ALTER TABLE UPLOADS ADD SYNDICATION INTEGER");
			Base.exec("ALTER TABLE UPLOADS ADD monetizeTitle VARCHAR(255)");
			Base.exec("ALTER TABLE UPLOADS ADD monetizeDescription VARCHAR(255)");
			Base.exec("ALTER TABLE UPLOADS ADD monetizeID VARCHAR(255)");
			Base.exec("ALTER TABLE UPLOADS ADD monetizeNotes VARCHAR(255)");
			Base.exec("ALTER TABLE UPLOADS ADD monetizeTMSID VARCHAR(255)");
			Base.exec("ALTER TABLE UPLOADS ADD monetizeISAN VARCHAR(255)");
			Base.exec("ALTER TABLE UPLOADS ADD monetizeEIDR VARCHAR(255)");
			Base.exec("ALTER TABLE UPLOADS ADD monetizeTitleEpisode VARCHAR(255)");
			Base.exec("ALTER TABLE UPLOADS ADD monetizeSeasonNB VARCHAR(255)");
			Base.exec("ALTER TABLE UPLOADS ADD monetizeEpisodeNB VARCHAR(255)");
			Base.exec("ALTER TABLE UPLOADS ADD monetizeClaimType INTEGER");
			Base.exec("ALTER TABLE UPLOADS ADD monetizeClaimPolicy INTEGER");
			Base.exec("ALTER TABLE UPLOADS ADD monetizeAsset INTEGER");
			Base.exec("ALTER TABLE UPLOADS ADD monetizePartner BOOLEAN");
			Base.commitTransaction();
			updated = true;
		}

		if (updated) {
			databaseUpdatedDialog();
		}
	}

	private void databaseUpdatedDialog() {
		final MonologFX dialog = new MonologFX(MonologFX.Type.INFO);
		dialog.setTitleText(I18nHelper.message("dialog.databaseupdated.title"));
		dialog.setMessage(I18nHelper.message("dialog.databaseupdated.message"));
		final MonologFXButton okButton = new MonologFXButton();
		okButton.setType(MonologFXButton.Type.OK);
		okButton.setLabel("Ok");
		dialog.addButton(okButton);
		dialog.showDialog();
		System.exit(0);
	}

	private void initDatabase() {
		Base.open(injector.getInstance(DataSource.class));
		try {
			Base.connection().setAutoCommit(true);
		} catch (final SQLException e) {
			logger.error("Database setAutoCommit failed!", e);
		}
		Base.openTransaction();
		Base.exec("CREATE TABLE IF NOT EXISTS ACCOUNTS(ID INTEGER NOT NULL auto_increment PRIMARY KEY,NAME VARCHAR(255),PASSWORD VARCHAR(255), TYPE VARCHAR(255), created_at DATETIME, updated_at DATETIME);");
		Base.exec("CREATE TABLE IF NOT EXISTS TEMPLATES(ID INTEGER NOT NULL auto_increment PRIMARY KEY,CATEGORY VARCHAR(255),COMMENT SMALLINT,COMMENTVOTE BOOLEAN, DEFAULTDIR VARCHAR(255),DESCRIPTION VARCHAR(16777216),EMBED BOOLEAN,KEYWORDS VARCHAR(16777216),MOBILE BOOLEAN,NAME VARCHAR(255),NUMBER SMALLINT,RATE BOOLEAN,VIDEORESPONSE SMALLINT,VISIBILITY SMALLINT,ACCOUNT_ID INTEGER,ENDDIR VARCHAR(255),LICENSE SMALLINT, created_at DATETIME, updated_at DATETIME, TITLE VARCHAR(255), thumbnail VARCHAR(255), facebook BOOLEAN, twitter BOOLEAN, message VARCHAR(5000));");
		Base.exec("CREATE TABLE IF NOT EXISTS PLAYLISTS(ID INTEGER NOT NULL auto_increment PRIMARY KEY,PKEY VARCHAR(255), PRIVATE BOOLEAN, TITLE VARCHAR(255),URL VARCHAR(255),THUMBNAIL VARCHAR(255), NUMBER INTEGER, SUMMARY VARCHAR(16777216), ACCOUNT_ID INTEGER, created_at DATETIME, updated_at DATETIME);");
		Base.exec("CREATE TABLE IF NOT EXISTS UPLOADS(ID INTEGER NOT NULL auto_increment PRIMARY KEY,ARCHIVED BOOLEAN,CATEGORY VARCHAR(255),COMMENT SMALLINT,COMMENTVOTE BOOLEAN,DESCRIPTION VARCHAR(16777216),EMBED BOOLEAN,FAILED BOOLEAN,FILE VARCHAR(500),VISIBILITY SMALLINT,KEYWORDS VARCHAR(16777216),MIMETYPE VARCHAR(255),MOBILE BOOLEAN,RATE BOOLEAN,TITLE VARCHAR(255),UPLOADURL VARCHAR(255),VIDEORESPONSE SMALLINT,STARTED TIMESTAMP,INPROGRESS BOOLEAN,LOCKED BOOLEAN,VIDEOID VARCHAR(255),ACCOUNT_ID INTEGER, ENDDIR VARCHAR(255), LICENSE SMALLINT, RELEASE TIMESTAMP,NUMBER SMALLINT, PAUSEONFINISH BOOLEAN, created_at DATETIME, updated_at DATETIME, thumbnail VARCHAR(255), facebook BOOLEAN, twitter BOOLEAN, message VARCHAR(5000));");
		Base.exec("CREATE TABLE IF NOT EXISTS UPLOADS_PLAYLISTS(id INTEGER NOT NULL auto_increment PRIMARY KEY, playlist_id INTEGER, upload_id INTEGER);");
		Base.exec("CREATE TABLE IF NOT EXISTS TEMPLATES_PLAYLISTS(id INTEGER NOT NULL auto_increment PRIMARY KEY, playlist_id INTEGER, template_id INTEGER);");
		Base.exec("CREATE TABLE IF NOT EXISTS SETTINGS(id INTEGER NOT NULL auto_increment PRIMARY KEY, `KEY` VARCHAR(255) NOT NULL UNIQUE, VALUE VARCHAR(255), created_at DATETIME, updated_at DATETIME);");
		Upload.updateAll("inprogress = ?", false);
		Base.commitTransaction();
	}

	@Override
	public void start(final Stage primaryStage) {
		try {
			Platform.setImplicitExit(true);
			initApplication(primaryStage);
			uploader.runStarttimeChecker();
		} catch (final IOException e) {
			logger.error("Couldn't start the application", e);
		}
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		LogfileCommitter.commit();
		uploader.stopStarttimeChecker();
		uploader.exit();
	}
}
