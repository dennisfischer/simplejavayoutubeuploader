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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.chaosfisch.util.plugin.Pluggable;
import org.chaosfisch.util.plugin.PluginManager;

import com.google.inject.Inject;

public class PluginManagerImpl implements PluginManager
{
	private Map<String, Pluggable>	plugins;
	@InjectLogger
	private Logger					logger;
	@Inject
	private PluginLoader			pluginLoader;

	@Override
	public List<Pluggable> loadPlugins(final String... disabledPlugins)
	{
		logger.info("Loading Plugins...: Disabled -> %s"); // NON-NLS
		plugins = pluginLoader.loadPlugins(Arrays.asList(disabledPlugins));

		logger.info("Checking dependencies..."); // NON-NLS
		for (Iterator<Map.Entry<String, Pluggable>> i = plugins.entrySet().iterator(); i.hasNext();)
		{
			final Map.Entry<String, Pluggable> entry = i.next();
			logger.info(String.format("Checking dependencies of %s", entry.getValue().getClass().getName()));// NON-NLS
			for (final String dependency : entry.getValue().getDependencies())
			{
				if (!plugins.containsKey(dependency))
				{
					logger.info(String.format("Missing dependency: %s <- %s", dependency, entry.getValue().getClass().getName())); // NON-NLS
					i.remove();
					break;
				}
			}
		}

		return new ArrayList<Pluggable>(plugins.values());
	}

	@Override
	public void startPlugins()
	{
		if (plugins != null)
		{
			for (final Pluggable p : plugins.values())
			{
				p.onStart();
			}
		}
	}

	@Override
	public void endPlugins()
	{
		if (plugins != null)
		{
			for (final Pluggable p : plugins.values())
			{
				p.onEnd();
			}
		}
	}

	@Override
	public Map<String, Pluggable> getPlugins()
	{
		return Collections.unmodifiableMap(plugins);
	}
}