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

package org.chaosfisch.youtubeuploader.services.socialize;

import java.util.List;

import org.bushe.swing.event.EventBus;
import org.chaosfisch.util.CRUDService;
import org.chaosfisch.youtubeuploader.mappers.MessageMapper;
import org.chaosfisch.youtubeuploader.models.Message;
import org.chaosfisch.youtubeuploader.services.socialize.providers.FacebookSocialProvider;
import org.chaosfisch.youtubeuploader.services.socialize.providers.GooglePlusSocialProvider;
import org.chaosfisch.youtubeuploader.services.socialize.providers.ISocialProvider;
import org.chaosfisch.youtubeuploader.services.socialize.providers.TwitterSocialProvider;
import org.chaosfisch.youtubeuploader.services.socialize.providers.YoutubeSocialProvider;
import org.mybatis.guice.transactional.Transactional;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Created with IntelliJ IDEA. User: Dennis Date: 14.04.12 Time: 21:28 To change
 * this template use File | Settings | File Templates.
 */
public class MessageService implements CRUDService<Message>
{

	public static final String	MESSAGE_ENTRY_ADDED		= "onMessageEntryAdded";	// NON-NLS
	public static final String	MESSAGE_ENTRY_REMOVED	= "onMessageEntryRemoved";	// NON-NLS
	public static final String	MESSAGE_ENTRY_UPDATED	= "onMessageEntryUpdated";	// NON-NLS

	@Inject
	private Injector			injector;
	@Inject
	private MessageMapper		messageMapper;

	public ISocialProvider get(final Provider provider)
	{
		switch (provider)
		{
			case FACEBOOK:
				return injector.getInstance(FacebookSocialProvider.class);
			case TWITTER:
				return injector.getInstance(TwitterSocialProvider.class);
			case GOOGLEPLUS:
				return injector.getInstance(GooglePlusSocialProvider.class);
			case YOUTUBE:
				return injector.getInstance(YoutubeSocialProvider.class);
		}
		return null;
	}

	@Override
	@Transactional
	public Message create(final Message message)
	{
		messageMapper.createMessage(message);
		EventBus.publish(MessageService.MESSAGE_ENTRY_ADDED, message);
		return message;
	}

	@Override
	@Transactional
	public Message update(final Message message)
	{
		messageMapper.updateMessage(message);
		EventBus.publish(MessageService.MESSAGE_ENTRY_UPDATED, message);
		return message;
	}

	@Override
	@Transactional
	public void delete(final Message message)
	{
		messageMapper.deleteMessage(message);
		EventBus.publish(MessageService.MESSAGE_ENTRY_REMOVED, message);
	}

	@Override
	@Transactional
	public List<Message> getAll()
	{
		return messageMapper.getMessages();
	}

	@Transactional
	public List<Message> find(final Message message, final boolean multiple)
	{
		return messageMapper.findMessages(message);
	}

	@Override
	@Transactional
	public Message find(final Message message)
	{
		return null;
	}

	@Transactional
	public Iterable<Message> findWithoutQueueID()
	{
		return messageMapper.findMessagesByQueueID(null);
	}

	@Transactional
	public void clearByUploadID(final Integer uploadID)
	{
		for (final Message message : messageMapper.findMessagesByQueueID(uploadID))
		{
			delete(message);
		}
	}
}
