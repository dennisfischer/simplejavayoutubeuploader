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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.impl.AccountServiceImpl;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.impl.PlaylistServiceImpl;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.impl.PresetServiceImpl;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.impl.QueueServiceImpl;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.AccountService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PlaylistService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PresetService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.QueueService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.AutoTitleGeneratorImpl;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.spi.AutoTitleGenerator;
import org.mybatis.guice.XMLMyBatisModule;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 26.02.12
 * Time: 20:17
 * To change this template use File | Settings | File Templates.
 */
public class BindingsModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		this.install(new XMLMyBatisModule()
		{
			@Override protected void initialize()
			{
				this.setClassPathResource("org/mybatis/mappers/mybatis_config.xml");
			}
		});
		this.bind(AccountService.class).to(AccountServiceImpl.class).in(Singleton.class);
		this.bind(QueueService.class).to(QueueServiceImpl.class).in(Singleton.class);
		this.bind(PresetService.class).to(PresetServiceImpl.class).in(Singleton.class);
		this.bind(PlaylistService.class).to(PlaylistServiceImpl.class).in(Singleton.class);
		this.bind(AutoTitleGenerator.class).to(AutoTitleGeneratorImpl.class);
	}
}
