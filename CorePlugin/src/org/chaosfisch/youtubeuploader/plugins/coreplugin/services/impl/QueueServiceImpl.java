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
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Queue;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.QueuePosition;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.QueueService;
import org.mybatis.guice.transactional.Transactional;
import org.mybatis.mappers.QueueMapper;

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

	@Transactional @Override public Queue createQueue(final Queue queue)
	{
		queue.sequence = this.queueMapper.countQueued();
		this.queueMapper.createQueue(queue);
		EventBus.publish(QUEUE_ENTRY_ADDED, queue);
		return queue;
	}

	@Transactional @Override public Queue deleteQueue(final Queue queue)
	{
		this.queueMapper.deleteQueue(queue);
		EventBus.publish(QUEUE_ENTRY_REMOVED, queue);
		return queue;
	}

	@Transactional @Override public Queue updateQueue(final Queue queue)
	{
		this.queueMapper.updateQueue(queue);
		EventBus.publish(QUEUE_ENTRY_UPDATED, queue);
		return queue;
	}

	@Transactional @Override public void sortList(final Queue queue, final QueuePosition queuePosition)
	{
		//To change body of implemented methods use File | Settings | File Templates.
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

	@Transactional @Override public Queue findQueue(final int identifier)
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

//

//	@Override
//	public void sortList(final Queue queueEntry, final QueuePosition queuePosition)
//	{
//		final Session session = this.sessionFactory.openSession();
//		session.getTransaction().begin();
//		session.refresh(queueEntry);
//		final int prePos = queueEntry.getSequence();
//		final int entries = this.countEntries(session);
//		switch (queuePosition) {
//			case QUEUE_BOTTOM:
//
//				session.createQuery("UPDATE Queue SET SEQUENCE = SEQUENCE-1 WHERE SEQUENCE > " + prePos).executeUpdate();
//				queueEntry.setSequence(entries - 1);
//				break;
//			case QUEUE_TOP:
//				session.createQuery("UPDATE Queue SET SEQUENCE = SEQUENCE+1 WHERE SEQUENCE < " + prePos).executeUpdate();
//				queueEntry.setSequence(0);
//				break;
//			case QUEUE_UP:
//				if (prePos == 0) {
//					break;
//				}
//				session.createQuery("UPDATE Queue SET SEQUENCE = " + prePos + " + " + (prePos - 1) + " - SEQUENCE WHERE SEQUENCE IN (" + prePos + "," +
//						                    "" + (prePos - 1) + ")").executeUpdate();
//				break;
//			case QUEUE_DOWN:
//				if (prePos == entries - 1) {
//					break;
//				}
//				session.createQuery("UPDATE QueueEntity SET SEQUENCE = " + prePos + " + " + (prePos + 1) + " - SEQUENCE WHERE SEQUENCE IN (" + prePos + "," +
//						                    "" + (prePos + 1) + ")").executeUpdate();
//				break;
//		}
//		session.getTransaction().commit();
//		session.close();
//	}
//
//	@Override
//	public Queue deleteQueue(final Queue queueEntry)
//	{
//		this.sortList(queueEntry, QueuePosition.QUEUE_BOTTOM);
//	}
//
//	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = "updateQueue")
//	public void onUploadProgress(final String topic, final Queue queueEntry)
//	{
//		final Session session = this.sessionFactory.openSession();
//		session.getTransaction().begin();
//		session.update(queueEntry);
//		session.getTransaction().commit();
//		session.close();
//	}
}