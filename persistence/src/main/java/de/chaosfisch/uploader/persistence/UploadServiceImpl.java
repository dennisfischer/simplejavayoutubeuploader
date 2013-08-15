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
import de.chaosfisch.uploader.persistence.dao.IUploadDao;

import java.util.List;

public class UploadServiceImpl extends AbstractUploadService {

	private final IUploadDao uploadDao;

	@Inject
	public UploadServiceImpl(final IMetadataService metadataService, final Uploader uploader, final IUploadDao uploadDao) {
		super(metadataService, uploader);
		this.uploadDao = uploadDao;
		uploader.setUploadService(this);
	}

	@Override
	public List<Upload> getAll() {
		return uploadDao.getAll();
	}

	@Override
	public Upload get(final String id) {
		return uploadDao.get(id);
	}

	@Override
	public void insert(final Upload upload) {
		uploadDao.insert(upload);
	}

	@Override
	public void update(final Upload upload) {
		uploadDao.update(upload);
	}

	@Override
	public void delete(final Upload upload) {
		uploadDao.delete(upload);
	}

	@Override
	public Upload fetchNextUpload() {
		return uploadDao.fetchNextUpload();
	}

	@Override
	public int count() {
		return uploadDao.count();
	}

	@Override
	public int countUnprocessed() {
		return uploadDao.countUnprocessed();
	}

	@Override
	public long countReadyStarttime() {
		return uploadDao.countReadyStarttime();
	}

	@Override
	public void resetUnfinishedUploads() {
		uploadDao.resetUnfinishedUploads();
	}

	@Override
	public List<Upload> fetchByArchived(final boolean archived) {
		return uploadDao.fetchByArchived(archived);
	}

	@Override
	public long getStarttimeDelay() {
		return uploadDao.fetchStarttimeDelay();
	}
}
