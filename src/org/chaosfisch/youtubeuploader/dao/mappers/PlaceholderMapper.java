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

import org.chaosfisch.youtubeuploader.models.Placeholder;

public interface PlaceholderMapper
{
	Placeholder findPlaceholder(Placeholder placeholder);

	List<Placeholder> getPlaceholders();

	void deletePlaceholder(Placeholder placeholder);

	void createPlaceholder(Placeholder placeholder);

	void updatePlaceholder(Placeholder placeholder);
}
