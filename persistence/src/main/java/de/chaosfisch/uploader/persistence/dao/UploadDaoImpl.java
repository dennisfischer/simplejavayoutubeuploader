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
import com.google.inject.persist.Transactional;
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.google.youtube.upload.events.UploadAdded;
import de.chaosfisch.google.youtube.upload.events.UploadRemoved;
import de.chaosfisch.google.youtube.upload.events.UploadUpdated;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class UploadDaoImpl implements IUploadDao {

	private final ArrayList<Upload> uploads = new ArrayList<>(10);

	@Inject
	protected EntityManager entityManager;
	@Inject
	protected EventBus      eventBus;

	@Override
	public List<Upload> getAll() {
		final List<Upload> result = entityManager.createQuery("SELECT u FROM upload u", Upload.class).getResultList();
		for (final Upload upload : result) {
			addOrUpdateUpload(upload);
		}
		return uploads;
	}

	@Override
	public Upload get(final int id) {
		final Upload upload = entityManager.find(Upload.class, id);
		addOrUpdateUpload(upload);
		return getUploadFromList(upload);
	}

	@Override
	public Upload fetchNextUpload() {
		final GregorianCalendar cal = new GregorianCalendar();

		final Upload upload = entityManager.createQuery("SELECT u FROM upload u, status s " +
				"WHERE s.archived <> true AND s.failed <> true AND s.running <> true AND s.locked <> true AND (u.dateOfStart <= :dateOfStart OR u.dateOfStart IS NULL) " +
				"ORDER BY u.dateOfStart DESC, s.failed ASC", Upload.class)
				.setParameter("dateOfStart", cal)
				.getSingleResult();

		addOrUpdateUpload(upload);
		return getUploadFromList(upload);
	}

	@Override
	public List<Upload> fetchByArchived(final boolean archived) {
		final List<Upload> result = entityManager.createQuery("SELECT u FROM upload u, status s WHERE u.status = s AND s.archived = true", Upload.class)
				.getResultList();

		final ArrayList<Upload> tmp = new ArrayList<>();
		for (final Upload upload : result) {
			addOrUpdateUpload(upload);
			tmp.add(upload);
		}
		return tmp;
	}

	@Override
	public long fetchStarttimeDelay() {
		final List<Upload> result = entityManager.createQuery("SELECT u FROM upload u, status s WHERE s.archived <> true AND s.failed <> true AND s.locked <> true AND NOT u.dateOfStart IS NULL ORDER BY u.dateOfStart ASC", Upload.class)
				.setMaxResults(1)
				.getResultList();
		if (result.isEmpty()) {
			return 0;
		} else {
			return result.get(0).getDateOfStart().getTimeInMillis() - System.currentTimeMillis();
		}
	}

	@Override
	@Transactional
	public void insert(final Upload upload) {
		entityManager.persist(upload);
		uploads.add(upload);
		eventBus.post(new UploadAdded(upload));
	}

	@Override
	@Transactional
	public void update(final Upload upload) {
		entityManager.merge(upload);
		eventBus.post(new UploadUpdated(upload));
	}

	@Override
	@Transactional
	public void delete(final Upload upload) {
		entityManager.remove(upload);
		uploads.remove(upload);
		eventBus.post(new UploadRemoved(upload));
	}

	@Override
	public int count() {
		return getAll().size();
	}

	@Override
	public int countUnprocessed() {
		return entityManager.createQuery("SELECT COUNT(s) FROM status s WHERE s.archived <> true AND s.failed <> true", Integer.class)
				.getSingleResult();
	}

	@Override
	public long countReadyStarttime() {
		final GregorianCalendar cal = new GregorianCalendar();

		return entityManager.createQuery("SELECT COUNT(s) FROM upload u, status s WHERE s.archived <> true AND s.running <> true AND s.failed <> true AND u.dateOfStart <= :dateOfStart", Long.class)
				.setParameter("dateOfStart", cal)
				.getSingleResult();
	}

	@Override
	public void resetUnfinishedUploads() {
		entityManager.getTransaction().begin();
		entityManager.createQuery("UPDATE status s SET s.running = false, s.failed = false WHERE s.archived <> true")
				.executeUpdate();
		entityManager.getTransaction().commit();
	}

	private void addOrUpdateUpload(final Upload upload) {
		if (uploads.contains(upload)) {
			refreshUpload(upload);
		} else {
			uploads.add(upload);
		}
	}

	private void refreshUpload(final Upload upload) {
		entityManager.refresh(getUploadFromList(upload));
	}

	private Upload getUploadFromList(final Upload upload) {
		return uploads.get(uploads.indexOf(upload));
	}
}
