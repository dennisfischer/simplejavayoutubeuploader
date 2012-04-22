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

package org.chaosfisch.youtubeuploader.plugins.socializeplugin.models;

import org.bushe.swing.event.EventBus;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.models.entities.MessageEntry;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.services.MessageService;
import org.hibernate.SessionFactory;
import org.hibernate.event.spi.*;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 22.04.12
 * Time: 16:06
 * To change this template use File | Settings | File Templates.
 */
public class EventListener implements PostDeleteEventListener, PostInsertEventListener, PostUpdateEventListener
{
	private final SessionFactory sessionFactory;

	public EventListener(final SessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;
	}

	@Override public void onPostDelete(final PostDeleteEvent postDeleteEvent)
	{
		if (postDeleteEvent.getEntity() instanceof MessageEntry) {
			EventBus.publish(MessageService.MESSAGE_ENTRY_REMOVED, postDeleteEvent.getEntity());
		}
	}

	@Override public void onPostInsert(final PostInsertEvent postInsertEvent)
	{
		if (postInsertEvent.getEntity() instanceof MessageEntry) {
			EventBus.publish(MessageService.MESSAGE_ENTRY_ADDED, postInsertEvent.getEntity());
		}
	}

	@Override public void onPostUpdate(final PostUpdateEvent postUpdateEvent)
	{
		if (postUpdateEvent.getEntity() instanceof MessageEntry) {
			EventBus.publish(MessageService.MESSAGE_ENTRY_UPDATED, postUpdateEvent.getEntity());
		}
	}
}
