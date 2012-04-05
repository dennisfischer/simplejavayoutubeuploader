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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.models;

import org.bushe.swing.event.EventBus;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.entities.AccountEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.entities.PlaylistEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.entities.PresetEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.entities.QueueEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.AccountService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PlaylistService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PresetService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.QueueService;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.event.spi.*;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 02.04.12
 * Time: 13:15
 * To change this template use File | Settings | File Templates.
 */
public class EventListener implements PostDeleteEventListener, PostInsertEventListener, PostUpdateEventListener, PreDeleteEventListener
{
	private final SessionFactory sessionFactory;

	private final Collection<String> processingEntities = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

	public EventListener(final SessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;
	}

	@Override public void onPostDelete(final PostDeleteEvent postDeleteEvent)
	{
		if (postDeleteEvent.getEntity() instanceof PresetEntry) {
			EventBus.publish(PresetService.PRESET_ENTRY_REMOVED, postDeleteEvent.getEntity());
		} else if (postDeleteEvent.getEntity() instanceof AccountEntry) {
			EventBus.publish(AccountService.ACCOUNT_ENTRY_REMOVED, postDeleteEvent.getEntity());
		} else if (postDeleteEvent.getEntity() instanceof PlaylistEntry) {
			EventBus.publish(PlaylistService.PLAYLIST_ENTRY_REMOVED, postDeleteEvent.getEntity());
		} else if (postDeleteEvent.getEntity() instanceof QueueEntry) {
			EventBus.publish(QueueService.QUEUE_ENTRY_REMOVED, postDeleteEvent.getEntity());
		}
	}

	@Override public boolean onPreDelete(final PreDeleteEvent preDeleteEvent) throws HibernateException
	{
		if (preDeleteEvent.getEntity() instanceof PlaylistEntry) {
			final PlaylistEntry playlistEntry = (PlaylistEntry) preDeleteEvent.getEntity();
			if (this.processingEntities.contains(playlistEntry.getClass().getName() + "_" + playlistEntry.getIdentity())) {
				return false;
			}

			this.processingEntities.add(playlistEntry.getClass().getName() + "_" + playlistEntry.getIdentity());
			final Session session = this.sessionFactory.openSession();
			try {
				session.getTransaction().begin();
				session.createQuery("UPDATE QueueEntry SET playlist_id = NULL WHERE playlist_id = " + playlistEntry.getIdentity()).executeUpdate(); //NON-NLS
				session.createQuery("UPDATE PresetEntry SET playlist_id = NULL WHERE playlist_id = " + playlistEntry.getIdentity()).executeUpdate(); //NON-NLS
				session.getTransaction().commit();
			} catch (Exception ex) {
				session.getTransaction().rollback();
			} finally {
				this.processingEntities.remove(playlistEntry.getClass().getName() + "_" + playlistEntry.getIdentity());
			}
		} else if (preDeleteEvent.getEntity() instanceof AccountEntry) {
			final AccountEntry accountEntry = (AccountEntry) preDeleteEvent.getEntity();
			// check if it's already been processed
			if (this.processingEntities.contains(accountEntry.getClass().getName() + "_" + accountEntry.getIdentity())) {
				return false;
			}
			// block it by ID
			this.processingEntities.add(accountEntry.getClass().getName() + "_" + accountEntry.getIdentity());
			final Session session = this.sessionFactory.openSession();
			try {

				session.getTransaction().begin();
				session.createQuery("DELETE FROM PlaylistEntry WHERE account_id = " + accountEntry.getIdentity()).executeUpdate();  //NON-NLS
				session.createQuery("UPDATE PresetEntry SET account_id = NULL, playlist_id = NULL WHERE ACCOUNT_ID = " + accountEntry.getIdentity()).executeUpdate(); //NON-NLS
				session.createQuery("UPDATE QueueEntry SET account_id = NULL, locked = true WHERE account_id = " + accountEntry.getIdentity()).executeUpdate(); //NON-NLS
				session.getTransaction().commit();
			} catch (Exception ex) {
				session.getTransaction().rollback();
			} finally {
				// release
				this.processingEntities.remove(accountEntry.getClass().getName() + "_" + accountEntry.getIdentity());
			}
		}
		return false;
	}

	@Override public void onPostInsert(final PostInsertEvent postInsertEvent)
	{
		if (postInsertEvent.getEntity() instanceof PresetEntry) {
			EventBus.publish(PresetService.PRESET_ENTRY_ADDED, postInsertEvent.getEntity());
		} else if (postInsertEvent.getEntity() instanceof AccountEntry) {
			EventBus.publish(AccountService.ACCOUNT_ENTRY_ADDED, postInsertEvent.getEntity());
		} else if (postInsertEvent.getEntity() instanceof PlaylistEntry) {
			EventBus.publish(PlaylistService.PLAYLIST_ENTRY_ADDED, postInsertEvent.getEntity());
		} else if (postInsertEvent.getEntity() instanceof QueueEntry) {
			EventBus.publish(QueueService.QUEUE_ENTRY_ADDED, postInsertEvent.getEntity());
		}
	}

	@Override public void onPostUpdate(final PostUpdateEvent postUpdateEvent)
	{
		if (postUpdateEvent.getEntity() instanceof PresetEntry) {
			EventBus.publish(PresetService.PRESET_ENTRY_UPDATED, postUpdateEvent.getEntity());
		} else if (postUpdateEvent.getEntity() instanceof AccountEntry) {
			EventBus.publish(AccountService.ACCOUNT_ENTRY_UPDATED, postUpdateEvent.getEntity());
		} else if (postUpdateEvent.getEntity() instanceof PlaylistEntry) {
			EventBus.publish(PlaylistService.PLAYLIST_ENTRY_UPDATED, postUpdateEvent.getEntity());
		} else if (postUpdateEvent.getEntity() instanceof QueueEntry) {
			EventBus.publish(QueueService.QUEUE_ENTRY_UPDATED, postUpdateEvent.getEntity());
		}
	}
}
