package org.chaosfisch.youtubeuploader.db.triggers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.chaosfisch.util.EventBusUtil;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template;
import org.chaosfisch.youtubeuploader.db.generated.tables.records.TemplateRecord;
import org.chaosfisch.youtubeuploader.models.events.ModelPostRemovedEvent;
import org.chaosfisch.youtubeuploader.models.events.ModelPostSavedEvent;
import org.jooq.SQLDialect;
import org.jooq.impl.Executor;

public class TemplateTrigger extends org.h2.tools.TriggerAdapter {

	@Override
	public void fire(final Connection conn, final ResultSet oldRow, final ResultSet newRow) throws SQLException {
		final Executor create = new Executor(conn, SQLDialect.H2);
		if (newRow == null) {
			final TemplateRecord result = (TemplateRecord) create.fetchOne(oldRow, TemplateRecord.class);
			EventBusUtil.getInstance().post(new ModelPostRemovedEvent(result.into(Template.class)));
		} else {
			final TemplateRecord result = (TemplateRecord) create.fetchOne(newRow, TemplateRecord.class);
			EventBusUtil.getInstance().post(new ModelPostSavedEvent(result.into(Template.class)));
		}
	}
}
