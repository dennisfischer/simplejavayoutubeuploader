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

import org.chaosfisch.youtubeuploader.models.Placeholder;

public interface PlaceholderDao extends CRUDDao<Placeholder>
{
	/**
	 * Event: Before Placeholder-object is added
	 */
	String	PLACEHOLDER_PRE_ADDED		= "placeholderPreAdded";

	/**
	 * Event: Before Placeholder-object is removed
	 */
	String	PLACEHOLDER_PRE_REMOVED		= "placeholderPreRemoved";

	/**
	 * Event: Before Placeholder-object is updated
	 */
	String	PLACEHOLDER_PRE_UPDATED		= "placeholderPreUpdated";

	/**
	 * Event: After Placeholder-object was added
	 */
	String	PLACEHOLDER_POST_ADDED		= "placeholderPostAdded";

	/**
	 * Event: After Placeholder-object was removed
	 */
	String	PLACEHOLDER_POST_REMOVED	= "placeholderPostRemoved";

	/**
	 * Event: After Placeholder-object was updated
	 */
	String	PLACEHOLDER_POST_UPDATED	= "placeholderPostUpdated";
}
