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

package org.chaosfisch.youtubeuploader.dao.spi;

import java.io.File;
import java.util.List;

import org.chaosfisch.youtubeuploader.models.Directory;
import org.chaosfisch.youtubeuploader.models.Preset;

public interface DirectoryDao extends CRUDDao<Directory>
{
	/**
	 * Event: Before Directory-object is added
	 */
	String	DIRECTORY_PRE_ADDED		= "directoryPreAdded";

	/**
	 * Event: Before Directory-object is removed
	 */
	String	DIRECTORY_PRE_REMOVED	= "directoryPreRemoved";

	/**
	 * Event: Before Directory-object is updated
	 */
	String	DIRECTORY_PRE_UPDATED	= "directoryPreUpdated";

	/**
	 * Event: After Directory-object was added
	 */
	String	DIRECTORY_POST_ADDED	= "directoryPostAdded";

	/**
	 * Event: After Directory-object was removed
	 */
	String	DIRECTORY_POST_REMOVED	= "directoryPostRemoved";

	/**
	 * Event: After Directory-object was updated
	 */
	String	DIRECTORY_POST_UPDATED	= "directoryPostUpdated";

	/**
	 * Retrieves all active directory-objects from the persistence storage
	 * 
	 * @return List<Directory> active directory objects
	 */
	List<Directory> getActive();

	/**
	 * Retrieves a single directory-object that matches the given file-path
	 * 
	 * @param file
	 *            the file to search with
	 * @return Directory the found directory object
	 */
	Directory findFile(final File file);

	/**
	 * Retrieves all valid presets that can be used for directory observing
	 * process
	 * 
	 * @return List<Preset> valid presets objects
	 */
	List<Preset> findPresets();
}
