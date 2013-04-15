/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.db.triggers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.chaosfisch.util.EventBusUtil;
import org.chaosfisch.youtubeuploader.db.events.ModelAddedEvent;
import org.chaosfisch.youtubeuploader.db.events.ModelRemovedEvent;
import org.chaosfisch.youtubeuploader.db.events.ModelUpdatedEvent;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public class UploadTrigger extends org.h2.tools.TriggerAdapter {

	@Override
	public void fire(final Connection conn, final ResultSet oldRow, final ResultSet newRow) throws SQLException {
		final DSLContext create = DSL.using(conn, SQLDialect.H2);
		Integer firstId = null;
		Integer lastId = null;
		if (newRow != null) {
			final Cursor<Record> result = create.fetchLazy(newRow);
			while (result.hasNext()) {
				final Upload record = result.fetchOneInto(Upload.class);
				lastId = record.getId();
				if (lastId.equals(firstId)) {
					break;
				}
				if (firstId == null) {
					firstId = record.getId();
				}

				if (oldRow == null) {
					EventBusUtil.getInstance()
						.post(new ModelAddedEvent(record));
				} else {
					EventBusUtil.getInstance()
						.post(new ModelUpdatedEvent(record));
				}
			}
		} else {
			final Cursor<Record> result = create.fetchLazy(oldRow);
			while (result.hasNext()) {
				final Upload record = result.fetchOneInto(Upload.class);
				lastId = record.getId();
				lastId = record.getId();
				if (lastId.equals(firstId)) {
					break;
				}
				if (firstId == null) {
					firstId = record.getId();
				}

				EventBusUtil.getInstance()
					.post(new ModelRemovedEvent(record));
			}
		}
	}
}
