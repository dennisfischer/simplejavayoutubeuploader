package org.chaosfisch.youtubeuploader.db.dao;

import org.jooq.impl.Executor;

import com.google.inject.Inject;

public class UploadPlaylistDao extends org.chaosfisch.youtubeuploader.db.generated.tables.daos.UploadPlaylistDao {
	private final Executor	create;

	@Inject
	public UploadPlaylistDao(final Executor create) {
		super(create);
		this.create = create;
	}
}
