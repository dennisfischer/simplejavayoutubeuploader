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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.services.impl;

import com.google.inject.Inject;
import org.bushe.swing.event.EventBus;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.mappers.QueueMapper;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Queue;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.QueuePosition;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.QueueService;
import org.mybatis.guice.transactional.Transactional;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 03.01.12
 * Time: 18:53
 * To change this template use File | Settings | File Templates.
 */
public class QueueServiceImpl implements QueueService
{
	@Inject private QueueMapper queueMapper;

	@Transactional @Override public Queue create(final Queue queue)
	{
		queue.sequence = this.queueMapper.countQueued();
		this.queueMapper.createQueue(queue);
		EventBus.publish(QueueService.QUEUE_ENTRY_ADDED, queue);
		return queue;
	}

	@Transactional @Override public Queue delete(final Queue queue)
	{
		this.sort(queue, QueuePosition.QUEUE_BOTTOM);
		this.queueMapper.deleteQueue(queue);
		EventBus.publish(QueueService.QUEUE_ENTRY_REMOVED, queue);
		return queue;
	}

	@Transactional @Override public Queue update(final Queue queue)
	{
		this.queueMapper.updateQueue(queue);
		EventBus.publish(QueueService.QUEUE_ENTRY_UPDATED, queue);
		return queue;
	}

	@Transactional @Override public void sort(final Queue queue, final QueuePosition queuePosition)
	{
		final int prePos = queue.sequence;
		final int entries = this.queueMapper.countQueued();
		switch (queuePosition) {
			case QUEUE_BOTTOM:
				this.queueMapper.moveBottom(queue);
				break;
			case QUEUE_TOP:
				this.queueMapper.moveTop(queue);
				break;
			case QUEUE_UP:
				if (prePos == 0) {
					return;
				}
				this.queueMapper.moveUp(queue);
				break;
			case QUEUE_DOWN:
				if (prePos == (entries - 1)) {
					return;
				}
				this.queueMapper.moveDown(queue);
				break;
		}
	}

	@Transactional @Override public List<Queue> getAll()
	{
		return this.queueMapper.getAll();
	}

	@Transactional @Override public List<Queue> getQueued()
	{
		return this.queueMapper.getQueued();
	}

	@Transactional @Override public List<Queue> getArchived()
	{
		return this.queueMapper.getArchived();
	}

	@Transactional @Override public Queue find(final int identifier)
	{
		return this.queueMapper.findQueue(identifier);
	}

	@Transactional @Override public Queue poll()
	{
		return this.queueMapper.poll();
	}

	@Transactional @Override public boolean hasStarttime()
	{
		return this.queueMapper.countStarttime() > 0;
	}
}