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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.services.impl;

import com.google.inject.Inject;
import org.bushe.swing.event.EventBus;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Preset;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PresetService;
import org.mybatis.guice.transactional.Transactional;
import org.mybatis.mappers.PresetMapper;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 10.01.12
 * Time: 22:13
 * To change this template use File | Settings | File Templates.
 */
public class PresetServiceImpl implements PresetService
{
	@Inject PresetMapper presetMapper;

	@Transactional @Override public Preset createPresetEntry(final Preset preset)
	{
		this.presetMapper.createPreset(preset);
		EventBus.publish(PRESET_ENTRY_ADDED, preset);
		return preset;
	}

	@Transactional @Override public Preset deletePresetEntry(final Preset preset)
	{
		this.presetMapper.deletePreset(preset);
		EventBus.publish(PRESET_ENTRY_REMOVED, preset);
		return preset;
	}

	@Transactional @Override public Preset updatePresetEntry(final Preset preset)
	{
		this.presetMapper.updatePreset(preset);
		EventBus.publish(PRESET_ENTRY_UPDATED, preset);
		return preset;
	}

	@Transactional @Override public List<Preset> getAllPresetEntry()
	{
		return this.presetMapper.getPresets();
	}

	@Transactional @Override public Preset findPresetEntry(final int identifier)
	{
		return this.presetMapper.findPreset(identifier);
	}
}
