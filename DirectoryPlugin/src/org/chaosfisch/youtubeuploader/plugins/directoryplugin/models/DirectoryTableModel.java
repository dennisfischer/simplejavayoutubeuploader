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

package org.chaosfisch.youtubeuploader.plugins.directoryplugin.models;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.chaosfisch.plugin.Pluggable;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.db.DirectoryEntry;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 15.03.12
 * Time: 19:10
 * To change this template use File | Settings | File Templates.
 */
public class DirectoryTableModel extends AbstractTableModel
{
	private final ArrayList<DirectoryEntry> directoryList  = new ArrayList<DirectoryEntry>(20);
	private final ResourceBundle            resourceBundle = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.plugins.directoryplugin.resources.directoryplugin"); //NON-NLS
	private final String[]                  columns        = {this.resourceBundle.getString("button.selectButton"), this.resourceBundle.getString("directorytable.presetLabel"),
			this.resourceBundle.getString("directorytable.activeLabel")};

	public DirectoryTableModel()
	{
		AnnotationProcessor.process(this);
	}

	public DirectoryTableModel(final List<DirectoryEntry> l)
	{
		AnnotationProcessor.process(this);
		this.directoryList.addAll(l);
	}

	void addDirectory(final DirectoryEntry directoryEntry)
	{
		this.directoryList.add(directoryEntry);
		this.fireTableDataChanged();
	}

	public void addDirectoryList(final List l)
	{
		for (final Object o : l) {
			if (o instanceof Pluggable) {
				this.addDirectory((DirectoryEntry) o);
			}
		}
	}

	public DirectoryEntry getDirectoryAt(final int row)
	{
		return this.directoryList.get(row);
	}

	public DirectoryEntry removeDirectoryAt(final int row)
	{
		final DirectoryEntry element = this.directoryList.remove(row);
		this.fireTableDataChanged();
		return element;
	}

	@Override
	public int getRowCount()
	{
		return this.directoryList.size();
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
		final DirectoryEntry directoryEntry = this.directoryList.get(row);
		switch (col) {
			case 0:
				return directoryEntry.getDirectory();
			case 1:
				return directoryEntry.getPreset();
			case 2:
				return directoryEntry.isActive();
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
		final DirectoryEntry directoryEntry = this.directoryList.get(row);
		switch (col) {
		}
		this.fireTableCellUpdated(row, col);
	}

	public boolean hasDirectoryAt(final int selectedRow)
	{
		return this.directoryList.size() >= selectedRow && selectedRow != -1;
	}

	public List<DirectoryEntry> getDirectoryList()
	{
		return this.directoryList;
	}

	public void removeAll()
	{
		final Iterator iterator = this.directoryList.iterator();
		while (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
			this.fireTableDataChanged();
		}
	}

	public void removeDirectory(final DirectoryEntry directoryEntry)
	{
		this.directoryList.remove(directoryEntry);
		this.fireTableDataChanged();
	}
}
