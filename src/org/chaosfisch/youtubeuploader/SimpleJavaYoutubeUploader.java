/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import javax.sql.DataSource;

import name.antonsmirnov.javafx.dialog.Dialog;

import org.chaosfisch.util.LogfileCommitter;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.models.Setting;
import org.chaosfisch.youtubeuploader.models.Upload;
import org.chaosfisch.youtubeuploader.modules.GuiceBindings;
import org.chaosfisch.youtubeuploader.services.youtube.uploader.Uploader;
import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class SimpleJavaYoutubeUploader extends Application
{

	/**
	 * A JavaFX controller factory for constructing controllers via Guice DI. To
	 * install this in the {@link FXMLLoader}, pass it as a parameter to
	 * {@link FXMLLoader#setControllerFactory(Callback)}.
	 * <p>
	 * Once set, make sure you do <b>not</b> use the static methods on
	 * {@link FXMLLoader} when creating your JavaFX node.
	 */
	class GuiceControllerFactory implements Callback<Class<?>, Object>
	{

		private final Injector	injector;

		public GuiceControllerFactory(final Injector anInjector)
		{
			injector = anInjector;
		}

		@Override
		public Object call(final Class<?> aClass)
		{
			return injector.getInstance(aClass);
		}
	}

	/**
	 * The application DI injector
	 */
	final Injector		injector	= Guice.createInjector(new GuiceBindings());
	final static Logger	logger		= LoggerFactory.getLogger(SimpleJavaYoutubeUploader.class);

	public static void main(final String[] args)
	{
		System.setOut(new PrintStream(System.out) {
			@Override
			public void print(final String s)
			{
				logger.info(s);
			}
		});
		System.setErr(new PrintStream(System.err) {
			@Override
			public void print(final String s)
			{
				if (!s.startsWith("WARNING: com.sun.javafx.css.StyleHelper calculateValue"))
				{
					logger.error(s);
				}
			}
		});
		final Locale[] availableLocales = { Locale.GERMAN, Locale.ENGLISH };
		if (!Arrays.asList(availableLocales).contains(Locale.getDefault()))
		{
			// TODO CHANGE THIS TO ENGLISH AS SOON AS TRANSLATED!
			Locale.setDefault(Locale.GERMAN);
		}

		launch(args);
	}

	final private Uploader	uploader	= injector.getInstance(Uploader.class);

	protected void initApplication(final Stage primaryStage) throws IOException
	{
		initDatabase();
		updateDatabase();

		final FXMLLoader fxLoader = new FXMLLoader(getClass().getResource("/org/chaosfisch/youtubeuploader/view/SimpleJavaYoutubeUploader.fxml"),
				I18nHelper.getResourceBundle());
		fxLoader.setControllerFactory(new GuiceControllerFactory(injector));
		fxLoader.load();
		final Scene scene = new Scene((Parent) fxLoader.getRoot(), 1000, 600);
		scene.getStylesheets().add(getClass().getResource("/org/chaosfisch/youtubeuploader/resources/style.css").toExternalForm());
		primaryStage.setTitle(I18nHelper.message("application.title"));
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/org/chaosfisch/youtubeuploader/resources/images/film.png")));
		primaryStage.setScene(scene);
		primaryStage.setMinHeight(600);
		primaryStage.setMinWidth(1000);
		primaryStage.setOnHiding(new EventHandler<WindowEvent>() {

			@Override
			public void handle(final WindowEvent event)
			{
				final Dialog dialog = Dialog.buildConfirmation("Exit application", "Do you really want to exit the application?")
						.addYesButton(null)
						.addCancelButton(new EventHandler<ActionEvent>() {

							@Override
							public void handle(final ActionEvent event)
							{
								primaryStage.show();
							}
						})
						.build();
				dialog.show();
			}
		});
		primaryStage.show();
	}

	private void updateDatabase()
	{
		if (!Setting.getMetaModel().getColumnMetadata().containsKey("key"))
		{
			Base.openTransaction();
			Base.exec("DROP TABLE SETTINGS");
			Base.exec("DROP TABLE PLACEHOLDERS");
			Base.exec("CREATE TABLE IF NOT EXISTS SETTINGS(id INTEGER NOT NULL auto_increment PRIMARY KEY, `KEY` VARCHAR(255) NOT NULL UNIQUE, VALUE VARCHAR(255), created_at DATETIME, updated_at DATETIME);");
			Base.commitTransaction();
			final Dialog dialog = Dialog.buildConfirmation("Anwendung neustarten!",
															"Die Anwendung muss neu gestartet werden. Die Datenbank wurde aktualisiert!")
					.addYesButton(null)
					.build();
			dialog.showAndWait();
			System.exit(0);
		}
	}

	private void initDatabase()
	{
		Base.open(injector.getInstance(DataSource.class));
		try
		{
			Base.connection().setAutoCommit(true);
		} catch (final SQLException e)
		{
			logger.error("Database setAutoCommit failed!", e);
		}
		Base.openTransaction();
		Base.exec("CREATE TABLE IF NOT EXISTS ACCOUNTS(ID INTEGER NOT NULL auto_increment PRIMARY KEY,NAME VARCHAR(255),PASSWORD VARCHAR(255), TYPE VARCHAR(255), created_at DATETIME, updated_at DATETIME);");
		Base.exec("CREATE TABLE IF NOT EXISTS TEMPLATES(ID INTEGER NOT NULL auto_increment PRIMARY KEY,CATEGORY VARCHAR(255),COMMENT SMALLINT,COMMENTVOTE BOOLEAN, DEFAULTDIR VARCHAR(255),DESCRIPTION VARCHAR(16777216),EMBED BOOLEAN,KEYWORDS VARCHAR(16777216),MOBILE BOOLEAN,NAME VARCHAR(255),NUMBER SMALLINT,RATE BOOLEAN,VIDEORESPONSE SMALLINT,VISIBILITY SMALLINT,ACCOUNT_ID INTEGER,ENDDIR VARCHAR(255),LICENSE SMALLINT, created_at DATETIME, updated_at DATETIME, TITLE VARCHAR(255));");
		Base.exec("CREATE TABLE IF NOT EXISTS PLAYLISTS(ID INTEGER NOT NULL auto_increment PRIMARY KEY,PKEY VARCHAR(255), PRIVATE BOOLEAN, TITLE VARCHAR(255),URL VARCHAR(255),THUMBNAIL VARCHAR(255), NUMBER INTEGER, SUMMARY VARCHAR(16777216), ACCOUNT_ID INTEGER, created_at DATETIME, updated_at DATETIME);");
		Base.exec("CREATE TABLE IF NOT EXISTS UPLOADS(ID INTEGER NOT NULL auto_increment PRIMARY KEY,ARCHIVED BOOLEAN,CATEGORY VARCHAR(255),COMMENT SMALLINT,COMMENTVOTE BOOLEAN,DESCRIPTION VARCHAR(16777216),EMBED BOOLEAN,FAILED BOOLEAN,FILE VARCHAR(500),VISIBILITY SMALLINT,KEYWORDS VARCHAR(16777216),MIMETYPE VARCHAR(255),MOBILE BOOLEAN,RATE BOOLEAN,TITLE VARCHAR(255),UPLOADURL VARCHAR(255),VIDEORESPONSE SMALLINT,STARTED TIMESTAMP,INPROGRESS BOOLEAN,LOCKED BOOLEAN,VIDEOID VARCHAR(255),ACCOUNT_ID INTEGER, ENDDIR VARCHAR(255), LICENSE SMALLINT, RELEASE TIMESTAMP,NUMBER SMALLINT, PAUSEONFINISH BOOLEAN, created_at DATETIME, updated_at DATETIME);");
		Base.exec("CREATE TABLE IF NOT EXISTS UPLOADS_PLAYLISTS(id INTEGER NOT NULL auto_increment PRIMARY KEY, playlist_id INTEGER, upload_id INTEGER);");
		Base.exec("CREATE TABLE IF NOT EXISTS TEMPLATES_PLAYLISTS(id INTEGER NOT NULL auto_increment PRIMARY KEY, playlist_id INTEGER, template_id INTEGER);");
		Base.exec("CREATE TABLE IF NOT EXISTS SETTINGS(id INTEGER NOT NULL auto_increment PRIMARY KEY, `KEY` VARCHAR(255) NOT NULL UNIQUE, VALUE VARCHAR(255), created_at DATETIME, updated_at DATETIME);");
		Upload.updateAll("inprogress = ?", false);
		Playlist.deleteAll();
		Base.commitTransaction();
	}

	@Override
	public void start(final Stage primaryStage)
	{
		try
		{
			Platform.setImplicitExit(true);
			initApplication(primaryStage);
			uploader.runStarttimeChecker();
		} catch (final IOException e)
		{
			logger.error("Couldn't start the application", e);
		}
	}

	@Override
	public void stop() throws Exception
	{
		super.stop();
		LogfileCommitter.commit();
		uploader.stopStarttimeChecker();
		uploader.exit();
	}
}
