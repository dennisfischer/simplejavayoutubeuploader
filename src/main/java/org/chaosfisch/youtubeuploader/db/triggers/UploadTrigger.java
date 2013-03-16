package org.chaosfisch.youtubeuploader.db.triggers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.chaosfisch.util.EventBusUtil;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;
import org.chaosfisch.youtubeuploader.models.events.ModelPostRemovedEvent;
import org.chaosfisch.youtubeuploader.models.events.ModelPostSavedEvent;
import org.jooq.Cursor;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.Executor;

public class UploadTrigger extends org.h2.tools.TriggerAdapter {

	@Override
	public void fire(final Connection conn, final ResultSet oldRow, final ResultSet newRow) throws SQLException {
		final Settings settings = new Settings();
		settings.setExecuteLogging(false);
		final Executor create = new Executor(conn, SQLDialect.H2, settings);
		Integer firstId = null;
		Integer lastId = null;
		if (newRow != null) {
			final Cursor<Record> result = create.fetchLazy(newRow);
			while (result.hasNext()) {
				final Upload record = result.fetchOne().into(Upload.class);
				lastId = record.getId();
				if (lastId == firstId) {
					break;
				}
				if (firstId == null) {
					firstId = record.getId();
				}

				EventBusUtil.getInstance().post(new ModelPostSavedEvent(record));
			}
		} else {
			final Cursor<Record> result = create.fetchLazy(oldRow);
			while (result.hasNext()) {
				final Upload record = result.fetchOne().into(Upload.class);
				lastId = record.getId();
				lastId = record.getId();
				if (lastId == firstId) {
					break;
				}
				if (firstId == null) {
					firstId = record.getId();
				}

				EventBusUtil.getInstance().post(new ModelPostRemovedEvent(record));
			}
		}
	}
}
