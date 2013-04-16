/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

/**
 * This class is generated by jOOQ
 */
package org.chaosfisch.youtubeuploader.db.generated.tables;

/** This class is generated by jOOQ. */
@javax.annotation.Generated(value = {"http://www.jooq.org", "3.0.0"},
		comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({"all", "unchecked"})
public class Playlist extends org.jooq.impl.TableImpl<org.chaosfisch.youtubeuploader.db.generated.tables.records.PlaylistRecord> {

	private static final long serialVersionUID = 571311239;

	/** The singleton instance of <code>PUBLIC.PLAYLIST</code> */
	public static final org.chaosfisch.youtubeuploader.db.generated.tables.Playlist PLAYLIST = new org.chaosfisch.youtubeuploader.db.generated.tables.Playlist();

	/** The class holding records for this type */
	@Override
	public java.lang.Class<org.chaosfisch.youtubeuploader.db.generated.tables.records.PlaylistRecord> getRecordType() {
		return org.chaosfisch.youtubeuploader.db.generated.tables.records.PlaylistRecord.class;
	}

	/** The column <code>PUBLIC.PLAYLIST.ID</code>. */
	public final org.jooq.TableField<org.chaosfisch.youtubeuploader.db.generated.tables.records.PlaylistRecord, java.lang.Integer> ID = createField("ID", org.jooq.impl.SQLDataType.INTEGER, this);

	/** The column <code>PUBLIC.PLAYLIST.PKEY</code>. */
	public final org.jooq.TableField<org.chaosfisch.youtubeuploader.db.generated.tables.records.PlaylistRecord, java.lang.String> PKEY = createField("PKEY", org
			.jooq
			.impl
			.SQLDataType
			.VARCHAR
			.length(255), this);

	/** The column <code>PUBLIC.PLAYLIST.PRIVATE</code>. */
	public final org.jooq.TableField<org.chaosfisch.youtubeuploader.db.generated.tables.records.PlaylistRecord, java.lang.Boolean> PRIVATE = createField("PRIVATE", org.jooq.impl.SQLDataType.BOOLEAN, this);

	/** The column <code>PUBLIC.PLAYLIST.TITLE</code>. */
	public final org.jooq.TableField<org.chaosfisch.youtubeuploader.db.generated.tables.records.PlaylistRecord, java.lang.String> TITLE = createField("TITLE", org
			.jooq
			.impl
			.SQLDataType
			.VARCHAR
			.length(255), this);

	/** The column <code>PUBLIC.PLAYLIST.URL</code>. */
	public final org.jooq.TableField<org.chaosfisch.youtubeuploader.db.generated.tables.records.PlaylistRecord, java.lang.String> URL = createField("URL", org
			.jooq
			.impl
			.SQLDataType
			.VARCHAR
			.length(255), this);

	/** The column <code>PUBLIC.PLAYLIST.THUMBNAIL</code>. */
	public final org.jooq.TableField<org.chaosfisch.youtubeuploader.db.generated.tables.records.PlaylistRecord, java.lang.String> THUMBNAIL = createField("THUMBNAIL", org
			.jooq
			.impl
			.SQLDataType
			.VARCHAR
			.length(255), this);

	/** The column <code>PUBLIC.PLAYLIST.NUMBER</code>. */
	public final org.jooq.TableField<org.chaosfisch.youtubeuploader.db.generated.tables.records.PlaylistRecord, java.lang.Integer> NUMBER = createField("NUMBER", org.jooq.impl.SQLDataType.INTEGER, this);

	/** The column <code>PUBLIC.PLAYLIST.SUMMARY</code>. */
	public final org.jooq.TableField<org.chaosfisch.youtubeuploader.db.generated.tables.records.PlaylistRecord, java.lang.String> SUMMARY = createField("SUMMARY", org
			.jooq
			.impl
			.SQLDataType
			.VARCHAR
			.length(5000), this);

	/** The column <code>PUBLIC.PLAYLIST.HIDDEN</code>. */
	public final org.jooq.TableField<org.chaosfisch.youtubeuploader.db.generated.tables.records.PlaylistRecord, java.lang.Boolean> HIDDEN = createField("HIDDEN", org.jooq.impl.SQLDataType.BOOLEAN, this);

	/** The column <code>PUBLIC.PLAYLIST.ACCOUNT_ID</code>. */
	public final org.jooq.TableField<org.chaosfisch.youtubeuploader.db.generated.tables.records.PlaylistRecord, java.lang.Integer> ACCOUNT_ID = createField("ACCOUNT_ID", org.jooq.impl.SQLDataType.INTEGER, this);

	/** The column <code>PUBLIC.PLAYLIST.DATE_OF_MODIFIED</code>. */
	public final org.jooq.TableField<org.chaosfisch.youtubeuploader.db.generated.tables.records.PlaylistRecord, java.util.Calendar> DATE_OF_MODIFIED = createField("DATE_OF_MODIFIED", org
			.jooq
			.impl
			.SQLDataType
			.TIMESTAMP
			.asConvertedDataType(new org.chaosfisch.youtubeuploader.db.converter.CalendarConverter()), this);

	/** Create a <code>PUBLIC.PLAYLIST</code> table reference */
	public Playlist() {
		super("PLAYLIST", org.chaosfisch.youtubeuploader.db.generated.Public.PUBLIC);
	}

	/** Create an aliased <code>PUBLIC.PLAYLIST</code> table reference */
	public Playlist(java.lang.String alias) {
		super(alias, org.chaosfisch.youtubeuploader.db.generated.Public.PUBLIC, org.chaosfisch.youtubeuploader.db.generated.tables.Playlist.PLAYLIST);
	}

	/** {@inheritDoc} */
	@Override
	public org.jooq.Identity<org.chaosfisch.youtubeuploader.db.generated.tables.records.PlaylistRecord, java.lang.Integer> getIdentity() {
		return org.chaosfisch.youtubeuploader.db.generated.Keys.IDENTITY_PLAYLIST;
	}

	/** {@inheritDoc} */
	@Override
	public org.jooq.UniqueKey<org.chaosfisch.youtubeuploader.db.generated.tables.records.PlaylistRecord> getPrimaryKey() {
		return org.chaosfisch.youtubeuploader.db.generated.Keys.CONSTRAINT_9;
	}

	/** {@inheritDoc} */
	@Override
	public java.util.List<org.jooq.UniqueKey<org.chaosfisch.youtubeuploader.db.generated.tables.records.PlaylistRecord>> getKeys() {
		return java.util
				.Arrays
				.<org.jooq.UniqueKey<org.chaosfisch.youtubeuploader.db.generated.tables.records.PlaylistRecord>>asList(org.chaosfisch.youtubeuploader.db.generated.Keys.CONSTRAINT_9);
	}

	/** {@inheritDoc} */
	@Override
	public java.util.List<org.jooq.ForeignKey<org.chaosfisch.youtubeuploader.db.generated.tables.records.PlaylistRecord, ?>> getReferences() {
		return java.util
				.Arrays
				.<org.jooq.ForeignKey<org.chaosfisch.youtubeuploader.db.generated.tables.records.PlaylistRecord, ?>>asList(org.chaosfisch.youtubeuploader.db.generated.Keys.CONSTRAINT_36);
	}

	/** {@inheritDoc} */
	@Override
	public org.chaosfisch.youtubeuploader.db.generated.tables.Playlist as(java.lang.String alias) {
		return new org.chaosfisch.youtubeuploader.db.generated.tables.Playlist(alias);
	}
}
