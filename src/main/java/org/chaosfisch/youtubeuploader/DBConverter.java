/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader;

import org.chaosfisch.youtubeuploader.converter.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

public final class DBConverter {

	private static final Logger logger = LoggerFactory.getLogger(DBConverter.class);

	public static void main(final String[] args) throws SQLException, IOException {

		try {
			if (Files.exists(Paths.get(ApplicationData.DATA_DIR + "/db/youtubeuploader.db.data"))) {
				logger.info("Converting v2");
				convertV2();
			} else if (Files.exists(Paths.get(ApplicationData.DATA_DIR + "/youtubeuploader.h2.db"))) {
				logger.info("Converting v3");
				convertV3();
			} else {
				logger.info("Nothing to convert");
			}
		} catch (Exception e) {
			logger.error("Conversion error", e);
		}
	}

	private static Connection getNewConnection() throws SQLException, IOException {
		try {
			Class.forName("org.h2.Driver");
		} catch (Exception e) {
			logger.error("ERROR: failed to load H2 JDBC driver.", e);
			return null;
		}
		final File schema = new File(ApplicationData.HOME + "/SimpleJavaYoutubeUploader/schema.sql");
		if (!schema.exists()) {
			try (final InputStream inputStream = DBConverter.class.getResourceAsStream("/schema.sql")) {
				Files.copy(inputStream, Paths.get(schema.toURI()));
			}
		}
		final String url = "jdbc:h2:" + ApplicationData.DATA_DIR + "/youtubeuploader-v3" + ";INIT=RUNSCRIPT FROM '~/SimpleJavaYoutubeUploader/schema.sql'";
		final Connection con = DriverManager.getConnection(url, "username", "");

		Statement stmt = con.createStatement();

		stmt.execute("DROP TRIGGER IF EXISTS ACCOUNT_I");
		stmt = con.createStatement();
		stmt.execute("DROP TRIGGER IF EXISTS ACCOUNT_U");
		stmt = con.createStatement();
		stmt.execute("DROP TRIGGER IF EXISTS ACCOUNT_D");
		stmt = con.createStatement();
		stmt.execute("DROP TRIGGER IF EXISTS PLAYLIST_I");
		stmt = con.createStatement();
		stmt.execute("DROP TRIGGER IF EXISTS PLAYLIST_U");
		stmt = con.createStatement();
		stmt.execute("DROP TRIGGER IF EXISTS PLAYLIST_D");
		stmt = con.createStatement();
		stmt.execute("DROP TRIGGER IF EXISTS TEMPLATE_I");
		stmt = con.createStatement();
		stmt.execute("DROP TRIGGER IF EXISTS TEMPLATE_U");
		stmt = con.createStatement();
		stmt.execute("DROP TRIGGER IF EXISTS TEMPLATE_D");
		stmt = con.createStatement();
		stmt.execute("DROP TRIGGER IF EXISTS UPLOAD_I");
		stmt = con.createStatement();
		stmt.execute("DROP TRIGGER IF EXISTS UPLOAD_U");
		stmt = con.createStatement();
		stmt.execute("DROP TRIGGER IF EXISTS UPLOAD_D");
		stmt = con.createStatement();
		stmt.execute("ALTER TABLE TEMPLATE DROP COLUMN IF EXISTS NUMBER");
		stmt = con.createStatement();
		stmt.execute("ALTER TABLE UPLOAD DROP COLUMN IF EXISTS NUMBER");

		return con;
	}

