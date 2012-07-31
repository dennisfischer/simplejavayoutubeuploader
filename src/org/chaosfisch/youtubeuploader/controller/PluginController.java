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
import org.chaosfisch.table.RowTableModel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

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

	public RowTableModel<Pluggable> getPluginTableModel(final List<Pluggable> pluggableList)
	{
		if (pluginTableModel == null) {
			final List<String> columns = Arrays.asList(resourceBundle.getString("pluginTable.name"), resourceBundle.getString("pluginTable.author"), resourceBundle.getString("pluginTable.disableCheckbox"));
			pluginTableModel = new RowTableModel<Pluggable>(Collections.unmodifiableList(pluggableList), columns, Pluggable.class)
			{
				private static final long serialVersionUID = 3423121373666600098L;

				@Override public Object getValueAt(final int rowIndex, final int columnIndex)
				{
					switch (columnIndex) {
						case 0:
							return getRow(rowIndex).getName();
						case 1:
							return getRow(rowIndex).getAuthor();
						case 2:
						default:
							return true;
					}
				}
			};
			pluginTableModel.setColumnClass(2, Boolean.class);
			pluginTableModel.setModelEditable(false);
		}
		return pluginTableModel;
	}

	public RowTableModel<Pluggable> getPluginTableModel()
	{
		return getPluginTableModel(Collections.<Pluggable>emptyList());
	}
}
