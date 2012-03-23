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

package org.chaosfisch.youtubeuploader.controller;

import org.chaosfisch.plugin.Pluggable;
import org.chaosfisch.youtubeuploader.models.PluginTableModel;

import javax.swing.table.TableModel;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 13.03.12
 * Time: 08:23
 * To change this template use File | Settings | File Templates.
 */
public class PluginController
{
	private final PluginTableModel pluginTableModel = new PluginTableModel();

	public TableModel getPluginTableModel(final List<Pluggable> pluggableList)
	{
		this.pluginTableModel.addPluginList(pluggableList);
		return this.pluginTableModel;
	}

	public TableModel getPluginTableModel()
	{
		return this.pluginTableModel;
	}
}
