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

package org.chaosfisch.youtubeuploader.db;

import org.bushe.swing.event.EventBus;
import org.chaosfisch.youtubeuploader.services.AccountService;
import org.chaosfisch.youtubeuploader.services.PlaylistService;
import org.chaosfisch.youtubeuploader.services.PresetService;
import org.chaosfisch.youtubeuploader.services.QueueService;
import org.hibernate.event.spi.*;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 02.04.12
 * Time: 13:15
 * To change this template use File | Settings | File Templates.
 */
public class EventListener implements PostDeleteEventListener, PostInsertEventListener, PostUpdateEventListener
{

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
