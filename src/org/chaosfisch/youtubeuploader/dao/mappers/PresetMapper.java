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

import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.models.Preset;

public interface PresetMapper
{
	Preset findPreset(Preset preset);

	List<Preset> findByAccount(Account account);

	List<Preset> findByPlaylist(Playlist playlist);

	List<Preset> getPresets();

	void createPreset(Preset preset);

	void updatePreset(Preset preset);

	void deletePreset(Preset preset);
}
