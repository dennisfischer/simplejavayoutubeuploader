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

package org.chaosfisch.youtubeuploader.db;

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 02.04.12
 * Time: 17:29
 * To change this template use File | Settings | File Templates.
 */
public class EventIntegrator implements org.hibernate.integrator.spi.Integrator
{

	@Override public void integrate(final Configuration configuration, final SessionFactoryImplementor sessionFactory, final SessionFactoryServiceRegistry serviceRegistry)
	{
		final EventListenerRegistry eventListenerRegistry = serviceRegistry.getService(EventListenerRegistry.class);

		final EventListener eventListener = new EventListener();
		eventListenerRegistry.prependListeners(EventType.POST_INSERT, eventListener);
		eventListenerRegistry.prependListeners(EventType.POST_UPDATE, eventListener);
		eventListenerRegistry.prependListeners(EventType.POST_DELETE, eventListener);
	}

	@Override public void integrate(final MetadataImplementor metadata, final SessionFactoryImplementor sessionFactory, final SessionFactoryServiceRegistry serviceRegistry)
	{
		final EventListenerRegistry eventListenerRegistry = serviceRegistry.getService(EventListenerRegistry.class);

		final EventListener eventListener = new EventListener();
		eventListenerRegistry.prependListeners(EventType.POST_INSERT, eventListener);
		eventListenerRegistry.prependListeners(EventType.POST_UPDATE, eventListener);
		eventListenerRegistry.prependListeners(EventType.POST_DELETE, eventListener);
	}

	@Override public void disintegrate(final SessionFactoryImplementor sessionFactory, final SessionFactoryServiceRegistry serviceRegistry)
	{
	}
}