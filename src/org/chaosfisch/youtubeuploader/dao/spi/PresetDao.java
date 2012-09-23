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

import org.chaosfisch.youtubeuploader.models.Preset;

public interface PresetDao extends CRUDDao<Preset>
{
	/**
	 * Event: Before Preset-object is added
	 */
	String	PRESET_PRE_ADDED	= "presetPreAdded";

	/**
	 * Event: Before Preset-object is removed
	 */
	String	PRESET_PRE_REMOVED	= "presetPreRemoved";

	/**
	 * Event: Before Preset-object is updated
	 */
	String	PRESET_PRE_UPDATED	= "presetPreUpdated";

	/**
	 * Event: After Preset-object was added
	 */
	String	PRESET_POST_ADDED	= "presetPostAdded";

	/**
	 * Event: After Preset-object was removed
	 */
	String	PRESET_POST_REMOVED	= "presetPostRemoved";

	/**
	 * Event: After Preset-object was updated
	 */
	String	PRESET_POST_UPDATED	= "presetPostUpdated";
}
