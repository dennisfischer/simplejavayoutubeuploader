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

import com.google.inject.Inject;
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.uploader.persistence.dao.transactional.Transactional;

import javax.persistence.EntityManager;
import java.util.GregorianCalendar;
import java.util.List;

public class UploadDaoImpl implements IUploadDao {

	@Inject
	protected EntityManager entityManager;

	@Override
	public List<Upload> getAll() {
		return entityManager.createQuery("SELECT u FROM upload u", Upload.class).getResultList();
	}

	@Override
	public Upload get(final int id) {
		return entityManager.find(Upload.class, id);
	}

	@Override
	@Transactional
	public void insert(final Upload upload) {
		entityManager.persist(upload);
	}

	@Override
	@Transactional
	public void update(final Upload upload) {
		entityManager.merge(upload);
	}

	@Override
	@Transactional
	public void delete(final Upload upload) {
		entityManager.remove(upload);
	}

	@Override
	public Upload fetchNextUpload() {
		final GregorianCalendar cal = new GregorianCalendar();

		return entityManager.createQuery("SELECT u FROM upload u, status s " +
				"WHERE s.archived <> true AND s.failed <> true AND s.running <> true AND s.locked <> true AND (s.dateOfStart >= :dateOfStart OR s.dateOfStart IS NULL) " +
				"ORDER BY s.dateOfStart DESC, s.failed ASC", Upload.class)
				.setParameter("dateOfStart", cal)
				.getSingleResult();
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
	public int countReadyStarttime() {
		final GregorianCalendar cal = new GregorianCalendar();

		return entityManager.createQuery("SELECT COUNT(s) FROM status s WHERE s.archived <> true AND s.running <> true AND s.failed <> true AND s.dateOfStart >= :dateOfStart", Integer.class)
				.setParameter("dateOfStart", cal)
				.getSingleResult();
	}

	@Override
	@Transactional
	public void resetUnfinishedUploads() {
		entityManager.createQuery("UPDATE status s SET s.running = false, s.failed = false WHERE s.archived <> true")
				.executeUpdate();
	}

	@Override
	public List<Upload> fetchByArchived(final boolean archived) {
		return entityManager.createQuery("SELECT u FROM upload u WHERE archvied = true", Upload.class).getResultList();
	}
}
