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
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.models.entities.Message;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.services.providers.*;

import java.util.ArrayList;
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

	public static final String MESSAGE_ENTRY_ADDED   = "onMessageEntryAdded"; //NON-NLS
	public static final String MESSAGE_ENTRY_REMOVED = "onMessageEntryRemoved"; //NON-NLS
	public static final String MESSAGE_ENTRY_UPDATED = "onMessageEntryUpdated"; //NON-NLS

	@Inject private Injector injector;

	public enum Provider
	{
		FACEBOOK, TWITTER, GOOGLEPLUS, YOUTUBE
	}

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

	public void createMessageEntry(final Message message)
	{
	}

	public List<Message> getMessageEntriesByQueueID(final int queueID)
	{
		final List<Message> messages = new ArrayList<Message>(0);
		String query = "select m from Message as m WHERE m.uploadID = " + queueID;
		return messages;
	}

	public List<Message> getMessageEntriesWithoutQueueID()
	{
		final List<Message> messages = new ArrayList<Message>(0);
		String query = "select m from Message as m WHERE m.uploadID = NULL";
		return messages;
	}

	public List<Message> getMessageEntries()
	{
		final List<Message> messages = new ArrayList<Message>(0);
		String query = "SELECT m FROM Message as m order by identity";
		return messages;
	}

	public void clearWithoutQueueID()
	{
		String query = "DELETE FROM Message as m WHERE m.uploadID = NULL";
	}

	public void clearWithQueueID(final int identity)
	{
		String query = "DELETE FROM Message as m WHERE m.uploadID = " + identity;
	}

	public void removeMessageEntry(final Message message)
	{
	}
}
