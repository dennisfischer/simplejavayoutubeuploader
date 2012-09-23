/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Dennis Fischer
 ******************************************************************************/

package org.chaosfisch.youtubeuploader.services.socialize;

import java.util.List;

import org.bushe.swing.event.EventBus;
import org.chaosfisch.youtubeuploader.dao.mappers.MessageMapper;
import org.chaosfisch.youtubeuploader.dao.spi.CRUDDao;
import org.chaosfisch.youtubeuploader.models.Message;
import org.chaosfisch.youtubeuploader.services.socialize.providers.FacebookSocialProvider;
import org.chaosfisch.youtubeuploader.services.socialize.providers.ISocialProvider;
import org.chaosfisch.youtubeuploader.services.socialize.providers.TwitterSocialProvider;
import org.mybatis.guice.transactional.Transactional;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class MessageService implements CRUDDao<Message>
{

	public static final String	MESSAGE_ENTRY_ADDED		= "onMessageEntryAdded";
	public static final String	MESSAGE_ENTRY_REMOVED	= "onMessageEntryRemoved";
	public static final String	MESSAGE_ENTRY_UPDATED	= "onMessageEntryUpdated";

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
