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
import org.chaosfisch.youtubeuploader.services.youtube.uploader.Uploader;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.javafx.PlatformUtil;

public class SimpleJavaYoutubeUploader extends Application {
	
	/**
	 * The application DI injector
	 */
	final Injector injector = Guice.createInjector(new GuiceBindings());
	final static Logger logger = LoggerFactory
			.getLogger(SimpleJavaYoutubeUploader.class);
	
	public static void main(final String[] args) {
		logger.info("Application started!");
		initLogger();
		initUpdater();
		initLocale();
		initSavedir();
		launch(args);
	}
	
	private static void initUpdater() {
		new ApplicationUpdater();
	}
	
	private static void initSavedir() {
		String userHome = System.getProperty("user.home");
		if (PlatformUtil.isMac()) {
			userHome += "/Library/Application Support/";
		}
		System.setProperty("user.home", userHome);
		
	}
	
	private static void initLocale() {
		final Locale[] availableLocales = {Locale.GERMAN, Locale.ENGLISH};
		if (!Arrays.asList(availableLocales).contains(Locale.getDefault())) {
			// TODO CHANGE THIS TO ENGLISH AS SOON AS TRANSLATED!
			Locale.setDefault(Locale.GERMAN);
		}
	}
	
	private static void initLogger() {
		System.setOut(new PrintStream(System.out) {
			@Override
			public void print(final String s) {
				logger.info(s);
			}
		});
		System.setErr(new PrintStream(System.err) {
			@Override
			public void print(final String s) {
				if (!s.startsWith("WARNING: com.sun.javafx.css.StyleHelper calculateValue")) {
					logger.error(s);
				}
			}
		});
	}
	
	final private Uploader uploader = injector.getInstance(Uploader.class);
	
	protected void initApplication(final Stage primaryStage) throws IOException {
		initDatabase();
		updateDatabase();
		
		final FXMLLoader fxLoader = new FXMLLoader(
				getClass()
						.getResource(
								"/org/chaosfisch/youtubeuploader/view/SimpleJavaYoutubeUploader.fxml"),
				I18nHelper.getResourceBundle());
		fxLoader.setControllerFactory(new GuiceControllerFactory(injector));
		fxLoader.load();
		final Scene scene = new Scene((Parent) fxLoader.getRoot(), 1000, 600);
		scene.getStylesheets().add(
				getClass().getResource(
						"/org/chaosfisch/youtubeuploader/resources/style.css")
						.toExternalForm());
		primaryStage.setTitle(I18nHelper.message("application.title"));
		primaryStage
				.getIcons()
				.add(new Image(
						getClass()
								.getResourceAsStream(
										"/org/chaosfisch/youtubeuploader/resources/images/film.png")));
		primaryStage.setScene(scene);
		primaryStage.setMinHeight(600);
		primaryStage.setMinWidth(1000);
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
				dialog.setTitleText("Exit application");
				dialog.setMessage("Do you really want to exit the application?");
				if (dialog.showDialog() == MonologFXButton.Type.NO) {
					event.consume();
				}
			}
		});
		primaryStage.show();
	}
	
	private void updateDatabase() {
		if (!Model.getMetaModel().getColumnMetadata().containsKey("key")) {
			Base.openTransaction();
			Base.exec("DROP TABLE SETTINGS");
			Base.exec("DROP TABLE PLACEHOLDERS");
			Base.exec("CREATE TABLE IF NOT EXISTS SETTINGS(id INTEGER NOT NULL auto_increment PRIMARY KEY, `KEY` VARCHAR(255) NOT NULL UNIQUE, VALUE VARCHAR(255), created_at DATETIME, updated_at DATETIME);");
			Base.commitTransaction();
			final MonologFX dialog = new MonologFX(MonologFX.Type.INFO);
			dialog.setTitleText("Anwendung neustarten!");
			dialog.setMessage("Die Anwendung muss neu gestartet werden. Die Datenbank wurde aktualisiert!");
			final MonologFXButton okButton = new MonologFXButton();
			okButton.setType(MonologFXButton.Type.OK);
			okButton.setLabel("Ok");
			dialog.addButton(okButton);
			dialog.showDialog();
			System.exit(0);
		}
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
		Base.exec("CREATE TABLE IF NOT EXISTS TEMPLATES(ID INTEGER NOT NULL auto_increment PRIMARY KEY,CATEGORY VARCHAR(255),COMMENT SMALLINT,COMMENTVOTE BOOLEAN, DEFAULTDIR VARCHAR(255),DESCRIPTION VARCHAR(16777216),EMBED BOOLEAN,KEYWORDS VARCHAR(16777216),MOBILE BOOLEAN,NAME VARCHAR(255),NUMBER SMALLINT,RATE BOOLEAN,VIDEORESPONSE SMALLINT,VISIBILITY SMALLINT,ACCOUNT_ID INTEGER,ENDDIR VARCHAR(255),LICENSE SMALLINT, created_at DATETIME, updated_at DATETIME, TITLE VARCHAR(255));");
		Base.exec("CREATE TABLE IF NOT EXISTS PLAYLISTS(ID INTEGER NOT NULL auto_increment PRIMARY KEY,PKEY VARCHAR(255), PRIVATE BOOLEAN, TITLE VARCHAR(255),URL VARCHAR(255),THUMBNAIL VARCHAR(255), NUMBER INTEGER, SUMMARY VARCHAR(16777216), ACCOUNT_ID INTEGER, created_at DATETIME, updated_at DATETIME);");
		Base.exec("CREATE TABLE IF NOT EXISTS UPLOADS(ID INTEGER NOT NULL auto_increment PRIMARY KEY,ARCHIVED BOOLEAN,CATEGORY VARCHAR(255),COMMENT SMALLINT,COMMENTVOTE BOOLEAN,DESCRIPTION VARCHAR(16777216),EMBED BOOLEAN,FAILED BOOLEAN,FILE VARCHAR(500),VISIBILITY SMALLINT,KEYWORDS VARCHAR(16777216),MIMETYPE VARCHAR(255),MOBILE BOOLEAN,RATE BOOLEAN,TITLE VARCHAR(255),UPLOADURL VARCHAR(255),VIDEORESPONSE SMALLINT,STARTED TIMESTAMP,INPROGRESS BOOLEAN,LOCKED BOOLEAN,VIDEOID VARCHAR(255),ACCOUNT_ID INTEGER, ENDDIR VARCHAR(255), LICENSE SMALLINT, RELEASE TIMESTAMP,NUMBER SMALLINT, PAUSEONFINISH BOOLEAN, created_at DATETIME, updated_at DATETIME, thumbnail VARCHAR(255));");
		Base.exec("CREATE TABLE IF NOT EXISTS UPLOADS_PLAYLISTS(id INTEGER NOT NULL auto_increment PRIMARY KEY, playlist_id INTEGER, upload_id INTEGER);");
		Base.exec("CREATE TABLE IF NOT EXISTS TEMPLATES_PLAYLISTS(id INTEGER NOT NULL auto_increment PRIMARY KEY, playlist_id INTEGER, template_id INTEGER);");
		Base.exec("CREATE TABLE IF NOT EXISTS SETTINGS(id INTEGER NOT NULL auto_increment PRIMARY KEY, `KEY` VARCHAR(255) NOT NULL UNIQUE, VALUE VARCHAR(255), created_at DATETIME, updated_at DATETIME);");
		Model.updateAll("inprogress = ?", false);
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
