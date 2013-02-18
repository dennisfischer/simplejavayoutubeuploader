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

import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;

import javax.sql.DataSource;

import org.chaosfisch.youtubeuploader.guice.GuiceBindings;
import org.chaosfisch.youtubeuploader.models.Setting;
import org.chaosfisch.youtubeuploader.models.Template;
import org.chaosfisch.youtubeuploader.models.Upload;
import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.javafx.PlatformUtil;

public class SimpleJavaYoutubeUploader {

	private static boolean	server	= false;
	private static Logger	logger	= LoggerFactory.getLogger(SimpleJavaYoutubeUploader.class);
	/**
	 * The application DI injector
	 */
	static Injector			injector;

	public static void main(String[] args) {
		args = new String[] { "-test" };
		if (args.length > 0) {
			server = true;
		}
		initLogger();
		initLocale();
		initSavedir();
		initUpdater();
		injector = Guice.createInjector(new GuiceBindings("youtubeuploader" + (server ? "-server" : "")));
		if (!server) {
			System.out.println("CLIENT");
			GuiUploader.initialize(args, injector);
		} else {
			System.out.println("SERVER");
			ConsoleUploader.initialize(args, injector);
		}
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
		final Locale[] availableLocales = { Locale.GERMANY, Locale.GERMAN, Locale.ENGLISH };
		if (!Arrays.asList(availableLocales).contains(Locale.getDefault())) {
			Locale.setDefault(Locale.ENGLISH);
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
				logger.error(s);
			}
		});
	}

	public static boolean updateDatabase() {
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

		if (!Template.getMetaModel().getColumnMetadata().containsKey("instreamdefaults")) {
			Base.openTransaction();
			Base.exec("ALTER TABLE TEMPLATES ADD instreamDefaults BOOLEAN");
			Base.exec("ALTER TABLE TEMPLATES ADD CLAIM BOOLEAN");
			Base.exec("ALTER TABLE TEMPLATES ADD OVERLAY BOOLEAN");
			Base.exec("ALTER TABLE TEMPLATES ADD TRUEVIEW BOOLEAN");
			Base.exec("ALTER TABLE TEMPLATES ADD INSTREAM BOOLEAN");
			Base.exec("ALTER TABLE TEMPLATES ADD PRODUCT BOOLEAN");
			Base.exec("ALTER TABLE TEMPLATES ADD SYNDICATION INTEGER");
			Base.exec("ALTER TABLE TEMPLATES ADD monetizeTitle VARCHAR(255)");
			Base.exec("ALTER TABLE TEMPLATES ADD monetizeDescription VARCHAR(255)");
			Base.exec("ALTER TABLE TEMPLATES ADD monetizeID VARCHAR(255)");
			Base.exec("ALTER TABLE TEMPLATES ADD monetizeNotes VARCHAR(255)");
			Base.exec("ALTER TABLE TEMPLATES ADD monetizeTMSID VARCHAR(255)");
			Base.exec("ALTER TABLE TEMPLATES ADD monetizeISAN VARCHAR(255)");
			Base.exec("ALTER TABLE TEMPLATES ADD monetizeEIDR VARCHAR(255)");
			Base.exec("ALTER TABLE TEMPLATES ADD monetizeTitleEpisode VARCHAR(255)");
			Base.exec("ALTER TABLE TEMPLATES ADD monetizeSeasonNB VARCHAR(255)");
			Base.exec("ALTER TABLE TEMPLATES ADD monetizeEpisodeNB VARCHAR(255)");
			Base.exec("ALTER TABLE TEMPLATES ADD monetizeClaimType INTEGER");
			Base.exec("ALTER TABLE TEMPLATES ADD monetizeClaimPolicy INTEGER");
			Base.exec("ALTER TABLE TEMPLATES ADD monetizeAsset INTEGER");
			Base.exec("ALTER TABLE TEMPLATES ADD monetizePartner BOOLEAN");
			Base.commitTransaction();
			updated = true;
		}

		return updated;
	}

	public static void initDatabase() {
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
}
