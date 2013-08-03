/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.persistence;

import com.google.inject.Inject;
import de.chaosfisch.google.youtube.upload.AbstractUploadService;
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.google.youtube.upload.Uploader;
import de.chaosfisch.google.youtube.upload.metadata.IMetadataService;

import java.util.List;

public class UploadServiceImpl extends AbstractUploadService {

	@Inject
	public UploadServiceImpl(final IMetadataService metadataService, final Uploader uploader) {
		super(metadataService, uploader);
		uploader.setUploadService(this);
	}

	@Override
	public List<Upload> getAll() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Upload get(final int id) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void insert(final Upload upload) {
		/*
		NEEDED ? FIXME
			return context.insertInto(Tables.UPLOAD)
				.set(context.newRecord(Tables.UPLOAD, upload))
				.returning()
				.fetchOne()
				.into(Upload.class);
		 */

		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void update(final Upload upload) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void delete(final Upload upload) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Upload findNextUpload() {
		/*
				final GregorianCalendar cal = new GregorianCalendar();

		return context.select()
				.from(Tables.UPLOAD)
				.where(Tables.UPLOAD.ARCHIVED.ne(true), Tables.UPLOAD.FAILED.ne(true), Tables.UPLOAD
						.INPROGRESS
						.ne(true), Tables.UPLOAD.LOCKED.ne(true), Tables.UPLOAD
						.DATE_OF_START
						.le(cal)
						.or(Tables.UPLOAD.DATE_OF_START.isNull()))
				.orderBy(Tables.UPLOAD.DATE_OF_START.desc(), Tables.UPLOAD.FAILED.asc())
				.limit(1)
				.fetchOneInto(Upload.class);
		 */

		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public int count() {
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public int countUnprocessed() {
		/*
		return context.select()
				.from(Tables.UPLOAD)
				.where(Tables.UPLOAD.ARCHIVED.ne(true), Tables.UPLOAD.FAILED.eq(false))
				.fetchCount();
		 */
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public int countReadyStarttime() {
		/*
		final GregorianCalendar cal = new GregorianCalendar();

		return context.select()
				.from(Tables.UPLOAD)
				.where(Tables.UPLOAD.ARCHIVED.ne(true), Tables.UPLOAD.INPROGRESS.ne(true), Tables.UPLOAD
						.FAILED
						.ne(true), Tables.UPLOAD.DATE_OF_START.le(cal))
				.fetchCount();
		 */
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void resetUnfinishedUploads() {
		//FIXME
		/*
		DSL.using(injector.getInstance(Configuration.class))
				.update(Tables.UPLOAD)
				.set(Tables.UPLOAD.INPROGRESS, false)
				.set(Tables.UPLOAD.FAILED, false)
				.execute();     */
	}

	@Override
	public Upload fetchByArchived(final boolean archived) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
