package org.chaosfisch.youtubeuploader.db.dao;

import java.sql.Timestamp;

import org.chaosfisch.youtubeuploader.db.generated.Tables;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Account;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;
import org.jooq.impl.Executor;

import com.google.inject.Inject;

public class UploadDao extends org.chaosfisch.youtubeuploader.db.generated.tables.daos.UploadDao {
	private final Executor	create;

	@Inject
	public UploadDao(final Executor create) {
		super(create);
		this.create = create;
	}

	public Upload insertReturning(final Upload upload) {
		return create.insertInto(Tables.UPLOAD).set(create.newRecord(Tables.UPLOAD, upload)).returning().fetchOne().into(Upload.class);
	}

	public Account fetchOneAccountByUpload(final Upload upload) {
		return create
			.select()
			.from(Tables.ACCOUNT)
			.where(Tables.UPLOAD.ACCOUNT_ID.eq(Tables.ACCOUNT.ID), Tables.UPLOAD.ID.eq(upload.getId()))
			.fetchOneInto(Account.class);
	}

	public Upload fetchNextUpload() {
		return create
			.select()
			.from(Tables.UPLOAD)
			.where(
				Tables.UPLOAD.ARCHIVED.eq(false).or(Tables.UPLOAD.ARCHIVED.isNull()),
				Tables.UPLOAD.INPROGRESS.eq(false).or(Tables.UPLOAD.INPROGRESS.isNull()),
				Tables.UPLOAD.LOCKED.eq(false).or(Tables.UPLOAD.LOCKED.isNull()),
				Tables.UPLOAD.STARTED.le(new Timestamp(System.currentTimeMillis())).or(Tables.UPLOAD.STARTED.isNull()))
			.orderBy(Tables.UPLOAD.STARTED.desc(), Tables.UPLOAD.FAILED.asc())
			.fetchOneInto(Upload.class);
	}

	public long countLeftUploads() {
		return create
			.select()
			.where(Tables.UPLOAD.ARCHIVED.eq(false).or(Tables.UPLOAD.ARCHIVED.isNull()), Tables.UPLOAD.FAILED.eq(false))
			.fetchCount();
	}

	public long countAvailableStartingUploads() {
		return create
			.select()
			.where(
				Tables.UPLOAD.ARCHIVED.eq(false).or(Tables.UPLOAD.ARCHIVED.isNull()),
				Tables.UPLOAD.INPROGRESS.eq(false).or(Tables.UPLOAD.INPROGRESS.isNull()),
				Tables.UPLOAD.STARTED.le(new Timestamp(System.currentTimeMillis())))
			.fetchCount();
	}
}