	private static void convertV2() throws SQLException, IOException {
		try {
			Class.forName("org.hsqldb.jdbcDriver");
		} catch (Exception e) {
			logger.error("ERROR: failed to load HSQLDB JDBC driver.", e);
			return;
		}

		try {
			Class.forName("org.h2.Driver");
		} catch (Exception e) {
			logger.error("ERROR: failed to load H2 JDBC driver.", e);
			return;
		}

		final Connection connectionOld = DriverManager.getConnection("jdbc:hsqldb:file:" + ApplicationData.DATA_DIR + "/db/youtubeuploader.db;encoding=UTF-8;crypt_key=604a6105889da65326bf35790a923932;crypt_type=blowfish;", "sa", "");
		final Connection connectionNew = getNewConnection();

		transferAccountsV2(connectionOld, connectionNew);
		transferTemplatesV2(connectionOld, connectionNew);
		transferUploadsV2(connectionOld, connectionNew);
		connectionNew.close();
		connectionOld.close();

		copyFolder(new File(ApplicationData.DATA_DIR + "/db"), new File(ApplicationData.DATA_DIR + "/db_backup"));
		deleteRecursive(new File(ApplicationData.DATA_DIR + "/db"));
	}

	private static void transferAccountsV2(final Connection connectionOld, final Connection connectionNew) throws SQLException {
		final Statement stmtAccountSelect = connectionOld.createStatement();
		final ResultSet rsAccount = stmtAccountSelect.executeQuery("SELECT * FROM ACCOUNTS");

		final PreparedStatement stmtAccountInsert = connectionNew.prepareStatement("INSERT INTO ACCOUNT (ID, NAME, PASSWORD) VALUES(?,?,?)");
		while (rsAccount.next()) {
			stmtAccountInsert.setInt(1, rsAccount.getInt("IDENTITY"));
			stmtAccountInsert.setString(2, rsAccount.getString("NAME"));
			stmtAccountInsert.setString(3, rsAccount.getString("PASSWORD"));
			stmtAccountInsert.execute();
		}
	}

