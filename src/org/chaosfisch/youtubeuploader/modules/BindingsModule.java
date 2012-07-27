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
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import org.chaosfisch.plugin.PluginManager;
import org.chaosfisch.plugin.PluginService;
import org.chaosfisch.youtubeuploader.controller.PluginController;
import org.chaosfisch.youtubeuploader.designmanager.DesignManager;
import org.chaosfisch.youtubeuploader.services.settingsservice.impl.PropertyFileSettingsPersisterImpl;
import org.chaosfisch.youtubeuploader.services.settingsservice.impl.SettingsServiceImpl;
import org.chaosfisch.youtubeuploader.services.settingsservice.spi.SettingsPersister;
import org.chaosfisch.youtubeuploader.services.settingsservice.spi.SettingsService;
import org.chaosfisch.youtubeuploader.util.PluginManagerImpl;
import org.chaosfisch.youtubeuploader.util.PluginServiceImpl;
import org.chaosfisch.youtubeuploader.util.logger.Log4JTypeListener;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 26.02.12
 * Time: 17:33
 * To change this template use File | Settings | File Templates.
 */
public class BindingsModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(PluginManager.class).to(PluginManagerImpl.class).in(Singleton.class);
		bind(PluginService.class).toInstance(new PluginServiceImpl());
		bind(JFrame.class).annotatedWith(Names.named("mainFrame")).to(JFrame.class).in(Singleton.class); //NON-NLS
		bind(DesignManager.class).in(Singleton.class);
		bind(PluginController.class).in(Singleton.class);
		bindListener(Matchers.any(), new Log4JTypeListener());
		bind(JFileChooser.class).in(Singleton.class);
		bind(SettingsService.class).to(SettingsServiceImpl.class).in(Singleton.class);
		bind(SettingsPersister.class).to(PropertyFileSettingsPersisterImpl.class).in(Singleton.class);
	}
}
