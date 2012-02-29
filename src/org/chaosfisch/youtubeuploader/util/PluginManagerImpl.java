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

import com.google.inject.Inject;
import org.apache.log4j.Logger;
import org.chaosfisch.plugin.Pluggable;
import org.chaosfisch.plugin.PluginManager;
import org.chaosfisch.youtubeuploader.util.logger.InjectLogger;

import java.io.File;
import java.io.IOException;
import java.util.List;

@SuppressWarnings("ALL")
public class PluginManagerImpl implements PluginManager
{
	private               List<Pluggable> plugins;
	@InjectLogger private Logger          logger;
	@Inject               PluginLoader    pluginLoader;

	public PluginManagerImpl()
	{
	}

	@Override
	public List<Pluggable> loadPlugins()
	{
		return this.loadPlugins(new File("./plugins"));
	}

	public List<Pluggable> loadPlugins(File directory)
	{
		logger.info("Loading Plugins from " + directory.getAbsolutePath());
		try {
			plugins = pluginLoader.loadPlugins(directory);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return plugins;
	}

	@Override
	public void startPlugins()
	{
		if (plugins != null) {
			for (Pluggable p : plugins) {
				p.onStart();
			}
		}
	}

	@Override
	public void endPlugins()
	{
		if (plugins != null) {
			for (Pluggable p : plugins) {
				p.onEnd();
			}
		}
	}
}