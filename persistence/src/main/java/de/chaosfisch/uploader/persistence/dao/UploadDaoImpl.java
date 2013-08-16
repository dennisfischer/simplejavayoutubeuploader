/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.persistence.dao;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import de.chaosfisch.google.youtube.upload.Status;
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.google.youtube.upload.events.UploadAdded;
import de.chaosfisch.google.youtube.upload.events.UploadRemoved;
import de.chaosfisch.google.youtube.upload.events.UploadUpdated;

import java.util.*;

class UploadDaoImpl implements IUploadDao {

	private final List<Upload> uploads = new ArrayList<>(10);

	@Inject
	protected EventBus            eventBus;
	@Inject
	protected IPersistenceService persistenceService;

	@Override
	public List<Upload> getAll() {
		return uploads;
	}

	@Override
	public Upload get(final String id) {
		for (final Upload upload : uploads) {
			if (upload.getId().equals(id)) {
				return upload;
			}
		}
		return null;
	}

	@Override
	public Upload fetchNextUpload() {
		if (uploads.isEmpty()) {
			return null;
		}
		final long timestamp = System.currentTimeMillis();

		final List<Upload> result = new ArrayList<>(uploads.size());
		for (final Upload upload : uploads) {
			if (null == upload.getDateOfStart() || upload.getDateOfStart().getTimeInMillis() <= timestamp) {
				final Status status = upload.getStatus();
				if (!status.isArchived() && !status.isFailed() && !status.isRunning() && !status.isLocked()) {
					result.add(upload);
				}
			}
		}

		Collections.sort(result, new Comparator<Upload>() {
			@Override
			public int compare(final Upload o1, final Upload o2) {
				final boolean o1Null = null == o1.getDateOfStart();
				final boolean o2Null = null == o2.getDateOfStart();
				if (o1Null || o2Null) {
					return o1Null && o2Null ? 0 : o1Null ? -1 : 1;
				}
				return Long.compare(o1.getDateOfStart().getTimeInMillis(), o2.getDateOfStart().getTimeInMillis());
			}
		});

		return result.get(0);
	}

	@Override
	public List<Upload> fetchByArchived(final boolean archived) {
		final List<Upload> result = new ArrayList<>(uploads.size());
		for (final Upload upload : uploads) {
			final Status status = upload.getStatus();
			if (status.isArchived() == archived) {
				result.add(upload);
			}
		}
		return result;
	}

	@Override
	public long fetchStarttimeDelay() {
		if (uploads.isEmpty()) {
			return 0;
		}

		final long time = System.currentTimeMillis();
		long delay = time;

		for (final Upload upload : uploads) {
			final Status status = upload.getStatus();
			if (null != upload.getDateOfStart() && !status.isArchived() && !status.isFailed() && !status.isRunning() && !status
					.isLocked()) {
				if (upload.getDateOfStart().getTimeInMillis() < delay) {
					delay = upload.getDateOfStart().getTimeInMillis();
				}
			}
		}
		return delay == time ? 0 : delay - System.currentTimeMillis();
	}

	@Override
	public void setUploads(final List<Upload> uploads) {
		this.uploads.clear();
		this.uploads.addAll(uploads);
	}

	@Override
	public List<Upload> getUploads() {
		return uploads;
	}

	@Override
	public void insert(final Upload upload) {
		upload.setId(UUID.randomUUID().toString());
		uploads.add(upload);
		persistenceService.saveToStorage();
		eventBus.post(new UploadAdded(upload));
	}

	@Override
	public void update(final Upload upload) {
		persistenceService.saveToStorage();
		eventBus.post(new UploadUpdated(upload));
	}

	@Override
	public void delete(final Upload upload) {
		uploads.remove(upload);
		persistenceService.saveToStorage();
		eventBus.post(new UploadRemoved(upload));
	}

	@Override
	public int count() {
		return getAll().size();
	}

	@Override
	public int countUnprocessed() {
		int count = 0;
		for (final Upload upload : uploads) {
			final Status status = upload.getStatus();
			if (!status.isArchived() && !status.isFailed() && !status.isLocked()) {
				count++;
			}
		}
		return count;
	}

	@Override
	public long countReadyStarttime() {
		final long time = System.currentTimeMillis();
		int count = 0;
		for (final Upload upload : uploads) {
			final Status status = upload.getStatus();
			if (!status.isArchived() && !status.isFailed() && !status.isLocked()) {
				if (null != upload.getDateOfStart() && upload.getDateOfStart().getTimeInMillis() <= time) {
					count++;
				}
			}
		}
		return count;
	}

	@Override
	public void resetUnfinishedUploads() {
		for (final Upload upload : uploads) {
			final Status status = upload.getStatus();
			if (!status.isArchived()) {
				status.setFailed(false);
				status.setRunning(false);
			}
		}
	}
}
