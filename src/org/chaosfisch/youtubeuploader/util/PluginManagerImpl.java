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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class PluginManagerImpl implements PluginManager
{
	private               Map<String, Pluggable> plugins;
	@InjectLogger private Logger                 logger;
	@Inject private       PluginLoader           pluginLoader;

	@Override
	public Collection<Pluggable> loadPlugins(final String... disabledPlugins)
	{
		this.logger.info("Loading Plugins...: Disabled -> %s"); //NON-NLS
		this.plugins = this.pluginLoader.loadPlugins(Arrays.asList(disabledPlugins));

		this.logger.info("Checking dependencies..."); //NON-NLS
		for (Iterator<Map.Entry<String, Pluggable>> i = this.plugins.entrySet().iterator(); i.hasNext(); ) {
			final Map.Entry<String, Pluggable> entry = i.next();
			this.logger.info(String.format("Checking dependencies of %s", entry.getValue().getClass().getName()));//NON-NLS
			for (final String dependency : entry.getValue().getDependencies()) {
				if (!this.plugins.containsKey(dependency)) {
					this.logger.info(String.format("Missing dependency: %s <- %s", dependency, entry.getValue().getClass().getName())); //NON-NLS
					i.remove();
					break;
				}
			}
		}

		return this.plugins.values();
	}

	@Override
	public void startPlugins()
	{
		if (this.plugins != null) {
			for (final Pluggable p : this.plugins.values()) {
				p.onStart();
			}
		}
	}

	@Override
	public void endPlugins()
	{
		if (this.plugins != null) {
			for (final Pluggable p : this.plugins.values()) {
				p.onEnd();
			}
		}
	}
}