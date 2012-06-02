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

import org.apache.log4j.Logger;
import org.chaosfisch.plugin.ExtensionPoints.ExtensionPoint;
import org.chaosfisch.plugin.Pluggable;
import org.chaosfisch.plugin.PluginService;
import org.chaosfisch.youtubeuploader.util.logger.InjectLogger;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 05.02.12
 * Time: 16:07
 * To change this template use File | Settings | File Templates.
 */
public class PluginServiceImpl implements PluginService
{
	private final Map<String, Vector<ExtensionPoint>> extensionPointMap = new HashMap<String, Vector<ExtensionPoint>>(20);
	@InjectLogger private Logger logger;

	@Override
	public void registerPlugins(final List<Pluggable> pluggableList)
	{
		for (final Pluggable p : pluggableList) {
			p.init();
		}
	}

	@Override
	public void registerExtension(final String type, final ExtensionPoint extension)
	{
		//noinspection StringConcatenation
		this.logger.debug("Extension registered, type: " + type); //NON-NLS
		if (!this.extensionPointMap.containsKey(type)) {
			this.extensionPointMap.put(type, new Vector<ExtensionPoint>(20));
		}
		this.extensionPointMap.get(type).add(extension);
	}

	public Vector<ExtensionPoint> getExtensions(final String extensionType)
	{
		if (this.extensionPointMap.containsKey(extensionType)) {
			return this.extensionPointMap.get(extensionType);
		}
		return new Vector<ExtensionPoint>(0);
	}

	@Override
	public File getPluginDataDirectory()
	{
		return new File("");
	}
}
