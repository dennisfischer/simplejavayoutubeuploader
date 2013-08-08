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
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.youtube.playlist.Playlist;
import de.chaosfisch.uploader.persistence.dao.transactional.Transactional;

import javax.persistence.EntityManager;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class PlaylistDaoImpl implements IPlaylistDao {

	@Inject
	protected EntityManager entityManager;

	@Override
	public List<Playlist> getAll(final Account account) {
		return entityManager.createQuery("SELECT p FROM playlist p", Playlist.class).getResultList();
	}

	@Override
	public Playlist get(final int id) {
		return entityManager.find(Playlist.class, id);
	}

	@Override
	@Transactional
	public void insert(final Playlist playlist) {
		entityManager.persist(playlist);
	}

	@Override
	@Transactional
	public void update(final Playlist playlist) {
		entityManager.merge(playlist);
	}

	@Override
	@Transactional
	public void delete(final Playlist playlist) {
		entityManager.remove(playlist);
	}

	@Override
	@Transactional
	public void cleanByAccount(final Account account) {
		final GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.add(Calendar.MINUTE, -5);
		entityManager.createQuery("DELETE p FROM playlist p WHERE p.dateOfModified >= :dateOfModified AND p.account = :account")
				.setParameter("dateOfModified", cal)
				.setParameter("account", account)
				.executeUpdate();
	}

	@Override
	public List<Playlist> fetchByHidden(final Account account, final boolean hidden) {

		return entityManager.createQuery("SELECT p FROM playlist p WHERE p.hidden = :hidden AND p.account = :account", Playlist.class)
				.setParameter("hidden", hidden)
				.setParameter("account", account)
				.getResultList();
	}

	@Override
	public Playlist fetchByPKey(final String playlistKey) {
		return entityManager.createQuery("SELECT p FROM playlist p WHERE p.pkey = :pkey", Playlist.class)
				.setParameter("pkey", playlistKey)
				.getSingleResult();
	}
}
