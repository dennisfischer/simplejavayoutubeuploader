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

package org.chaosfisch.youtubeuploader.dao.impl;

import java.util.List;

import org.bushe.swing.event.EventBus;
import org.chaosfisch.youtubeuploader.dao.mappers.QueueMapper;
import org.chaosfisch.youtubeuploader.dao.spi.QueueDao;
import org.chaosfisch.youtubeuploader.models.Queue;
import org.chaosfisch.youtubeuploader.models.QueuePosition;
import org.mybatis.guice.transactional.Transactional;

import com.google.inject.Inject;

public class QueueDaoImpl implements QueueDao
{
	@Inject
	private QueueMapper	queueMapper;

	@Transactional
	@Override
	public Queue create(final Queue queue)
	{
		queue.sequence = queueMapper.countQueued();
		EventBus.publish(QueueDao.QUEUE_PRE_ADDED, queue);
		queueMapper.createQueue(queue);
		EventBus.publish(QueueDao.QUEUE_POST_ADDED, queue);
		return queue;
	}

	@Transactional
	@Override
	public void delete(final Queue queue)
	{
		sort(queue, QueuePosition.QUEUE_BOTTOM);
		EventBus.publish(QueueDao.QUEUE_PRE_REMOVED, queue);
		queueMapper.deleteQueue(queue);
		EventBus.publish(QueueDao.QUEUE_POST_REMOVED, queue);
	}

	@Transactional
	@Override
	public Queue update(final Queue queue)
	{
		EventBus.publish(QueueDao.QUEUE_PRE_UPDATED, queue);
		queueMapper.updateQueue(queue);
		EventBus.publish(QueueDao.QUEUE_POST_UPDATED, queue);
		return queue;
	}

	@Transactional
	@Override
	public void sort(final Queue queue, final QueuePosition queuePosition)
	{
		final int prePos = queue.sequence;
		final int entries = queueMapper.countQueued();
		switch (queuePosition)
		{
			case QUEUE_BOTTOM:
				queueMapper.moveBottom(queue);
				break;
			case QUEUE_TOP:
				queueMapper.moveTop(queue);
				break;
			case QUEUE_UP:
				if (prePos == 0) { return; }
				queueMapper.moveUp(queue);
				break;
			case QUEUE_DOWN:
				if (prePos == (entries - 1)) { return; }
				queueMapper.moveDown(queue);
				break;
		}
	}

	@Transactional
	@Override
	public List<Queue> getAll()
	{
		return queueMapper.getAll();
	}

	@Transactional
	@Override
	public List<Queue> getQueued()
	{
		return queueMapper.getQueued();
	}

	@Transactional
	@Override
	public List<Queue> getArchived()
	{
		return queueMapper.getArchived();
	}

	@Transactional
	@Override
	public Queue find(final Queue queue)
	{
		return queueMapper.findQueue(queue);
	}

	@Transactional
	@Override
	public Queue poll()
	{
		final Queue queue = queueMapper.poll();
		if (queue == null) { return null; }
		queue.inprogress = true;
		return update(queue);
	}

	@Transactional
	@Override
	public boolean hasStarttime()
	{
		return queueMapper.countStarttime() > 0;
	}

	@Transactional
	@Override
	public List<Queue> getValidQueued()
	{
		return queueMapper.getValidQueued();
	}
}
