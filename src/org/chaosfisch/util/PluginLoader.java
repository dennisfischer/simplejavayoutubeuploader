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

package org.chaosfisch.util;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;
import org.apache.xbean.finder.ResourceFinder;
import org.chaosfisch.util.plugin.Pluggable;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class PluginLoader
{
	@Inject
	Injector	injector;
	@InjectLogger
	Logger		logger;

	public Map<String, Pluggable> loadPlugins(final Collection<String> disabledPlugins)
	{
		final ResourceFinder finder = new ResourceFinder("META-INF/services/");// NON-NLS
		try
		{
			final List<Class<? extends Pluggable>> classes = finder.findAllImplementations(Pluggable.class);

			final Map<String, Pluggable> pluggableList = new HashMap<String, Pluggable>(classes.size());

			for (final Class<? extends Pluggable> pluggable : classes)
			{
				if (!disabledPlugins.contains(pluggable.getName()))
				{
					logger.info(String.format("Plugin Loaded: %s", pluggable.getName()));// NON-NLS
					pluggableList.put(pluggable.getName(), injector.getInstance(pluggable));
				}
			}
			return pluggableList;
		} catch (ClassNotFoundException e)
		{
			logger.warn(String.format("Plugin could not be loaded: %s", e.getMessage()));// NON-NLS
		} catch (IOException ignored)
		{
			logger.error("Pluginloader fatal error: 1x00001");// NON-NLS
		}
		return new WeakHashMap<String, Pluggable>(0);
	}
}