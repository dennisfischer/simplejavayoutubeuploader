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

package org.chaosfisch.youtubeuploader.services;

import java.util.Map;

public interface SettingsPersister
{
	boolean has(String uniqueKey);

	Object get(String uniqueKey);

	void set(String uniqueKey, String value);

	void save();

	Map<String, Object> getAll();
}
