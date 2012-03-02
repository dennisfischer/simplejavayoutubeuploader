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

package org.chaosfisch.youtubeuploader.services;

import org.chaosfisch.youtubeuploader.db.PresetEntry;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 10.01.12
 * Time: 21:47
 * To change this template use File | Settings | File Templates.
 */
public interface PresetService
{
	public static final String PRESET_ENTRY_ADDED   = "presetEntryAdded";
	public static final String PRESET_ENTRY_REMOVED = "presetEntryRemoved";

	PresetEntry createPresetEntry(PresetEntry presetEntry);

	PresetEntry deletePresetEntry(PresetEntry presetEntry);

	PresetEntry updatePresetEntry(PresetEntry presetEntry);

	List getAllPresetEntry();

	PresetEntry findPresetEntry(int identifier);
}
