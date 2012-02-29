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
import org.chaosfisch.plugin.Pluggable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

@SuppressWarnings("ALL")
public class PluginLoader
{
	@Inject Injector injector;

	public List<Pluggable> loadPlugins(File plugDir) throws IOException
	{
		ServiceLoader<Pluggable> pluggableServiceLoader = ServiceLoader.load(Pluggable.class);
		List<Pluggable> pluggableList = new ArrayList<Pluggable>();
		for (Pluggable pluggable : pluggableServiceLoader) {
			pluggableList.add(injector.getInstance(pluggable.getClass()));
			pluggable = null;
		}
		return pluggableList;
	}
}