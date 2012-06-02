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
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.table.RowTableModel;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.IdentityList;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.services.spi.DirectoryService;

import java.util.Arrays;
import java.util.Collections;
import java.util.ResourceBundle;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 15.03.12
 * Time: 19:10
 * To change this template use File | Settings | File Templates.
 */
public class DirectoryTableModel extends RowTableModel<Directory>
{
	private static final long serialVersionUID = 3730645697933907437L;

	public DirectoryTableModel()
	{
		this(Collections.<Directory>emptyList());
	}

	public DirectoryTableModel(final Iterable<Directory> directories)
	{
		super(Directory.class);
		final ResourceBundle resourceBundle = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.plugins.directoryplugin.resources.directoryplugin");//NON-NLS
		this.setDataAndColumnNames(new IdentityList<Directory>(), Arrays.asList(resourceBundle.getString("button.selectButton"), resourceBundle.getString("directorytable.presetLabel"), resourceBundle.getString("directorytable.activeLabel")));

		for (final Directory directory : directories) {
			this.addRow(directory);
		}

		this.setColumnClass(0, String.class);
		this.setColumnClass(1, String.class);
		this.setColumnClass(2, Boolean.class);
		this.setModelEditable(false);
		AnnotationProcessor.process(this);
	}

	@Override
	public Object getValueAt(final int row, final int col)
	{
		final Directory directory = this.getRow(row);
		switch (col) {
			case 0:
				return directory.directory;
			case 1:
				return directory.preset;
			case 2:
				return directory.active;
			default:
				return null;
		}
	}

	@EventTopicSubscriber(topic = DirectoryService.DIRECTORY_ADDED)
	public void onDirectoryAdded(final String topic, final Directory directory)
	{
		this.addRow(directory);
	}

	@EventTopicSubscriber(topic = DirectoryService.DIRECTORY_REMOVED)
	public void onDirectoryRemoved(final String topic, final Directory directory)
	{
		this.removeElement(directory);
	}
}
