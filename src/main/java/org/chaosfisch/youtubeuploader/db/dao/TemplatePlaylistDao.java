package org.chaosfisch.youtubeuploader.db.dao;

import org.jooq.impl.Executor;

import com.google.inject.Inject;

public class TemplatePlaylistDao extends org.chaosfisch.youtubeuploader.db.generated.tables.daos.TemplatePlaylistDao {
	private final Executor	create;

	@Inject
	public TemplatePlaylistDao(final Executor create) {
		super(create);
		this.create = create;
	}
}
