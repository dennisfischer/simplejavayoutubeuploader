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

/*
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 06.03.12
 * Time: 09:49
 */
package org.chaosfisch.youtubeuploader.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.chaosfisch.youtubeuploader.services.settingsservice.impl.PropertyFileSettingsPersisterImpl;
import org.chaosfisch.youtubeuploader.services.settingsservice.impl.SettingsServiceImpl;
import org.chaosfisch.youtubeuploader.services.settingsservice.spi.SettingsPersister;
import org.chaosfisch.youtubeuploader.services.settingsservice.spi.SettingsService;

public class SettingsModule extends AbstractModule
{
	protected void configure()
	{
		this.bind(SettingsService.class).to(SettingsServiceImpl.class).in(Singleton.class);
		this.bind(SettingsPersister.class).to(PropertyFileSettingsPersisterImpl.class).in(Singleton.class);
	}
}
