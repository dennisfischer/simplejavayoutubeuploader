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
/*
package org.chaosfisch.youtubeuploader.plugins.coreplugin.models;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 02.04.12
 * Time: 13:15
 * To change this template use File | Settings | File Templates.
 */
/*
public class EventListener
{

	@Override public void onPostDelete(final PostDeleteEvent postDeleteEvent)
	{
		if (postDeleteEvent.getEntity() instanceof Playlist) {
			EventBus.publish(PlaylistService.PLAYLIST_ENTRY_REMOVED, postDeleteEvent.getEntity());
		} else if (postDeleteEvent.getEntity() instanceof Queue) {
			EventBus.publish(QueueService.QUEUE_ENTRY_REMOVED, postDeleteEvent.getEntity());
		}
	}

	@Override public boolean onPreDelete(final PreDeleteEvent preDeleteEvent)
	{
		if (preDeleteEvent.getEntity() instanceof Playlist) {
			final Playlist playlistEntry = (Playlist) preDeleteEvent.getEntity();
			if (this.processingEntities.contains(playlistEntry.getClass().getName() + "_" + playlistEntry.getIdentity())) {
				return false;
			}

			this.processingEntities.add(playlistEntry.getClass().getName() + "_" + playlistEntry.getIdentity());
			final Session session = this.sessionFactory.openSession();
			try {
				session.getTransaction().begin();
				session.createQuery("UPDATE Queue SET playlist_id = NULL WHERE playlist_id = " + playlistEntry.getIdentity()).executeUpdate(); //NON-NLS
				session.createQuery("UPDATE Preset SET playlist_id = NULL WHERE playlist_id = " + playlistEntry.getIdentity()).executeUpdate(); //NON-NLS
				session.getTransaction().commit();
			} catch (Exception ex) {
				session.getTransaction().rollback();
			} finally {
				this.processingEntities.remove(playlistEntry.getClass().getName() + "_" + playlistEntry.getIdentity());
			}
		}
		return false;
	}

	@Override public void onPostInsert(final PostInsertEvent postInsertEvent)
	{
		if (postInsertEvent.getEntity() instanceof Playlist) {
			EventBus.publish(PlaylistService.PLAYLIST_ENTRY_ADDED, postInsertEvent.getEntity());
		} else if (postInsertEvent.getEntity() instanceof Queue) {
			EventBus.publish(QueueService.QUEUE_ENTRY_ADDED, postInsertEvent.getEntity());
		}
	}

	@Override public void onPostUpdate(final PostUpdateEvent postUpdateEvent)
	{
		if (postUpdateEvent.getEntity() instanceof Playlist) {
			EventBus.publish(PlaylistService.PLAYLIST_ENTRY_UPDATED, postUpdateEvent.getEntity());
		} else if (postUpdateEvent.getEntity() instanceof Queue) {
			EventBus.publish(QueueService.QUEUE_ENTRY_UPDATED, postUpdateEvent.getEntity());
		}
	}


}
   */