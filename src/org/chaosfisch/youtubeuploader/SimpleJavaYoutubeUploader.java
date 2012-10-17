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
import java.sql.SQLException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.sql.DataSource;

import org.chaosfisch.youtubeuploader.modules.GuiceBindings;
import org.chaosfisch.youtubeuploader.services.uploader.Uploader;
import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mchange.v2.c3p0.DataSources;

public class SimpleJavaYoutubeUploader extends Application
{

	/**
	 * The application DI injector
	 */
	final Injector			injector	= Guice.createInjector(new GuiceBindings());
	final static Logger		logger		= LoggerFactory.getLogger(SimpleJavaYoutubeUploader.class);
	final private Uploader	uploader	= injector.getInstance(Uploader.class);

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

	public static void main(final String[] args)
	{
		launch(args);
	}

	protected void initApplication(final Stage primaryStage) throws IOException
	{
		try
		{
			final DataSource dataSourceUnpooled = DataSources.unpooledDataSource("jdbc:h2:~SimpleJavaYoutubeUploader/dbTest/youtubeuploader", "", "");
			final DataSource dataSourcePooled = DataSources.pooledDataSource(dataSourceUnpooled);
			Base.open(dataSourcePooled);
			try
			{
				Base.connection().setAutoCommit(true);
			} catch (SQLException e)
			{
				e.printStackTrace();
			}
			Base.openTransaction();
			Base.exec("CREATE TABLE IF NOT EXISTS ACCOUNTS(ID INTEGER NOT NULL auto_increment PRIMARY KEY,NAME VARCHAR(255),PASSWORD VARCHAR(255), TYPE VARCHAR(255));");
			Base.exec("CREATE TABLE IF NOT EXISTS DIRECTORIES(ID INTEGER NOT NULL auto_increment PRIMARY KEY,DIRECTORY VARCHAR(255),ACTIVE BOOLEAN,LOCKED BOOLEAN,PRESET_ID INTEGER);");
			Base.exec("CREATE TABLE IF NOT EXISTS MESSAGES(ID INTEGER NOT NULL auto_increment PRIMARY KEY,MESSAGE VARCHAR(255),QUEUE_ID INTEGER,FACEBOOK BOOLEAN,TWITTER BOOLEAN,GOOGLEPLUS BOOLEAN,YOUTUBE BOOLEAN);");
			Base.exec("CREATE TABLE IF NOT EXISTS PRESETS(ID INTEGER NOT NULL auto_increment PRIMARY KEY,CATEGORY VARCHAR(255),COMMENT SMALLINT,COMMENTVOTE BOOLEAN, DEFAULTDIR VARCHAR(255),DESCRIPTION VARCHAR(16777216),EMBED BOOLEAN,KEYWORDS VARCHAR(16777216),MOBILE BOOLEAN,NAME VARCHAR(255),NUMBERMODIFIER SMALLINT,RATE BOOLEAN,VIDEORESPONSE SMALLINT,VISIBILITY SMALLINT,PLAYLISTS_ID VARCHAR(255),ACCOUNT_ID INTEGER,PLAYLIST_ID INTEGER,MONETIZE BOOLEAN,MONETIZEOVERLAY BOOLEAN,MONETIZETRUEVIEW BOOLEAN,MONETIZEPRODUCT BOOLEAN,ENDDIR VARCHAR(255),LICENSE SMALLINT,CLAIM BOOLEAN,CLAIMTYPE SMALLINT,CLAIMPOLICY SMALLINT,PARTNEROVERLAY BOOLEAN,PARTNERTRUEVIEW BOOLEAN,PARTNERPRODUCT BOOLEAN,PARTNERINSTREAM BOOLEAN,ASSET VARCHAR(255),WEBTITLE VARCHAR(255),WEBDESCRIPTION VARCHAR(255),WEBID VARCHAR(255),WEBNOTES VARCHAR(255),TVTMSID VARCHAR(255),TVISAN VARCHAR(255),TVEIDR VARCHAR(255),SHOWTITLE VARCHAR(255),EPISODETITLE VARCHAR(255),SEASONNB VARCHAR(255),EPISODENB VARCHAR(255),TVID VARCHAR(255),TVNOTES VARCHAR(255),MOVIETITLE VARCHAR(255),MOVIEDESCRIPTION VARCHAR(255),MOVIETMSID VARCHAR(255),MOVIEISAN VARCHAR(255),MOVIEEIDR VARCHAR(255),MOVIEID VARCHAR(255),MOVIENOTES VARCHAR(255));");
			Base.exec("CREATE TABLE IF NOT EXISTS PLACEHOLDER(ID INTEGER NOT NULL auto_increment PRIMARY KEY,PLACEHOLDER VARCHAR(255),REPLACEMENT VARCHAR(255));");
			Base.exec("CREATE TABLE IF NOT EXISTS PLAYLISTS(ID INTEGER NOT NULL auto_increment PRIMARY KEY,PKEY VARCHAR(255), PRIVATE BOOLEAN, TITLE VARCHAR(255),URL VARCHAR(255), NUMBER INTEGER, SUMMARY VARCHAR(16777216), ACCOUNT_ID INTEGER);");
			Base.exec("CREATE TABLE IF NOT EXISTS QUEUE(ID INTEGER NOT NULL auto_increment PRIMARY KEY,ARCHIVED BOOLEAN,CATEGORY VARCHAR(255),COMMENT SMALLINT,COMMENTVOTE BOOLEAN,DESCRIPTION VARCHAR(16777216),EMBED BOOLEAN,FAILED BOOLEAN,FILE VARCHAR(255),KEYWORDS VARCHAR(16777216),MIMETYPE VARCHAR(255),MOBILE BOOLEAN,PRIVATEFILE BOOLEAN,RATE BOOLEAN,TITLE VARCHAR(255),UNLISTED BOOLEAN,UPLOADURL VARCHAR(255),VIDEORESPONSE SMALLINT,SEQUENCE INTEGER,STARTED TIMESTAMP,INPROGRESS BOOLEAN,LOCKED BOOLEAN DEFAULT FALSE,VIDEOID VARCHAR(255),ACCOUNT_ID INTEGER,PLAYLISTS_ID INTEGER,PLAYLIST_ID INTEGER,MONETIZE BOOLEAN,MONETIZEOVERLAY BOOLEAN,MONETIZETRUEVIEW BOOLEAN,MONETIZEPRODUCT BOOLEAN,ENDDIR VARCHAR(255),LICENSE SMALLINT,RELEASE TIMESTAMP,NUMBER SMALLINT,CLAIM BOOLEAN,CLAIMTYPE SMALLINT,CLAIMPOLICY SMALLINT,PARTNEROVERLAY BOOLEAN,PARTNERTRUEVIEW BOOLEAN,PARTNERPRODUCT BOOLEAN,PARTNERINSTREAM BOOLEAN,ASSET VARCHAR(255),WEBTITLE VARCHAR(255),WEBDESCRIPTION VARCHAR(255),WEBID VARCHAR(255),WEBNOTES VARCHAR(255),TVTMSID VARCHAR(255),TVISAN VARCHAR(255),TVEIDR VARCHAR(255),SHOWTITLE VARCHAR(255),EPISODETITLE VARCHAR(255),SEASONNB VARCHAR(255),EPISODENB VARCHAR(255),TVID VARCHAR(255),TVNOTES VARCHAR(255),MOVIETITLE VARCHAR(255),MOVIEDESCRIPTION VARCHAR(255),MOVIETMSID VARCHAR(255),MOVIEISAN VARCHAR(255),MOVIEEIDR VARCHAR(255),MOVIEID VARCHAR(255),MOVIENOTES VARCHAR(255), PAUSEONFINISH BOOLEAN);");
			Base.exec("CREATE TABLE IF NOT EXISTS SETTINGS(ID VARCHAR(255) NOT NULL PRIMARY KEY, VALUE VARCHAR(255));");
			Base.commitTransaction();
		} catch (SQLException e1)
		{
			e1.printStackTrace();
		}

		final FXMLLoader fxLoader = new FXMLLoader(getClass().getResource("/org/chaosfisch/youtubeuploader/view/SimpleJavaYoutubeUploader.fxml"),
				I18nHelper.getResourceBundle());
		fxLoader.setControllerFactory(new GuiceControllerFactory(injector));
		fxLoader.load();
		final Scene scene = new Scene((Parent) fxLoader.getRoot(), 1000, 500);
		scene.getStylesheets().add(getClass().getResource("/org/chaosfisch/youtubeuploader/resources/style.css").toExternalForm());
		primaryStage.setTitle(I18nHelper.message("application.title"));
		primaryStage.setScene(scene);
		primaryStage.setMinHeight(500);
		primaryStage.setMinWidth(1000);
		primaryStage.show();
	}

	@Override
	public void start(final Stage primaryStage)
	{
		try
		{
			initApplication(primaryStage);
			// uploader.runStarttimeChecker();
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void stop() throws Exception
	{
		super.stop();
		uploader.stopStarttimeChecker();
		uploader.exit();
	}
}