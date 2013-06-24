/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.youtubeuploader.db.triggers;

import de.chaosfisch.google.youtube.Playlist;
import de.chaosfisch.util.EventBusUtil;
import de.chaosfisch.youtubeuploader.db.events.ModelAddedEvent;
import de.chaosfisch.youtubeuploader.db.events.ModelRemovedEvent;
import de.chaosfisch.youtubeuploader.db.events.ModelUpdatedEvent;
import org.chaosfisch.youtubeuploader.db.generated.Tables;
import org.h2.tools.TriggerAdapter;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlaylistTrigger extends TriggerAdapter {
	private Integer firstId;
	private Integer lastId;

	@Override
	public void fire(final Connection conn, final ResultSet oldRow, final ResultSet newRow) throws SQLException {
		firstId = null;
		lastId = null;
		final DSLContext create = DSL.using(conn, SQLDialect.H2);
		if (null != newRow) {
			final Cursor<Record> result = create.fetchLazy(newRow, Tables.PLAYLIST.fields());
			while (result.hasNext()) {
				final Playlist record = getPlaylist(result);
				if (null == record) {
					return;
				}

				if (null == oldRow) {
					EventBusUtil.getInstance().post(new ModelAddedEvent(record));
				} else {
					EventBusUtil.getInstance().post(new ModelUpdatedEvent(record));
				}
			}
		} else {
			final Cursor<Record> result = create.fetchLazy(oldRow, Tables.PLAYLIST.fields());
			while (result.hasNext()) {
				final Playlist record = getPlaylist(result);
				if (null == record) {
					return;
				}

				EventBusUtil.getInstance().post(new ModelRemovedEvent(record));
			}
		}
	}

	private Playlist getPlaylist(final Cursor<Record> result) {
		final Playlist record = result.fetchOneInto(Playlist.class);
		lastId = record.getId();
		if (lastId.equals(firstId)) {
			return null;
		}
		if (null == firstId) {
			firstId = record.getId();
		}
		return record;
	}
}