	private static void transferTemplatesV2(final Connection connectionOld, final Connection connectionNew) throws SQLException {

		final Statement stmtPresetSelect = connectionOld.createStatement();
		final ResultSet rsPreset = stmtPresetSelect.executeQuery("SELECT * FROM PRESETS");

		final PreparedStatement stmtPresetInsert = connectionNew.prepareStatement("INSERT INTO TEMPLATE (CATEGORY, COMMENT, COMMENTVOTE, DEFAULTDIR, DESCRIPTION, EMBED, KEYWORDS, MOBILE, NAME, RATE, VIDEORESPONSE, VISIBILITY, ACCOUNT_ID, ENDDIR, LICENSE, TITLE, THUMBNAIL, FACEBOOK, TWITTER, MESSAGE, MONETIZE_INSTREAM_DEFAULTS, MONETIZE_CLAIM, MONETIZE_OVERLAY, MONETIZE_TRUEVIEW, MONETIZE_INSTREAM, MONETIZE_PRODUCT, MONETIZE_SYNDICATION, MONETIZE_TITLE, MONETIZE_DESCRIPTION, MONETIZE_ID, MONETIZE_NOTES, MONETIZE_TMSID, MONETIZE_ISAN, MONETIZE_EIDR, MONETIZE_TITLEEPISODE, MONETIZE_SEASON_NB, MONETIZE_EPISODE_NB, MONETIZE_CLAIMTYPE, MONETIZE_CLAIMOPTION, MONETIZE_ASSET, MONETIZE_PARTNER) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

		while (rsPreset.next()) {
			Category category = Category.GAMES;
			for (final Category cat : Category.values()) {
				if (cat.toCategory().term.equals(rsPreset.getString("CATEGORY"))) {
					category = cat;
				}
			}

			stmtPresetInsert.setString(1, category.name());
			stmtPresetInsert.setString(2, Comment.values()[rsPreset.getInt("COMMENT")].name());
			stmtPresetInsert.setBoolean(3, rsPreset.getBoolean("COMMENTVOTE"));
			stmtPresetInsert.setString(4, rsPreset.getString("DEFAULTDIR"));
			stmtPresetInsert.setString(5, rsPreset.getString("DESCRIPTION"));
			stmtPresetInsert.setBoolean(6, rsPreset.getBoolean("EMBED"));
			stmtPresetInsert.setString(7, rsPreset.getString("KEYWORDS"));
			stmtPresetInsert.setBoolean(8, rsPreset.getBoolean("MOBILE"));
			stmtPresetInsert.setString(9, rsPreset.getString("NAME"));
			stmtPresetInsert.setBoolean(10, rsPreset.getBoolean("RATE"));
			stmtPresetInsert.setString(11, Videoresponse.values()[rsPreset.getInt("VIDEORESPONSE")].name());
			stmtPresetInsert.setString(12, Visibility.values()[rsPreset.getInt("VISIBILITY")].name());
			stmtPresetInsert.setObject(13, rsPreset.getObject("ACCOUNT_ID"), Types.INTEGER);
			stmtPresetInsert.setString(14, rsPreset.getString("ENDDIR"));
			stmtPresetInsert.setString(15, License.values()[rsPreset.getString("LICENSE").equals("youtube")
															? 0
															: 1].name());
			stmtPresetInsert.setString(16, rsPreset.getString("TITLE"));
			stmtPresetInsert.setNull(17, Types.VARCHAR);
			stmtPresetInsert.setBoolean(18, false);
			stmtPresetInsert.setBoolean(19, false);
			stmtPresetInsert.setNull(20, Types.VARCHAR);
			stmtPresetInsert.setBoolean(21, false);
			stmtPresetInsert.setBoolean(22, false);
			stmtPresetInsert.setBoolean(23, false);
			stmtPresetInsert.setBoolean(24, false);
			stmtPresetInsert.setBoolean(25, false);
			stmtPresetInsert.setBoolean(26, false);
			stmtPresetInsert.setString(27, Syndication.GLOBAL.name());
			stmtPresetInsert.setNull(28, Types.VARCHAR);
			stmtPresetInsert.setNull(29, Types.VARCHAR);
			stmtPresetInsert.setNull(30, Types.VARCHAR);
			stmtPresetInsert.setNull(31, Types.VARCHAR);
			stmtPresetInsert.setNull(32, Types.VARCHAR);
			stmtPresetInsert.setNull(33, Types.VARCHAR);
			stmtPresetInsert.setNull(34, Types.VARCHAR);
			stmtPresetInsert.setNull(35, Types.VARCHAR);
			stmtPresetInsert.setNull(36, Types.VARCHAR);
			stmtPresetInsert.setNull(37, Types.VARCHAR);
			stmtPresetInsert.setString(38, ClaimType.AUDIO_VISUAL.name());
			stmtPresetInsert.setString(39, ClaimOption.MONETIZE.name());
			stmtPresetInsert.setString(40, Asset.WEB.name());
			stmtPresetInsert.setBoolean(41, false);
			stmtPresetInsert.execute();
		}
	}

	private static void transferUploadsV2(final Connection connectionOld, final Connection connectionNew) throws SQLException {
		final Statement stmtUploadSelect = connectionOld.createStatement();
		final ResultSet rsUpload = stmtUploadSelect.executeQuery("SELECT * FROM QUEUE WHERE ARCHIVED=FALSE");

		final PreparedStatement stmtUploadInsert = connectionNew.prepareStatement("INSERT INTO UPLOAD (ARCHIVED, CATEGORY, COMMENT, COMMENTVOTE, DESCRIPTION, EMBED, FAILED, FILE, KEYWORDS, MIMETYPE, MOBILE, RATE, TITLE, UPLOADURL, VIDEORESPONSE, VISIBILITY, DATE_OF_START, INPROGRESS, LOCKED, VIDEOID, ACCOUNT_ID, ENDDIR, LICENSE, DATE_OF_RELEASE, PAUSEONFINISH, THUMBNAIL, FACEBOOK, TWITTER, MESSAGE, MONETIZE_INSTREAM_DEFAULTS, MONETIZE_CLAIM, MONETIZE_OVERLAY, MONETIZE_TRUEVIEW, MONETIZE_INSTREAM, MONETIZE_PRODUCT, MONETIZE_SYNDICATION, MONETIZE_TITLE, MONETIZE_DESCRIPTION, MONETIZE_ID, MONETIZE_NOTES, MONETIZE_TMSID, MONETIZE_ISAN, MONETIZE_EIDR, MONETIZE_TITLEEPISODE, MONETIZE_SEASON_NB, MONETIZE_EPISODE_NB, MONETIZE_CLAIMTYPE, MONETIZE_CLAIMOPTION, MONETIZE_ASSET, MONETIZE_PARTNER, STATUS) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

		while (rsUpload.next()) {
			Category category = Category.GAMES;
			for (final Category cat : Category.values()) {
				if (cat.toCategory().term.equals(rsUpload.getString("CATEGORY"))) {
					category = cat;
				}
			}

			stmtUploadInsert.setBoolean(1, false);
			stmtUploadInsert.setString(2, category.name());
			stmtUploadInsert.setString(3, Comment.values()[rsUpload.getInt("COMMENT")].name());
			stmtUploadInsert.setBoolean(4, rsUpload.getBoolean("COMMENTVOTE"));
			stmtUploadInsert.setString(5, rsUpload.getString("DESCRIPTION"));
			stmtUploadInsert.setBoolean(6, rsUpload.getBoolean("EMBED"));
			stmtUploadInsert.setBoolean(7, false);
			stmtUploadInsert.setString(8, rsUpload.getString("FILE"));
			stmtUploadInsert.setString(9, rsUpload.getString("KEYWORDS"));
			stmtUploadInsert.setString(10, rsUpload.getString("MIMETYPE"));
			stmtUploadInsert.setBoolean(11, rsUpload.getBoolean("MOBILE"));
			stmtUploadInsert.setBoolean(12, rsUpload.getBoolean("RATE"));
			stmtUploadInsert.setString(13, rsUpload.getString("TITLE"));
			stmtUploadInsert.setNull(14, Types.VARCHAR);
			stmtUploadInsert.setString(15, Videoresponse.values()[rsUpload.getInt("VIDEORESPONSE")].name());
			stmtUploadInsert.setString(16, Visibility.PRIVATE.name());
			stmtUploadInsert.setNull(17, Types.TIMESTAMP);
			stmtUploadInsert.setBoolean(18, false);
			stmtUploadInsert.setBoolean(19, false);
			stmtUploadInsert.setNull(20, Types.VARCHAR);
			stmtUploadInsert.setObject(21, rsUpload.getObject("ACCOUNT_ID"), Types.INTEGER);
			stmtUploadInsert.setString(22, rsUpload.getString("ENDDIR"));
			stmtUploadInsert.setString(23, License.values()[rsUpload.getString("LICENSE").equals("youtube")
															? 0
															: 1].name());
			stmtUploadInsert.setNull(24, Types.TIMESTAMP);
			stmtUploadInsert.setBoolean(25, false);
			stmtUploadInsert.setNull(26, Types.VARCHAR);
			stmtUploadInsert.setBoolean(27, false);
			stmtUploadInsert.setBoolean(28, false);
			stmtUploadInsert.setNull(29, Types.VARCHAR);
			stmtUploadInsert.setBoolean(30, false);
			stmtUploadInsert.setBoolean(31, false);
			stmtUploadInsert.setBoolean(32, false);
			stmtUploadInsert.setBoolean(33, false);
			stmtUploadInsert.setBoolean(34, false);
			stmtUploadInsert.setBoolean(35, false);
			stmtUploadInsert.setString(36, Syndication.GLOBAL.name());
			stmtUploadInsert.setNull(37, Types.VARCHAR);
			stmtUploadInsert.setNull(38, Types.VARCHAR);
			stmtUploadInsert.setNull(39, Types.VARCHAR);
			stmtUploadInsert.setNull(40, Types.VARCHAR);
			stmtUploadInsert.setNull(41, Types.VARCHAR);
			stmtUploadInsert.setNull(42, Types.VARCHAR);
			stmtUploadInsert.setNull(43, Types.VARCHAR);
			stmtUploadInsert.setNull(44, Types.VARCHAR);
			stmtUploadInsert.setNull(45, Types.VARCHAR);
			stmtUploadInsert.setNull(46, Types.VARCHAR);
			stmtUploadInsert.setString(47, ClaimType.AUDIO_VISUAL.name());
			stmtUploadInsert.setString(48, ClaimOption.MONETIZE.name());
			stmtUploadInsert.setString(49, Asset.WEB.name());
			stmtUploadInsert.setBoolean(50, false);
			stmtUploadInsert.setNull(51, Types.VARCHAR);
			stmtUploadInsert.execute();
		}
	}

	private static void convertV3() throws SQLException, IOException {
		try {
			Class.forName("org.h2.Driver");
		} catch (Exception e) {
			logger.error("ERROR: failed to load H2 JDBC driver.", e);
			return;
		}
		final Connection connectionOld = DriverManager.getConnection("jdbc:h2:" + ApplicationData.DATA_DIR + "/youtubeuploader", "username", "");
		final Connection connectionNew = getNewConnection();

		transferAccountsV3(connectionOld, connectionNew);
		transferTemplatesV3(connectionOld, connectionNew);
		transferUploadsV3(connectionOld, connectionNew);
	}

	private static void transferUploadsV3(final Connection connectionOld, final Connection connectionNew) throws SQLException {
		final Statement stmtUploadSelect = connectionOld.createStatement();
		final ResultSet rsUpload = stmtUploadSelect.executeQuery("SELECT * FROM UPLOADS WHERE ARCHIVED  = FALSE");

		final PreparedStatement stmtUploadInsert = connectionNew.prepareStatement("INSERT INTO UPLOAD (ARCHIVED, CATEGORY, COMMENT, COMMENTVOTE, DESCRIPTION, EMBED, FAILED, FILE, KEYWORDS, MIMETYPE, MOBILE, RATE, TITLE, UPLOADURL, VIDEORESPONSE, VISIBILITY, DATE_OF_START, INPROGRESS, LOCKED, VIDEOID, ACCOUNT_ID, ENDDIR, LICENSE, DATE_OF_RELEASE, PAUSEONFINISH, THUMBNAIL, FACEBOOK, TWITTER, MESSAGE, MONETIZE_INSTREAM_DEFAULTS, MONETIZE_CLAIM, MONETIZE_OVERLAY, MONETIZE_TRUEVIEW, MONETIZE_INSTREAM, MONETIZE_PRODUCT, MONETIZE_SYNDICATION, MONETIZE_TITLE, MONETIZE_DESCRIPTION, MONETIZE_ID, MONETIZE_NOTES, MONETIZE_TMSID, MONETIZE_ISAN, MONETIZE_EIDR, MONETIZE_TITLEEPISODE, MONETIZE_SEASON_NB, MONETIZE_EPISODE_NB, MONETIZE_CLAIMTYPE, MONETIZE_CLAIMOPTION, MONETIZE_ASSET, MONETIZE_PARTNER, STATUS) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		while (rsUpload.next()) {
			Category category = Category.GAMES;
			for (final Category cat : Category.values()) {
				if (cat.toCategory().term.equals(rsUpload.getString("CATEGORY"))) {
					category = cat;
				}
			}

			stmtUploadInsert.setBoolean(1, false);
			stmtUploadInsert.setString(2, category.name());
			stmtUploadInsert.setString(3, Comment.values()[rsUpload.getInt("COMMENT")].name());
			stmtUploadInsert.setBoolean(4, rsUpload.getBoolean("COMMENTVOTE"));
			stmtUploadInsert.setString(5, rsUpload.getString("DESCRIPTION"));
			stmtUploadInsert.setBoolean(6, rsUpload.getBoolean("EMBED"));
			stmtUploadInsert.setBoolean(7, false);
			stmtUploadInsert.setString(8, rsUpload.getString("FILE"));
			stmtUploadInsert.setString(9, rsUpload.getString("KEYWORDS"));
			stmtUploadInsert.setString(10, rsUpload.getString("MIMETYPE"));
			stmtUploadInsert.setBoolean(11, rsUpload.getBoolean("MOBILE"));
			stmtUploadInsert.setBoolean(12, rsUpload.getBoolean("RATE"));
			stmtUploadInsert.setString(13, rsUpload.getString("TITLE"));
			stmtUploadInsert.setNull(14, Types.VARCHAR);
			stmtUploadInsert.setString(15, Videoresponse.values()[rsUpload.getInt("VIDEORESPONSE")].name());
			stmtUploadInsert.setString(16, Visibility.values()[rsUpload.getInt("VISIBILITY")].name());
			stmtUploadInsert.setNull(17, Types.TIMESTAMP);
			stmtUploadInsert.setBoolean(18, false);
			stmtUploadInsert.setBoolean(19, false);
			stmtUploadInsert.setNull(20, Types.VARCHAR);
			stmtUploadInsert.setObject(21, rsUpload.getObject("ACCOUNT_ID"), Types.INTEGER);
			stmtUploadInsert.setString(22, rsUpload.getString("ENDDIR"));
			stmtUploadInsert.setString(23, License.values()[rsUpload.getString("LICENSE").equals("youtube")
															? 0
															: 1].name());
			stmtUploadInsert.setNull(24, Types.TIMESTAMP);
			stmtUploadInsert.setBoolean(25, rsUpload.getBoolean("PAUSEONFINISH"));
			stmtUploadInsert.setString(26, rsUpload.getString("THUMBNAIL"));
			stmtUploadInsert.setBoolean(27, rsUpload.getBoolean("FACEBOOK"));
			stmtUploadInsert.setBoolean(28, rsUpload.getBoolean("TWITTER"));
			stmtUploadInsert.setString(29, rsUpload.getString("MESSAGE"));
			stmtUploadInsert.setBoolean(30, rsUpload.getBoolean("INSTREAMDEFAULTS"));
			stmtUploadInsert.setBoolean(31, rsUpload.getBoolean("CLAIM"));
			stmtUploadInsert.setBoolean(32, rsUpload.getBoolean("OVERLAY"));
			stmtUploadInsert.setBoolean(33, rsUpload.getBoolean("TRUEVIEW"));
			stmtUploadInsert.setBoolean(34, rsUpload.getBoolean("INSTREAM"));
			stmtUploadInsert.setBoolean(35, rsUpload.getBoolean("PRODUCT"));
			stmtUploadInsert.setString(36, Syndication.values()[rsUpload.getInt("SYNDICATION")].name());
			stmtUploadInsert.setString(37, rsUpload.getString("MONETIZETITLE"));
			stmtUploadInsert.setString(38, rsUpload.getString("MONETIZEDESCRIPTION"));
			stmtUploadInsert.setString(39, rsUpload.getString("MONETIZEID"));
			stmtUploadInsert.setString(40, rsUpload.getString("MONETIZENOTES"));
			stmtUploadInsert.setString(41, rsUpload.getString("MONETIZETMSID"));
			stmtUploadInsert.setString(42, rsUpload.getString("MONETIZEISAN"));
			stmtUploadInsert.setString(43, rsUpload.getString("MONETIZEEIDR"));
			stmtUploadInsert.setString(44, rsUpload.getString("MONETIZETITLEEPISODE"));
			stmtUploadInsert.setString(45, rsUpload.getString("MONETIZESEASONNB"));
			stmtUploadInsert.setString(46, rsUpload.getString("MONETIZEEPISODENB"));
			stmtUploadInsert.setString(47, ClaimType.values()[rsUpload.getInt("MONETIZECLAIMTYPE")].name());
			stmtUploadInsert.setString(48, ClaimOption.values()[rsUpload.getInt("MONETIZECLAIMPOLICY")].name());
			stmtUploadInsert.setString(49, Asset.values()[rsUpload.getInt("MONETIZEASSET")].name());
			stmtUploadInsert.setBoolean(50, rsUpload.getBoolean("MONETIZEPARTNER"));
			stmtUploadInsert.setNull(51, Types.VARCHAR);
			stmtUploadInsert.execute();
		}
	}

	private static void transferTemplatesV3(final Connection connectionOld, final Connection connectionNew) throws SQLException {
		final Statement stmtPresetSelect = connectionOld.createStatement();
		final ResultSet rsTemplate = stmtPresetSelect.executeQuery("SELECT * FROM TEMPLATES");

		final PreparedStatement stmtPresetInsert = connectionNew.prepareStatement("INSERT INTO TEMPLATE (CATEGORY, COMMENT, COMMENTVOTE, DEFAULTDIR, DESCRIPTION, EMBED, KEYWORDS, MOBILE, NAME, RATE, VIDEORESPONSE, VISIBILITY, ACCOUNT_ID, ENDDIR, LICENSE, TITLE, THUMBNAIL, FACEBOOK, TWITTER, MESSAGE, MONETIZE_INSTREAM_DEFAULTS, MONETIZE_CLAIM, MONETIZE_OVERLAY, MONETIZE_TRUEVIEW, MONETIZE_INSTREAM, MONETIZE_PRODUCT, MONETIZE_SYNDICATION, MONETIZE_TITLE, MONETIZE_DESCRIPTION, MONETIZE_ID, MONETIZE_NOTES, MONETIZE_TMSID, MONETIZE_ISAN, MONETIZE_EIDR, MONETIZE_TITLEEPISODE, MONETIZE_SEASON_NB, MONETIZE_EPISODE_NB, MONETIZE_CLAIMTYPE, MONETIZE_CLAIMOPTION, MONETIZE_ASSET, MONETIZE_PARTNER) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

		while (rsTemplate.next()) {
			Category category = Category.GAMES;
			for (final Category cat : Category.values()) {
				if (cat.toCategory().term.equals(rsTemplate.getString("CATEGORY"))) {
					category = cat;
				}
			}

			stmtPresetInsert.setString(1, category.name());
			stmtPresetInsert.setString(2, Comment.values()[rsTemplate.getInt("COMMENT")].name());
			stmtPresetInsert.setBoolean(3, rsTemplate.getBoolean("COMMENTVOTE"));
			stmtPresetInsert.setString(4, rsTemplate.getString("DEFAULTDIR"));
			stmtPresetInsert.setString(5, rsTemplate.getString("DESCRIPTION"));
			stmtPresetInsert.setBoolean(6, rsTemplate.getBoolean("EMBED"));
			stmtPresetInsert.setString(7, rsTemplate.getString("KEYWORDS"));
			stmtPresetInsert.setBoolean(8, rsTemplate.getBoolean("MOBILE"));
			stmtPresetInsert.setString(9, rsTemplate.getString("NAME"));
			stmtPresetInsert.setBoolean(10, rsTemplate.getBoolean("RATE"));
			stmtPresetInsert.setString(11, Videoresponse.values()[rsTemplate.getInt("VIDEORESPONSE")].name());
			stmtPresetInsert.setString(12, Visibility.values()[rsTemplate.getInt("VISIBILITY")].name());
			stmtPresetInsert.setObject(13, rsTemplate.getObject("ACCOUNT_ID"), Types.INTEGER);
			stmtPresetInsert.setString(14, rsTemplate.getString("ENDDIR"));
			stmtPresetInsert.setString(15, License.values()[rsTemplate.getString("LICENSE").equals("youtube")
															? 0
															: 1].name());
			stmtPresetInsert.setString(16, rsTemplate.getString("TITLE"));
			stmtPresetInsert.setString(17, rsTemplate.getString("THUMBNAIL"));
			stmtPresetInsert.setBoolean(18, rsTemplate.getBoolean("FACEBOOK"));
			stmtPresetInsert.setBoolean(19, rsTemplate.getBoolean("TWITTER"));
			stmtPresetInsert.setString(20, rsTemplate.getString("MESSAGE"));
			stmtPresetInsert.setBoolean(21, rsTemplate.getBoolean("INSTREAMDEFAULTS"));
			stmtPresetInsert.setBoolean(22, rsTemplate.getBoolean("CLAIM"));
			stmtPresetInsert.setBoolean(23, rsTemplate.getBoolean("OVERLAY"));
			stmtPresetInsert.setBoolean(24, rsTemplate.getBoolean("TRUEVIEW"));
			stmtPresetInsert.setBoolean(25, rsTemplate.getBoolean("INSTREAM"));
			stmtPresetInsert.setBoolean(26, rsTemplate.getBoolean("PRODUCT"));
			stmtPresetInsert.setString(27, Syndication.values()[rsTemplate.getInt("SYNDICATION")].name());
			stmtPresetInsert.setString(28, rsTemplate.getString("MONETIZETITLE"));
			stmtPresetInsert.setString(29, rsTemplate.getString("MONETIZEDESCRIPTION"));
			stmtPresetInsert.setString(30, rsTemplate.getString("MONETIZEID"));
			stmtPresetInsert.setString(31, rsTemplate.getString("MONETIZENOTES"));
			stmtPresetInsert.setString(32, rsTemplate.getString("MONETIZETMSID"));
			stmtPresetInsert.setString(33, rsTemplate.getString("MONETIZEISAN"));
			stmtPresetInsert.setString(34, rsTemplate.getString("MONETIZEEIDR"));
			stmtPresetInsert.setString(35, rsTemplate.getString("MONETIZETITLEEPISODE"));
			stmtPresetInsert.setString(36, rsTemplate.getString("MONETIZESEASONNB"));
			stmtPresetInsert.setString(37, rsTemplate.getString("MONETIZEEPISODENB"));
			stmtPresetInsert.setString(38, ClaimType.values()[rsTemplate.getInt("MONETIZECLAIMTYPE")].name());
			stmtPresetInsert.setString(39, ClaimOption.values()[rsTemplate.getInt("MONETIZECLAIMPOLICY")].name());
			stmtPresetInsert.setString(40, Asset.values()[rsTemplate.getInt("MONETIZEASSET")].name());
			stmtPresetInsert.setBoolean(41, rsTemplate.getBoolean("MONETIZEPARTNER"));
			stmtPresetInsert.execute();
		}
	}

	private static void transferAccountsV3(final Connection connectionOld, final Connection connectionNew) throws SQLException {
		final Statement stmtAccountSelect = connectionOld.createStatement();
		final ResultSet rsAccount = stmtAccountSelect.executeQuery("SELECT * FROM ACCOUNTS WHERE TYPE = 'YOUTUBE'");

		final PreparedStatement stmtAccountInsert = connectionNew.prepareStatement("INSERT INTO ACCOUNT (ID, NAME, PASSWORD) VALUES(?,?,?)");
		while (rsAccount.next()) {
			stmtAccountInsert.setInt(1, rsAccount.getInt("ID"));
			stmtAccountInsert.setString(2, rsAccount.getString("NAME"));
			stmtAccountInsert.setString(3, rsAccount.getString("PASSWORD"));
			stmtAccountInsert.execute();
		}
	}

	public static void copyFolder(final File src, final File dest) throws IOException {

		if (src.isDirectory()) {

			//if directory not exists, create it
			if (!dest.exists() && dest.mkdir()) {
				System.out.println("Directory copied from " + src + "  to " + dest);
			} else {
				return;
			}

			//list all the directory contents
			final String[] files = src.list();

			for (final String file : files) {
				//construct the src and dest file structure
				final File srcFile = new File(src, file);
				final File destFile = new File(dest, file);
				//recursive copy
				copyFolder(srcFile, destFile);
			}

		} else {
			//if file, then copy it
			//Use bytes stream to support all file types

			try (final InputStream in = new FileInputStream(src);
				 final OutputStream out = new FileOutputStream(dest)) {

				final byte[] buffer = new byte[1024];

				int length;
				//copy the file content in bytes
				while ((length = in.read(buffer)) > 0) {
					out.write(buffer, 0, length);
				}

				in.close();
				out.close();
				System.out.println("File copied from " + src + " to " + dest);
			}
		}
	}

	public static void deleteRecursive(final File path) throws FileNotFoundException {
		if (!path.exists()) {
			throw new FileNotFoundException(path.getAbsolutePath());
		}
		if (path.isDirectory()) {
			final File[] files = path.listFiles();
			if (files != null) {
				for (final File f : files) {
					deleteRecursive(f);
				}
			}
		}
		path.deleteOnExit();
	}
}