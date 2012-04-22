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

package org.chaosfisch.youtubeuploader.plugins.socializeplugin.services;

import com.google.inject.Inject;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.models.entities.MessageEntry;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.services.providers.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 14.04.12
 * Time: 21:28
 * To change this template use File | Settings | File Templates.
 */
public class MessageService
{
	@Inject private SessionFactory sessionFactory;

	public static final String MESSAGE_ENTRY_ADDED   = "onMessageEntryAdded"; //NON-NLS
	public static final String MESSAGE_ENTRY_REMOVED = "onMessageEntryRemoved"; //NON-NLS
	public static final String MESSAGE_ENTRY_UPDATED = "onMessageEntryUpdated"; //NON-NLS

	public enum Provider
	{
		FACEBOOK, TWITTER, GOOGLEPLUS, YOUTUBE
	}

	public ISocialProvider get(final Provider provider)
	{
		switch (provider) {
			case FACEBOOK:
				return new FacebookSocialProvider();
			case TWITTER:
				return new TwitterSocialProvider();
			case GOOGLEPLUS:
				return new GooglePlusSocialProvider();
			case YOUTUBE:
				return new YoutubeSocialProvider();
		}
		return null;
	}

	public void createMessageEntry(final MessageEntry messageEntry)
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		session.save(messageEntry);
		session.getTransaction().commit();
	}

	public List<MessageEntry> getMessageEntriesByQueueID(final int queueID)
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		List<MessageEntry> messageEntries = session.createQuery("select m from MessageEntry as m WHERE m.uploadID = " + queueID).list();
		session.getTransaction().commit();
		return messageEntries;
	}

	public List<MessageEntry> getMessageEntriesWithoutQueueID()
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		List<MessageEntry> messageEntries = session.createQuery("select m from MessageEntry as m WHERE m.uploadID = NULL").list();
		session.getTransaction().commit();
		return messageEntries;
	}

	public List<MessageEntry> getMessageEntries()
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		List<MessageEntry> messageEntries = session.createQuery("SELECT m FROM MessageEntry as m order by identity").list();
		session.getTransaction().commit();
		return messageEntries;
	}

	public void clearWithoutQueueID()
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		session.createQuery("DELETE FROM MessageEntry as m WHERE m.uploadID = NULL").executeUpdate();
		session.getTransaction().commit();
	}

	public void clearWithQueueID(final int identity)
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		session.createQuery("DELETE FROM MessageEntry as m WHERE m.uploadID = " + identity).executeUpdate();
		session.getTransaction().commit();
	}

	public void removeMessageEntry(final MessageEntry messageEntry)
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		session.delete(messageEntry);
		session.getTransaction().commit();
	}
}
