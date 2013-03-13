package org.chaosfisch.youtubeuploader.db.triggers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.chaosfisch.util.EventBusUtil;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist;
import org.chaosfisch.youtubeuploader.db.generated.tables.records.PlaylistRecord;
import org.chaosfisch.youtubeuploader.models.events.ModelPostRemovedEvent;
import org.chaosfisch.youtubeuploader.models.events.ModelPostSavedEvent;
import org.jooq.SQLDialect;
import org.jooq.impl.Executor;

public class PlaylistTrigger extends org.h2.tools.TriggerAdapter {

	@Override
	public void fire(final Connection conn, final ResultSet oldRow, final ResultSet newRow) throws SQLException {
		final Executor create = new Executor(conn, SQLDialect.H2);
		if (newRow == null) {
			final PlaylistRecord result = (PlaylistRecord) create.fetchOne(oldRow, PlaylistRecord.class);
			EventBusUtil.getInstance().post(new ModelPostRemovedEvent(result.into(Playlist.class)));
		} else {
			final PlaylistRecord result = (PlaylistRecord) create.fetchOne(newRow, PlaylistRecord.class);
			EventBusUtil.getInstance().post(new ModelPostSavedEvent(result.into(Playlist.class)));
		}
	}
}
