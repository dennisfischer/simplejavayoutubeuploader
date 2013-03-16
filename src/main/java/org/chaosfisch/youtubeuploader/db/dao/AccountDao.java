package org.chaosfisch.youtubeuploader.db.dao;

import org.jooq.impl.Executor;

import com.google.inject.Inject;

public class AccountDao extends org.chaosfisch.youtubeuploader.db.generated.tables.daos.AccountDao {

	private final Executor	create;

	@Inject
	public AccountDao(final Executor create) {
		super(create);
		this.create = create;
	}

}
