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

package org.chaosfisch.youtubeuploader.view;

import com.google.inject.Inject;
import org.chaosfisch.plugin.Pluggable;
import org.chaosfisch.table.RowTableModel;
import org.chaosfisch.youtubeuploader.controller.PluginController;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;

public class PluginViewPanel extends JDialog
{
	private static final long serialVersionUID = -7964085115455095508L;
	private JPanel  contentPane;
	private JTable  pluginTable;
	private JButton addButton;
	private JButton removeButton;

	@Inject private PluginController pluginController;

	public PluginViewPanel()
	{
		setContentPane(contentPane);
		setModal(true);
	}

	public void run()
	{
		pluginTable.setModel(pluginController.getPluginTableModel());
	}

	public void setPluggableList(final Collection<Pluggable> pluggableList)
	{
		final RowTableModel<Pluggable> rowTableModel = pluginController.getPluginTableModel();
		if (rowTableModel.getRowCount() > 0) {
			rowTableModel.removeRowRange(0, rowTableModel.getRowCount() - 1);
		}
		rowTableModel.insertRows(pluginTable.getModel().getRowCount(), new ArrayList<Pluggable>(pluggableList));
	}
}
