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

package org.chaosfisch.youtubeuploader.plugins.directoryplugin.services.impl;

import com.google.inject.Inject;
import org.bushe.swing.event.EventBus;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.models.entities.DirectoryEntry;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.services.spi.DirectoryService;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;

import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 15.03.12
 * Time: 18:03
 * To change this template use File | Settings | File Templates.
 */
public class DirectoryServiceImpl implements DirectoryService
{
	@Inject private SessionFactory sessionFactory;

	@Override public List<DirectoryEntry> getAll()
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		final List<DirectoryEntry> returnList = session.createQuery("select d from DirectoryEntry as d order by directory").list(); //NON-NLS
		session.getTransaction().commit();
		return returnList;
	}

	@Override public List<DirectoryEntry> getAllActive()
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		final Criteria criteria = session.createCriteria(DirectoryEntry.class); //NON-NLS
		criteria.addOrder(Order.asc("directory")); //NON-NLS
		criteria.add(Restrictions.eq("active", true)); //NON-NLS
		criteria.add(Restrictions.ne("locked", true)); //NON-NLS

		final List<DirectoryEntry> returnList = criteria.list();
		session.getTransaction().commit();
		return returnList;
	}

	@Override public DirectoryEntry findByFile(final File file)
	{
		final String directory = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(File.separator));
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		final Criteria criteria = session.createCriteria(DirectoryEntry.class); //NON-NLS
		criteria.addOrder(Order.asc("directory")); //NON-NLS
		criteria.add(Property.forName("directory").eq(directory)); //NON-NLS
		final DirectoryEntry directoryEntry = (DirectoryEntry) criteria.uniqueResult();

		session.getTransaction().commit();
		return directoryEntry;
	}

	@Override public DirectoryEntry createDirectoryEntry(final DirectoryEntry directoryEntry)
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		session.save(directoryEntry);
		session.getTransaction().commit();
		EventBus.publish(DirectoryService.DIRECTORY_ENTRY_ADDED, directoryEntry);
		return directoryEntry;
	}

	@Override public DirectoryEntry deleteDirectoryEntry(final DirectoryEntry directoryEntry)
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		session.delete(directoryEntry);
		session.getTransaction().commit();
		EventBus.publish(DirectoryService.DIRECTORY_ENTRY_REMOVED, directoryEntry);
		return directoryEntry;
	}

	@Override public DirectoryEntry updateDirectoryEntry(final DirectoryEntry directoryEntry)
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		session.update(directoryEntry);
		session.getTransaction().commit();
		EventBus.publish(DirectoryService.DIRECTORY_ENTRY_UPDATED, directoryEntry);
		return directoryEntry;
	}
}
