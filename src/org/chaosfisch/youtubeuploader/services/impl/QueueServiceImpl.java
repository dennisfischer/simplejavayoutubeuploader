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

package org.chaosfisch.youtubeuploader.services.impl;

import com.google.inject.Inject;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.youtubeuploader.db.QueueEntry;
import org.chaosfisch.youtubeuploader.services.QueuePosition;
import org.chaosfisch.youtubeuploader.services.QueueService;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 03.01.12
 * Time: 18:53
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection", "StringConcatenation"})
public class QueueServiceImpl implements QueueService
{

	private final SessionFactory sessionFactory;

	/**
	 * Creates a new instance of this serviceImpl
	 *
	 * @param sessionFactory requires a Hibernate SessionFactory
	 */
	@Inject
	public QueueServiceImpl(final SessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;
		AnnotationProcessor.process(this);
	}

	/**
	 * Adds / Persists a QueueEntry
	 *
	 * @param queueEntry the QueueEntry that should be added
	 * @return the added QueueEntry
	 */
	@Override
	public QueueEntry createQueueEntry(final QueueEntry queueEntry)
	{
		final Session session = this.sessionFactory.getCurrentSession();
		if (queueEntry.getSequence() == 0) {
			session.getTransaction().begin();
			queueEntry.setSequence(this.countEntries(session));
		} else {
			session.getTransaction().begin();
		}

		session.save(queueEntry);
		session.getTransaction().commit();
		EventBus.publish(QUEUE_ENTRY_ADDED, queueEntry);
		return queueEntry;
	}

	/**
	 * Counts all entries in the database
	 *
	 * @param session a open current HibernateSession
	 * @return int elements in database
	 */
	private int countEntries(final Session session)
	{
		final Object object = session.createQuery("select count(identity) from QueueEntry").uniqueResult();
		return Integer.parseInt(object.toString());
	}

	/**
	 * Assigns a new place / new sequence to the entry
	 *
	 * @param queueEntry    the QueueEntry to reposition
	 * @param queuePosition the Position the QueueEntry should get
	 */
	@Override
	public void sortQueueEntry(final QueueEntry queueEntry, final QueuePosition queuePosition)
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		session.refresh(queueEntry);
		final int prePos = queueEntry.getSequence();
		final int entries = this.countEntries(session);
		switch (queuePosition) {
			case QUEUE_BOTTOM:

				session.createQuery("UPDATE QueueEntry SET SEQUENCE = SEQUENCE-1 WHERE SEQUENCE > " + prePos).executeUpdate();
				queueEntry.setSequence(entries - 1);
				break;
			case QUEUE_TOP:
				session.createQuery("UPDATE QueueEntry SET SEQUENCE = SEQUENCE+1 WHERE SEQUENCE < " + prePos).executeUpdate();
				queueEntry.setSequence(0);
				break;
			case QUEUE_UP:
				if (prePos == 0) {
					break;
				}
				session.createQuery("UPDATE QueueEntry SET SEQUENCE = " + prePos + " + " + (prePos - 1) + " - SEQUENCE WHERE SEQUENCE IN (" + prePos + "," +
						"" + (prePos - 1) + ")").executeUpdate();
				break;
			case QUEUE_DOWN:
				if (prePos == entries - 1) {
					break;
				}
				session.createQuery("UPDATE QueueEntity SET SEQUENCE = " + prePos + " + " + (prePos + 1) + " - SEQUENCE WHERE SEQUENCE IN (" + prePos + "," +
						"" + (prePos + 1) + ")").executeUpdate();
				break;
		}
		session.getTransaction().commit();
	}

	@Override
	public QueueEntry deleteQueueEntry(final QueueEntry queueEntry)
	{
		this.sortQueueEntry(queueEntry, QueuePosition.QUEUE_BOTTOM);
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		session.delete(queueEntry);
		session.getTransaction().commit();
		EventBus.publish(QUEUE_ENTRY_REMOVED, queueEntry);
		return queueEntry;
	}

	/**
	 * Updates / persists the QueueEntry
	 *
	 * @param queueEntry the QueueEntry to update
	 * @return the updated QueueEntry
	 */
	@Override
	public QueueEntry updateQueueEntry(final QueueEntry queueEntry)
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		session.update(queueEntry);
		session.getTransaction().commit();
		return queueEntry;
	}

	/**
	 * Returns all entries in database
	 *
	 * @return List<QueueEntry> all elements
	 */
	@Override
	public List<QueueEntry> getAllQueueEntry()
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		final List<QueueEntry> returnList = session.createQuery("select q from QueueEntry as q order by identity").list();
		session.getTransaction().commit();
		return returnList;
	}

	/**
	 * Returns all queued entries
	 *
	 * @return List<QueueEntry> queued elements
	 */
	@Override
	public List<QueueEntry> getQueuedQueueEntry()
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		final List<QueueEntry> returnList = session.createQuery("select q from QueueEntry as q WHERE q.archived = false ORDER by sequence asc").list();
		session.getTransaction().commit();
		return returnList;
	}

	/**
	 * Returns all archived entries
	 *
	 * @return List<QueueEntry> archived elements
	 */
	@Override
	public List<QueueEntry> getArchivedQueueEntry()
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		final List<QueueEntry> returnList = session.createQuery("select q from QueueEntry as q WHERE q.archived = true order by identity").list();
		session.getTransaction().commit();
		return returnList;
	}

	/**
	 * Tries to find a QueueEntry by id
	 *
	 * @param identifier the unique id of the QueueEntry
	 * @return The found QueueEntry or null
	 */
	@Override
	public QueueEntry findQueueEntry(final int identifier)
	{
		final Session session = this.sessionFactory.getCurrentSession();
		return (QueueEntry) session.load(QueueEntry.class, identifier);
	}

	/**
	 * Polls item and updates inProgress to true
	 *
	 * @return Polls current first positioned item or null if not found
	 */
	@Override
	public QueueEntry poll()
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		final QueueEntry queueEntry = (QueueEntry) session.createCriteria(QueueEntry.class).setMaxResults(1).addOrder(Order.desc("started")).addOrder(Order.asc("sequence")).addOrder(Order.asc("failed")
		).add(Restrictions.eq("archived", false)).add(Restrictions.eq("inprogress", false)).add(Restrictions.or(Restrictions.lt("started", new Date()),
				Restrictions.isNull("started"))).setMaxResults(1).uniqueResult();
		if (queueEntry != null) {
			queueEntry.setInprogress(true);
		}
		session.getTransaction().commit();
		return queueEntry;
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = "updateQueueEntry")
	public void onUploadProgress(final String topic, final Object o)
	{
		final QueueEntry queueEntry = (QueueEntry) o;

		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		session.update(queueEntry);
		session.getTransaction().commit();
	}

	@Override
	public boolean hasStarttimeEntry()
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		final QueueEntry queueEntry = (QueueEntry) session.createCriteria(QueueEntry.class).setMaxResults(1).addOrder(Order.asc("started")).addOrder(Order.asc("sequence")).addOrder(Order.asc("failed"))
				.add(Restrictions.eq("archived", false)).add(Restrictions.eq("inprogress", false)).add(Restrictions.lt("started", new Date())).uniqueResult();
		session.getTransaction().commit();
		return queueEntry != null;
	}
}