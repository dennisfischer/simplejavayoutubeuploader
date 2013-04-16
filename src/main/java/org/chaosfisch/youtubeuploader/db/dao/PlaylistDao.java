/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader.db.dao;

import com.google.inject.Inject;
import org.chaosfisch.youtubeuploader.db.generated.Tables;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Account;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class PlaylistDao extends org.chaosfisch.youtubeuploader.db.generated.tables.daos.PlaylistDao {

	private final DSLContext context;

	@Inject
	public PlaylistDao(final Configuration configuration) {
		super(configuration);
		context = DSL.using(configuration);
	}

	public Account fetchOneAccountByPlaylist(final Playlist playlist) {
		return context.select()
					  .from(Tables.ACCOUNT)
					  .join(Tables.PLAYLIST)
					  .on(Tables.PLAYLIST.ACCOUNT_ID.eq(Tables.ACCOUNT.ID))
					  .where(Tables.PLAYLIST.ID.eq(playlist.getId()))
					  .fetchOneInto(Account.class);
	}

	public List<Playlist> fetchByTemplate(final Template template) {
		return context.select()
					  .from(Tables.PLAYLIST)
					  .join(Tables.TEMPLATE_PLAYLIST)
					  .on(Tables.TEMPLATE_PLAYLIST.PLAYLIST_ID.eq(Tables.PLAYLIST.ID))
					  .where(Tables.TEMPLATE_PLAYLIST.TEMPLATE_ID.eq(template.getId()))
					  .fetchInto(Playlist.class);
	}

	public List<Playlist> fetchByUpload(final Upload upload) {
		return context.select()
					  .from(Tables.PLAYLIST)
					  .join(Tables.UPLOAD_PLAYLIST)
					  .on(Tables.UPLOAD_PLAYLIST.PLAYLIST_ID.eq(Tables.PLAYLIST.ID))
					  .where(Tables.UPLOAD_PLAYLIST.UPLOAD_ID.eq(upload.getId()))
					  .fetchInto(Playlist.class);
	}

	public void cleanByAccount(final Account account) {
		final GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.add(Calendar.MINUTE, -5);
		context.delete(Tables.PLAYLIST)
			   .where(Tables.PLAYLIST.DATE_OF_MODIFIED.le(cal), Tables.PLAYLIST.ACCOUNT_ID.eq(account.getId()))
			   .execute();
	}

	public List<Playlist> fetchUnhidden(final Integer id) {
		return context.select()
					  .from(Tables.PLAYLIST)
					  .where(Tables.PLAYLIST.ACCOUNT_ID.eq(id).and(Tables.PLAYLIST.HIDDEN.eq(false)))
					  .fetchInto(Playlist.class);
	}
}
