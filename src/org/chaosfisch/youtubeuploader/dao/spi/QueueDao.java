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

package org.chaosfisch.youtubeuploader.dao.spi;

import java.util.List;

import org.chaosfisch.youtubeuploader.models.Queue;
import org.chaosfisch.youtubeuploader.models.QueuePosition;

public interface QueueDao extends CRUDDao<Queue>
{
	/**
	 * Event: Before Queue-object is added
	 */
	String	QUEUE_PRE_ADDED		= "queuePreAdded";

	/**
	 * Event: Before Queue-object is removed
	 */
	String	QUEUE_PRE_REMOVED	= "queuePreRemoved";

	/**
	 * Event: Before Queue-object is updated
	 */
	String	QUEUE_PRE_UPDATED	= "queuePreUpdated";

	/**
	 * Event: After Queue-object was added
	 */
	String	QUEUE_POST_ADDED	= "queuePostAdded";

	/**
	 * Event: After Queue-object was removed
	 */
	String	QUEUE_POST_REMOVED	= "queuePostRemoved";

	/**
	 * Event: After Queue-object was updated
	 */
	String	QUEUE_POST_UPDATED	= "queuePostUpdated";

	/**
	 * Assigns a new place / new sequence to the entry
	 * 
	 * @param queue
	 *            the Queue(Entry) to reposition
	 * @param queuePosition
	 *            the Position the Queue(Entry) should get
	 */
	void sort(Queue queue, QueuePosition queuePosition);

	/**
	 * Retrieves all queued items of the persistence storage
	 * 
	 * @return List<Queue> queued items
	 */
	List<Queue> getQueued();

	/**
	 * Retrieves all archived items of the persistence storage
	 * 
	 * @return List<Queue> archived items
	 */
	List<Queue> getArchived();

	/**
	 * Polls item and updates inProgress to true
	 * 
	 * @return Queue polled item
	 */
	Queue poll();

	/**
	 * Checks if elements with start-time / elements that have to be started are
	 * existing
	 * 
	 * @return boolean true | false
	 */
	boolean hasStarttime();

	/**
	 * Retrieves all valid queued items. This checks for existing Account.
	 * 
	 * @return List<Queue> valid queued items
	 */
	List<Queue> getValidQueued();
}
