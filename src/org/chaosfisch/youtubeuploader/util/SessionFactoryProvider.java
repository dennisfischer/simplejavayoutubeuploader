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

package org.chaosfisch.youtubeuploader.util;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 07.01.12
 * Time: 18:58
 * To change this template use File | Settings | File Templates.
 */

import com.google.inject.Provider;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class SessionFactoryProvider implements Provider<SessionFactory>
{

	private final SessionFactory sessionFactory = this.buildSessionFactory();

	private SessionFactory buildSessionFactory()
	{
		try {
			// Create the SessionFactory from hibernate.cfg.xml
			final Configuration configuration = new Configuration().configure(SessionFactoryProvider.class.getResource("/org/chaosfisch/youtubeuploader/resources/hibernate.cfg.xml"));
			configuration.addURL(SessionFactoryProvider.class.getResource("/org/chaosfisch/youtubeuploader/db/hbm/AccountEntry.hbm.xml"));
			configuration.addURL(SessionFactoryProvider.class.getResource("/org/chaosfisch/youtubeuploader/db/hbm/QueueEntry.hbm.xml"));
			configuration.addURL(SessionFactoryProvider.class.getResource("/org/chaosfisch/youtubeuploader/db/hbm/PresetEntry.hbm.xml"));
			configuration.addURL(SessionFactoryProvider.class.getResource("/org/chaosfisch/youtubeuploader/db/hbm/DirectoryEntry.hbm.xml"));
			configuration.addURL(SessionFactoryProvider.class.getResource("/org/chaosfisch/youtubeuploader/db/hbm/PlaylistEntry.hbm.xml"));
			return configuration.buildSessionFactory();
		} catch (Throwable ex) {
			// Make sure you log the exception, as it might be swallowed
			//noinspection StringConcatenation
			System.err.println("Initial SessionFactory creation failed." + ex); //NON-NLS
			throw new ExceptionInInitializerError(ex);
		}
	}

	public SessionFactory get()
	{
		return this.sessionFactory;
	}
}