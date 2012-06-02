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
import com.google.inject.Injector;
import com.google.inject.persist.Transactional;
import org.bushe.swing.event.EventBus;
import org.chaosfisch.util.CRUDService;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.mappers.MessageMapper;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.models.Message;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.services.providers.*;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 14.04.12
 * Time: 21:28
 * To change this template use File | Settings | File Templates.
 */
public class MessageService implements CRUDService<Message>
{

	public static final String MESSAGE_ENTRY_ADDED   = "onMessageEntryAdded"; //NON-NLS
	public static final String MESSAGE_ENTRY_REMOVED = "onMessageEntryRemoved"; //NON-NLS
	public static final String MESSAGE_ENTRY_UPDATED = "onMessageEntryUpdated"; //NON-NLS

	@Inject private Injector      injector;
	@Inject private MessageMapper messageMapper;

	public ISocialProvider get(final Provider provider)
	{
		switch (provider) {
			case FACEBOOK:
				return this.injector.getInstance(FacebookSocialProvider.class);
			case TWITTER:
				return this.injector.getInstance(TwitterSocialProvider.class);
			case GOOGLEPLUS:
				return this.injector.getInstance(GooglePlusSocialProvider.class);
			case YOUTUBE:
				return this.injector.getInstance(YoutubeSocialProvider.class);
		}
		return null;
	}

	@Override @Transactional public Message create(final Message message)
	{
		this.messageMapper.createMessage(message);
		EventBus.publish(MessageService.MESSAGE_ENTRY_ADDED, message);
		return message;
	}

	@Override @Transactional public Message update(final Message message)
	{
		this.messageMapper.updateMessage(message);
		EventBus.publish(MessageService.MESSAGE_ENTRY_UPDATED, message);
		return message;
	}

	@Override @Transactional public void delete(final Message message)
	{
		this.messageMapper.deleteMessage(message);
		EventBus.publish(MessageService.MESSAGE_ENTRY_REMOVED, message);
	}

	@Override @Transactional public Iterable<Message> getAll()
	{
		return this.messageMapper.getMessages();
	}

	@Override @Transactional public Iterable<Message> find(final Message message)
	{
		return this.messageMapper.findMessages(message);
	}

	@Transactional public Iterable<Message> findWithoutQueueID()
	{
		return this.messageMapper.findMessagesByQueueID(null);
	}

	@Transactional public void clearByUploadID(final Integer uploadID)
	{
		for (final Message message : this.messageMapper.findMessagesByQueueID(uploadID)) {
			this.delete(message);
		}
	}
}
