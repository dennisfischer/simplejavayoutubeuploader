/*
 * Copyright (c) 2012, Dennis Fischer
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.chaosfisch.youtubeuploader.services;

import com.google.inject.Inject;
import org.bushe.swing.event.EventBus;
import org.chaosfisch.youtubeuploader.db.PresetEntry;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 10.01.12
 * Time: 22:13
 * To change this template use File | Settings | File Templates.
 */
public class PresetServiceImpl implements PresetService
{
	private final SessionFactory sessionFactory;

	@Inject
	public PresetServiceImpl(final SessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;
	}

	public PresetEntry createPresetEntry(final PresetEntry presetEntry)
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		final Query temp;
		temp = session.createQuery("Select Count(*) From PresetEntry Where name = :name");
		temp.setParameter("name", presetEntry.getName());

		if ((Long) temp.uniqueResult() > 0) {
			session.getTransaction().commit();
			return null;
		}

		session.save(presetEntry);
		session.getTransaction().commit();
		EventBus.publish(PRESET_ENTRY_ADDED, presetEntry);
		return presetEntry;
	}

	public PresetEntry deletePresetEntry(final PresetEntry presetEntry)
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		session.delete(presetEntry);
		session.getTransaction().commit();
		return presetEntry;
	}

	public PresetEntry updatePresetEntry(final PresetEntry presetEntry)
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		session.update(presetEntry);
		session.getTransaction().commit();
		return presetEntry;
	}

	@Override
	public List getAllPresetEntry()
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		final List returnList = session.createQuery("select p from PresetEntry as p order by name").list(); //NON-NLS
		session.getTransaction().commit();
		return returnList;
	}

	public PresetEntry findPresetEntry(final int identifier)
	{
		final Session session = this.sessionFactory.getCurrentSession();
		return (PresetEntry) session.load(PresetEntry.class, identifier);
	}
}
