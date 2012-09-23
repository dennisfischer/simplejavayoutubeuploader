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

package org.chaosfisch.youtubeuploader.dao.mappers;

import java.util.List;

import org.chaosfisch.youtubeuploader.models.Message;

public interface MessageMapper
{

	List<Message> getMessages();

	List<Message> findMessages(Message message);

	void deleteMessage(Message message);

	void createMessage(Message message);

	void updateMessage(Message message);

	void deleteByUploadID(Integer uploadID);

	List<Message> findMessagesByQueueID(Integer uploadID);
}
