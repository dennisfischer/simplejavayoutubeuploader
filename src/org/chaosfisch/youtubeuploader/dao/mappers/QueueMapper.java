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

import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.models.Queue;

public interface QueueMapper
{
	List<Queue> getAll();

	List<Queue> getQueued();

	List<Queue> getArchived();

	List<Queue> findByAccount(Account account);

	List<Queue> findByPlaylist(Playlist playlist);

	Queue findQueue(Queue queue);

	Queue poll();

	void createQueue(Queue queue);

	void updateQueue(Queue queue);

	void deleteQueue(Queue queue);

	void moveTop(Queue queue);

	void moveUp(Queue queue);

	void moveDown(Queue queue);

	void moveBottom(Queue queue);

	int countQueued();

	int countStarttime();

	List<Queue> getValidQueued();
}
