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
import com.google.inject.Injector;
import org.apache.xbean.finder.ResourceFinder;
import org.chaosfisch.plugin.Pluggable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PluginLoader
{
	@Inject Injector injector;

	public List<Pluggable> loadPlugins(final File plugDir) throws IOException
	{
		final ResourceFinder finder = new ResourceFinder("META-INF/services/");
		try {
			final List<Class> classes = finder.findAllImplementations(Pluggable.class);

			final List<Pluggable> pluggableList = new ArrayList<Pluggable>(classes.size());
			for (final Class pluggable : classes) {
				pluggableList.add(this.injector.<Pluggable>getInstance(pluggable));
			}
			return pluggableList;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		return new ArrayList<Pluggable>(0);
	}
}