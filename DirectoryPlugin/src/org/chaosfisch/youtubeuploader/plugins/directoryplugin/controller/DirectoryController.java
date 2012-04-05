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

package org.chaosfisch.youtubeuploader.plugins.directoryplugin.controller;

import com.google.inject.Inject;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.entities.PresetEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PresetService;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.models.DirectoryTableModel;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.models.entities.DirectoryEntry;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.services.spi.DirectoryService;

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

	public DirectoryController()
	{

	}

	public void run()
	{
		this.directoryTableModel.addDirectoryList(this.directoryService.getAll());
	}

	public DirectoryTableModel getDirectoryTableModel()
	{
		return this.directoryTableModel;
	}

	public void addAction(final boolean activeCheckboxSelected, final String directoryTextFieldText, final PresetEntry presetListSelectedItem)
	{
		final DirectoryEntry directoryEntry = new DirectoryEntry();
		directoryEntry.setActive(activeCheckboxSelected);
		directoryEntry.setDirectory(directoryTextFieldText);
		directoryEntry.setPreset(presetListSelectedItem);
		this.directoryService.createDirectoryEntry(directoryEntry);
	}

	public void deleteAction(final DirectoryEntry directoryEntry)
	{
		this.directoryService.deleteDirectoryEntry(directoryEntry);
	}

	public void checkboxChangeAction(final boolean activeCheckboxSelected, final DirectoryEntry directoryEntry)
	{
		directoryEntry.setActive(activeCheckboxSelected);
		this.directoryService.updateDirectoryEntry(directoryEntry);
	}
}