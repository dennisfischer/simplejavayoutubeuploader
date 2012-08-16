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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.mappers;

import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Account;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Playlist;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Preset;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 06.05.12
 * Time: 08:34
 * To change this template use File | Settings | File Templates.
 */
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
