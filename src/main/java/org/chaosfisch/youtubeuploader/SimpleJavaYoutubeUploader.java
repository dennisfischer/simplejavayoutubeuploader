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
import java.util.Properties;

import javax.sql.DataSource;

import org.chaosfisch.youtubeuploader.guice.GuiceBindings;
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
			// server = true;
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

		final Properties p = new Properties(System.getProperties());
		p.put("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
		p.put("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "INFO");
		System.setProperties(p);
	}

	public static boolean updateDatabase() {
		boolean updated = false;

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

		Base.exec("ALTER TABLE UPLOADS ALTER COLUMN ARCHIVED BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE UPLOADS ALTER COLUMN FAILED BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE UPLOADS ALTER COLUMN INPROGRESS BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE UPLOADS ALTER COLUMN LOCKED BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE UPLOADS ALTER COLUMN PAUSEONFINISH BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE UPLOADS ALTER COLUMN RATE BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE UPLOADS ALTER COLUMN MOBILE BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE UPLOADS ALTER COLUMN EMBED BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE UPLOADS ALTER COLUMN COMMENTVOTE BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE UPLOADS ALTER COLUMN facebook BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE UPLOADS ALTER COLUMN twitter BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE UPLOADS ALTER COLUMN instreamDefaults BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE UPLOADS ALTER COLUMN CLAIM BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE UPLOADS ALTER COLUMN OVERLAY BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE UPLOADS ALTER COLUMN TRUEVIEW BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE UPLOADS ALTER COLUMN INSTREAM BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE UPLOADS ALTER COLUMN PRODUCT BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE UPLOADS ALTER COLUMN monetizePartner BOOLEAN DEFAULT FALSE NOT NULL");

		Base.exec("ALTER TABLE TEMPLATES ALTER COLUMN RATE BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE TEMPLATES ALTER COLUMN MOBILE BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE TEMPLATES ALTER COLUMN EMBED BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE TEMPLATES ALTER COLUMN COMMENTVOTE BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE TEMPLATES ALTER COLUMN facebook BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE TEMPLATES ALTER COLUMN twitter BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE TEMPLATES ALTER COLUMN instreamDefaults BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE TEMPLATES ALTER COLUMN CLAIM BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE TEMPLATES ALTER COLUMN OVERLAY BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE TEMPLATES ALTER COLUMN TRUEVIEW BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE TEMPLATES ALTER COLUMN INSTREAM BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE TEMPLATES ALTER COLUMN PRODUCT BOOLEAN DEFAULT FALSE NOT NULL");
		Base.exec("ALTER TABLE TEMPLATES ALTER COLUMN monetizePartner BOOLEAN DEFAULT FALSE NOT NULL");

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
		Base
			.exec("CREATE TABLE IF NOT EXISTS ACCOUNTS(ID INTEGER NOT NULL auto_increment PRIMARY KEY,NAME VARCHAR(255),PASSWORD VARCHAR(255), TYPE VARCHAR(255), created_at DATETIME, updated_at DATETIME);");
		Base.exec("CREATE TABLE IF NOT EXISTS TEMPLATES (\r\n" + "    ID INTEGER NOT NULL auto_increment PRIMARY KEY,\r\n"
				+ "    CATEGORY VARCHAR(255),\r\n" + "    COMMENT SMALLINT(5),\r\n" + "    COMMENTVOTE BOOLEAN DEFAULT FALSE NOT NULL,\r\n"
				+ "    DEFAULTDIR VARCHAR(255),\r\n" + "    DESCRIPTION VARCHAR(16777216),\r\n"
				+ "    EMBED BOOLEAN DEFAULT FALSE NOT NULL,\r\n" + "    KEYWORDS VARCHAR(16777216),\r\n"
				+ "    MOBILE BOOLEAN DEFAULT FALSE NOT NULL,\r\n" + "    NAME VARCHAR(255),\r\n" + "    NUMBER SMALLINT(5),\r\n"
				+ "    RATE BOOLEAN DEFAULT FALSE NOT NULL,\r\n" + "    VIDEORESPONSE SMALLINT(5),\r\n" + "    VISIBILITY SMALLINT(5),\r\n"
				+ "    ACCOUNT_ID INT(10),\r\n" + "    ENDDIR VARCHAR(255),\r\n" + "    LICENSE SMALLINT(5),\r\n"
				+ "    CREATED_AT TIMESTAMP,\r\n" + "    UPDATED_AT TIMESTAMP,\r\n" + "    TITLE VARCHAR(255),\r\n"
				+ "    THUMBNAIL VARCHAR(255),\r\n" + "    FACEBOOK BOOLEAN DEFAULT FALSE NOT NULL,\r\n"
				+ "    TWITTER BOOLEAN DEFAULT FALSE NOT NULL,\r\n" + "    MESSAGE VARCHAR(5000),\r\n"
				+ "    INSTREAMDEFAULTS BOOLEAN DEFAULT FALSE NOT NULL,\r\n" + "    CLAIM BOOLEAN DEFAULT FALSE NOT NULL,\r\n"
				+ "    OVERLAY BOOLEAN DEFAULT FALSE NOT NULL,\r\n" + "    TRUEVIEW BOOLEAN DEFAULT FALSE NOT NULL,\r\n"
				+ "    INSTREAM BOOLEAN DEFAULT FALSE NOT NULL,\r\n" + "    PRODUCT BOOLEAN DEFAULT FALSE NOT NULL,\r\n"
				+ "    SYNDICATION INT(10),\r\n" + "    MONETIZETITLE VARCHAR(255),\r\n" + "    MONETIZEDESCRIPTION VARCHAR(255),\r\n"
				+ "    MONETIZEID VARCHAR(255),\r\n" + "    MONETIZENOTES VARCHAR(255),\r\n" + "    MONETIZETMSID VARCHAR(255),\r\n"
				+ "    MONETIZEISAN VARCHAR(255),\r\n" + "    MONETIZEEIDR VARCHAR(255),\r\n"
				+ "    MONETIZETITLEEPISODE VARCHAR(255),\r\n" + "    MONETIZESEASONNB VARCHAR(255),\r\n"
				+ "    MONETIZEEPISODENB VARCHAR(255),\r\n" + "    MONETIZECLAIMTYPE INT(10),\r\n" + "    MONETIZECLAIMPOLICY INT(10),\r\n"
				+ "    MONETIZEASSET INT(10),\r\n" + "    MONETIZEPARTNER BOOLEAN DEFAULT FALSE NOT NULL\r\n" + ")\r\n" + "");
		Base
			.exec("CREATE TABLE IF NOT EXISTS PLAYLISTS(ID INTEGER NOT NULL auto_increment PRIMARY KEY,PKEY VARCHAR(255), PRIVATE BOOLEAN, TITLE VARCHAR(255),URL VARCHAR(255),THUMBNAIL VARCHAR(255), NUMBER INTEGER, SUMMARY VARCHAR(16777216), ACCOUNT_ID INTEGER, created_at DATETIME, updated_at DATETIME);");
		Base.exec("CREATE TABLE IF NOT EXISTS UPLOADS (\r\n" + "    ID INTEGER NOT NULL auto_increment PRIMARY KEY,\r\n"
				+ "    ARCHIVED BOOLEAN DEFAULT FALSE NOT NULL,\r\n" + "    CATEGORY VARCHAR(255),\r\n" + "    COMMENT SMALLINT(5),\r\n"
				+ "    COMMENTVOTE  BOOLEAN DEFAULT FALSE NOT NULL,\r\n" + "    DESCRIPTION VARCHAR(16777216),\r\n"
				+ "    EMBED  BOOLEAN DEFAULT FALSE NOT NULL,\r\n" + "    FAILED  BOOLEAN DEFAULT FALSE NOT NULL,\r\n"
				+ "    FILE VARCHAR(500),\r\n" + "    VISIBILITY SMALLINT(5),\r\n" + "    KEYWORDS VARCHAR(16777216),\r\n"
				+ "    MIMETYPE VARCHAR(255),\r\n" + "    MOBILE  BOOLEAN DEFAULT FALSE NOT NULL,\r\n"
				+ "    RATE  BOOLEAN DEFAULT FALSE NOT NULL,\r\n" + "    TITLE VARCHAR(255),\r\n" + "    UPLOADURL VARCHAR(255),\r\n"
				+ "    VIDEORESPONSE SMALLINT(5),\r\n" + "    STARTED TIMESTAMP,\r\n"
				+ "    INPROGRESS  BOOLEAN DEFAULT FALSE NOT NULL,\r\n" + "    LOCKED  BOOLEAN DEFAULT FALSE NOT NULL,\r\n"
				+ "    VIDEOID VARCHAR(255),\r\n" + "    ACCOUNT_ID INT(10),\r\n" + "    ENDDIR VARCHAR(255),\r\n"
				+ "    LICENSE SMALLINT(5),\r\n" + "    RELEASE TIMESTAMP,\r\n" + "    NUMBER SMALLINT(5),\r\n"
				+ "    PAUSEONFINISH  BOOLEAN DEFAULT FALSE NOT NULL,\r\n" + "    CREATED_AT TIMESTAMP,\r\n"
				+ "    UPDATED_AT TIMESTAMP,\r\n" + "    THUMBNAIL VARCHAR(255),\r\n" + "    FACEBOOK  BOOLEAN DEFAULT FALSE NOT NULL,\r\n"
				+ "    TWITTER  BOOLEAN DEFAULT FALSE NOT NULL,\r\n" + "    MESSAGE VARCHAR(5000),\r\n"
				+ "    INSTREAMDEFAULTS  BOOLEAN DEFAULT FALSE NOT NULL,\r\n" + "    CLAIM  BOOLEAN DEFAULT FALSE NOT NULL,\r\n"
				+ "    OVERLAY  BOOLEAN DEFAULT FALSE NOT NULL,\r\n" + "    TRUEVIEW  BOOLEAN DEFAULT FALSE NOT NULL,\r\n"
				+ "    INSTREAM  BOOLEAN DEFAULT FALSE NOT NULL,\r\n" + "    PRODUCT  BOOLEAN DEFAULT FALSE NOT NULL,\r\n"
				+ "    SYNDICATION INT(10),\r\n" + "    MONETIZETITLE VARCHAR(255),\r\n" + "    MONETIZEDESCRIPTION VARCHAR(255),\r\n"
				+ "    MONETIZEID VARCHAR(255),\r\n" + "    MONETIZENOTES VARCHAR(255),\r\n" + "    MONETIZETMSID VARCHAR(255),\r\n"
				+ "    MONETIZEISAN VARCHAR(255),\r\n" + "    MONETIZEEIDR VARCHAR(255),\r\n"
				+ "    MONETIZETITLEEPISODE VARCHAR(255),\r\n" + "    MONETIZESEASONNB VARCHAR(255),\r\n"
				+ "    MONETIZEEPISODENB VARCHAR(255),\r\n" + "    MONETIZECLAIMTYPE INT(10),\r\n" + "    MONETIZECLAIMPOLICY INT(10),\r\n"
				+ "    MONETIZEASSET INT(10),\r\n" + "    MONETIZEPARTNER  BOOLEAN DEFAULT FALSE NOT NULL\r\n" + ");\r\n" + "");
		Base
			.exec("CREATE TABLE IF NOT EXISTS UPLOADS_PLAYLISTS(id INTEGER NOT NULL auto_increment PRIMARY KEY, playlist_id INTEGER, upload_id INTEGER);");
		Base
			.exec("CREATE TABLE IF NOT EXISTS TEMPLATES_PLAYLISTS(id INTEGER NOT NULL auto_increment PRIMARY KEY, playlist_id INTEGER, template_id INTEGER);");
		Upload.updateAll("inprogress = ?", false);
		Base.commitTransaction();
	}
}
