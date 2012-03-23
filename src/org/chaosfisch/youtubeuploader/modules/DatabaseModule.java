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

package org.chaosfisch.youtubeuploader.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.chaosfisch.youtubeuploader.services.AccountService;
import org.chaosfisch.youtubeuploader.services.PlaylistService;
import org.chaosfisch.youtubeuploader.services.PresetService;
import org.chaosfisch.youtubeuploader.services.QueueService;
import org.chaosfisch.youtubeuploader.services.impl.AccountServiceImpl;
import org.chaosfisch.youtubeuploader.services.impl.PlaylistServiceImpl;
import org.chaosfisch.youtubeuploader.services.impl.PresetServiceImpl;
import org.chaosfisch.youtubeuploader.services.impl.QueueServiceImpl;
import org.chaosfisch.youtubeuploader.util.SessionFactoryProvider;
import org.hibernate.SessionFactory;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 26.02.12
 * Time: 20:17
 * To change this template use File | Settings | File Templates.
 */
public class DatabaseModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		this.bind(SessionFactory.class).toProvider(SessionFactoryProvider.class).in(Singleton.class);
		this.bind(AccountService.class).to(AccountServiceImpl.class).in(Singleton.class);
		this.bind(QueueService.class).to(QueueServiceImpl.class).in(Singleton.class);
		this.bind(PresetService.class).to(PresetServiceImpl.class).in(Singleton.class);
		this.bind(PlaylistService.class).to(PlaylistServiceImpl.class).in(Singleton.class);
	}
}
