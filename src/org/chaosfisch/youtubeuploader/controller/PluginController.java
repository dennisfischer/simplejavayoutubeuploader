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

import com.google.common.collect.Lists;
import org.chaosfisch.plugin.Pluggable;
import org.chaosfisch.table.RowTableModel;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 13.03.12
 * Time: 08:23
 * To change this template use File | Settings | File Templates.
 */
public class PluginController
{
	private RowTableModel<Pluggable> pluginTableModel;
	private final ResourceBundle resourceBundle = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.resources.application"); //NON-NLS

	public PluginController()
	{

	}

	public RowTableModel<Pluggable> getPluginTableModel(final Collection<Pluggable> pluggableList)
	{
		if (this.pluginTableModel == null) {
			final List<String> columns = Arrays.asList(this.resourceBundle.getString("pluginTable.name"), this.resourceBundle.getString("pluginTable.author"), this.resourceBundle.getString(
					"pluginTable.disableCheckbox"));
			this.pluginTableModel = new RowTableModel<Pluggable>(Lists.newArrayList(pluggableList), columns, Pluggable.class)
			{
				@Override public Object getValueAt(final int rowIndex, final int columnIndex)
				{
					switch (columnIndex) {
						case 0:
							return this.getRow(rowIndex).getName();
						case 1:
							return this.getRow(rowIndex).getAuthor();
						case 2:
						default:
							return true;
					}
				}
			};
			this.pluginTableModel.setColumnClass(2, Boolean.class);
			this.pluginTableModel.setModelEditable(false);
		}
		return this.pluginTableModel;
	}

	public RowTableModel<Pluggable> getPluginTableModel()
	{
		return this.getPluginTableModel(Collections.<Pluggable>emptyList());
	}
}
