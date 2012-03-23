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

package org.chaosfisch.youtubeuploader.models;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.chaosfisch.plugin.Pluggable;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 13.03.12
 * Time: 08:26
 * To change this template use File | Settings | File Templates.
 */
public class PluginTableModel extends AbstractTableModel
{
	private final ArrayList<Pluggable> pluginList     = new ArrayList<Pluggable>(20);
	private final ResourceBundle       resourceBundle = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.resources.application"); //NON-NLS
	private final String[]             columns        = {this.resourceBundle.getString("pluginTable.name"), this.resourceBundle.getString("pluginTable.author"),
			this.resourceBundle.getString("pluginTable.disableCheckbox")};

	public PluginTableModel()
	{
		AnnotationProcessor.process(this);
	}

	public PluginTableModel(final List<Pluggable> l)
	{
		AnnotationProcessor.process(this);
		this.pluginList.addAll(l);
	}

	void addPlugin(final Pluggable pluggable)
	{
		this.pluginList.add(pluggable);
		this.fireTableDataChanged();
	}

	public void addPluginList(final List l)
	{
		for (final Object o : l) {
			if (o instanceof Pluggable) {
				this.addPlugin((Pluggable) o);
			}
		}
	}

	public Pluggable getPluginAt(final int row)
	{
		return this.pluginList.get(row);
	}

	public Pluggable removePluginAt(final int row)
	{
		final Pluggable element = this.pluginList.remove(row);
		this.fireTableDataChanged();
		return element;
	}

	@Override
	public int getRowCount()
	{
		return this.pluginList.size();
	}

	@Override
	public int getColumnCount()
	{
		return this.columns.length;
	}

	@Override
	public String getColumnName(final int col)
	{
		return this.columns[col];
	}

	@Override
	public Object getValueAt(final int row, final int col)
	{
		final Pluggable plugin = this.pluginList.get(row);
		switch (col) {
			case 0:
				return plugin.getName();
			case 1:
				return plugin.getAuthor();
			case 2:
				return true;
			default:
				return null;
		}
	}

	@Override
	public Class getColumnClass(final int col)
	{
		switch (col) {
			case 0:
				return String.class;
			case 1:
				return String.class;
			case 2:
				return Boolean.class;
			default:
				return null;
		}
	}

	@Override
	public void setValueAt(final Object value, final int row, final int col)
	{
		final Pluggable plugin = this.pluginList.get(row);
		switch (col) {
		}
		this.fireTableCellUpdated(row, col);
	}

	public boolean hasPluginAt(final int selectedRow)
	{
		return this.pluginList.size() >= selectedRow && selectedRow != -1;
	}

	public List<Pluggable> getPluginList()
	{
		return this.pluginList;
	}

	public void removeAll()
	{
		final Iterator iterator = this.pluginList.iterator();
		while (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
			this.fireTableDataChanged();
		}
	}

	public void removePlugin(final Pluggable plugin)
	{
		this.pluginList.remove(plugin);
		this.fireTableDataChanged();
	}
}