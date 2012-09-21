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

package org.chaosfisch.youtubeuploader.services.impl;

import com.google.inject.Inject;
import org.bushe.swing.event.EventBus;
import org.chaosfisch.youtubeuploader.mappers.PresetMapper;
import org.chaosfisch.youtubeuploader.models.Preset;
import org.chaosfisch.youtubeuploader.services.spi.PresetService;
import org.mybatis.guice.transactional.Transactional;

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

	@Transactional @Override public Preset create(final Preset preset)
	{
		EventBus.publish(PresetService.PRESET_PRE_ADDED, preset);
		presetMapper.createPreset(preset);
		EventBus.publish(PresetService.PRESET_ADDED, preset);
		return preset;
	}

	@Transactional @Override public void delete(final Preset preset)
	{
		EventBus.publish(PresetService.PRESET_PRE_REMOVED, preset);
		presetMapper.deletePreset(preset);
		EventBus.publish(PresetService.PRESET_REMOVED, preset);
	}

	@Transactional @Override public Preset update(final Preset preset)
	{
		EventBus.publish(PresetService.PRESET_PRE_UPDATED, preset);
		presetMapper.updatePreset(preset);
		EventBus.publish(PresetService.PRESET_UPDATED, preset);
		return preset;
	}

	@Transactional @Override public List<Preset> getAll()
	{
		return presetMapper.getPresets();
	}

	@Transactional @Override public Preset find(final Preset preset)
	{
		return presetMapper.findPreset(preset);
	}
}
