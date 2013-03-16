package org.chaosfisch.youtubeuploader.db.dao;

import org.jooq.impl.Executor;

import com.google.inject.Inject;

public class TemplateDao extends org.chaosfisch.youtubeuploader.db.generated.tables.daos.TemplateDao {

	private final Executor	create;

	@Inject
	public TemplateDao(final Executor create) {
		super(create);
		this.create = create;
	}

}
