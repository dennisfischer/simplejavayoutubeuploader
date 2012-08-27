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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi;

import org.chaosfisch.util.CRUDService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Queue;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 05.01.12
 * Time: 10:14
 * To change this template use File | Settings | File Templates.
 */
public interface QueueService extends CRUDService<Queue>
{
	String QUEUE_PRE_ADDED   = "queuePreAdded"; //NON-NLS
	String QUEUE_ADDED       = "queueAdded"; //NON-NLS
	String QUEUE_PRE_REMOVED = "queuePreRemoved"; //NON-NLS
	String QUEUE_REMOVED     = "queueRemoved"; //NON-NLS
	String QUEUE_PRE_UPDATED = "queuePreUpdated"; //NON-NLS
	String QUEUE_UPDATED     = "queueUpdated"; //NON-NLS

	/**
	 * Assigns a new place / new sequence to the entry
	 *
	 * @param queue         the Queue(Entry) to reposition
	 * @param queuePosition the Position the Queue(Entry) should get
	 */
	void sort(Queue queue, QueuePosition queuePosition);

	List<Queue> getAll();

	List<Queue> getQueued();

	List<Queue> getArchived();

	/**
	 * Polls item and updates inProgress to true
	 *
	 * @return Polls current first positioned item or null if not found
	 */
	Queue poll();

	boolean hasStarttime();

	List<Queue> getValidQueued();
}
