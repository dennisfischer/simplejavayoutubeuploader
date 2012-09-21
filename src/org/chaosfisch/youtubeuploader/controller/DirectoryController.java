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

import com.google.inject.Inject;

import org.chaosfisch.youtubeuploader.models.Directory;
import org.chaosfisch.youtubeuploader.models.DirectoryTableModel;
import org.chaosfisch.youtubeuploader.models.Preset;
import org.chaosfisch.youtubeuploader.services.spi.DirectoryService;
import org.chaosfisch.youtubeuploader.services.spi.PresetService;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 15.03.12
 * Time: 20:57
 * To change this template use File | Settings | File Templates.
 */
public class DirectoryController
{
	@Inject private DirectoryTableModel directoryTableModel;
	@Inject private DirectoryService    directoryService;
	@Inject private PresetService       presetService;

	public void run()
	{
		if (directoryTableModel.getRowCount() > 0) {
			directoryTableModel.removeRowRange(0, directoryTableModel.getRowCount() - 1);
		}
		for (final Directory directory : directoryService.getAll()) {
			directoryTableModel.addRow(directory);
		}
	}

	public DirectoryTableModel getDirectoryTableModel()
	{
		return directoryTableModel;
	}

	public void addAction(final boolean activeCheckboxSelected, final String directoryTextFieldText, final Preset presetListSelectedItem)
	{
		final Directory directory = new Directory();
		directory.active = activeCheckboxSelected;
		directory.directory = directoryTextFieldText;
		directory.preset = presetListSelectedItem;
		directoryService.create(directory);
	}

	public void deleteAction(final Directory directory)
	{
		directoryService.delete(directory);
	}

	public void checkboxChangeAction(final boolean activeCheckboxSelected, final Directory directory)
	{
		directory.active = activeCheckboxSelected;
		directoryService.update(directory);
	}
}