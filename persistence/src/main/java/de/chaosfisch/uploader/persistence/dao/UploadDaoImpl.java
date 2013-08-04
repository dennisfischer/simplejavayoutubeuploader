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

import javax.persistence.EntityManager;
import java.util.GregorianCalendar;
import java.util.List;

public class UploadDaoImpl implements IUploadDao {

	@Inject
	protected EntityManager entityManager;

	@Override
	public List<Upload> getAll() {
		return (List<Upload>) entityManager.createQuery("SELECT u FROM upload u").getResultList();
	}

	@Override
	public Upload get(final int id) {
		return entityManager.find(Upload.class, id);
	}

	@Override
	public void insert(final Upload upload) {
		entityManager.persist(upload);
	}

	@Override
	public void update(final Upload upload) {
		entityManager.persist(upload);
	}

	@Override
	public void delete(final Upload upload) {
		entityManager.remove(upload);
	}

	@Override
	public Upload fetchNextUpload() {
		final GregorianCalendar cal = new GregorianCalendar();

		return (Upload) entityManager.createQuery("SELECT u FROM upload u " +
				"WHERE archived <> true AND failed <> true AND running <> true AND locked <> true AND (dateOfStart >= :dateOfStart OR dateOfStart IS NULL) " +
				"ORDER BY dateOfStart DESC, FAILED ASC").setParameter("dateOfStart", cal).getSingleResult();
	}

	@Override
	public int count() {
		return getAll().size();
	}

	@Override
	public int countUnprocessed() {
		return entityManager.createQuery("SELECT s FROM status s WHERE s.archived <> true AND s.failed <> true")
				.getResultList()
				.size();
	}

	@Override
	public int countReadyStarttime() {
		final GregorianCalendar cal = new GregorianCalendar();

		return entityManager.createQuery("SELECT u FROM upload WHERE archived <> true AND running <> true AND failed <> true AND dateOfStart >= :dateOfStart")
				.setParameter("dateOfStart", cal)
				.getResultList()
				.size();
	}

	@Override
	public void resetUnfinishedUploads() {
		try {
			entityManager.getTransaction().begin();
			entityManager.createQuery("UPDATE status s SET s.running = false, s.failed = false WHERE s.archived <> true")
					.executeUpdate();
			entityManager.getTransaction().commit();
		} catch (Exception e) {
			entityManager.getTransaction().rollback();
		}
	}

	@Override
	public List<Upload> fetchByArchived(final boolean archived) {
		return (List<Upload>) entityManager.createQuery("SELECT u FROM upload u WHERE archvied = true").getResultList();
	}
}
