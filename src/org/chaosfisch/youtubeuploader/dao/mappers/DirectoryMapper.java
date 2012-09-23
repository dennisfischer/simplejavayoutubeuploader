/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Dennis Fischer
 ******************************************************************************/

package org.chaosfisch.youtubeuploader.dao.mappers;

import java.util.List;

import org.chaosfisch.youtubeuploader.models.Directory;
import org.chaosfisch.youtubeuploader.models.Preset;

public interface DirectoryMapper
{
	List<Directory> getDirectories();

	Directory findDirectories(Directory directory);

	void deleteDirectory(Directory directory);

	void createDirectory(Directory directory);

	void updateDirectory(Directory directory);

	List<Preset> getQualifiedPresets();

	List<Directory> findMultiple(Directory findEntry);
}
