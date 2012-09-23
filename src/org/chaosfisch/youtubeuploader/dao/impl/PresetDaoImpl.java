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

package org.chaosfisch.youtubeuploader.dao.impl;

import java.util.List;

import org.bushe.swing.event.EventBus;
import org.chaosfisch.youtubeuploader.dao.mappers.PresetMapper;
import org.chaosfisch.youtubeuploader.dao.spi.PresetDao;
import org.chaosfisch.youtubeuploader.models.Preset;
import org.mybatis.guice.transactional.Transactional;

import com.google.inject.Inject;

public class PresetDaoImpl implements PresetDao
{
	@Inject
	PresetMapper	presetMapper;

	@Transactional
	@Override
	public Preset create(final Preset preset)
	{
		EventBus.publish(PresetDao.PRESET_PRE_ADDED, preset);
		presetMapper.createPreset(preset);
		EventBus.publish(PresetDao.PRESET_POST_ADDED, preset);
		return preset;
	}

	@Transactional
	@Override
	public void delete(final Preset preset)
	{
		EventBus.publish(PresetDao.PRESET_PRE_REMOVED, preset);
		presetMapper.deletePreset(preset);
		EventBus.publish(PresetDao.PRESET_POST_REMOVED, preset);
	}

	@Transactional
	@Override
	public Preset update(final Preset preset)
	{
		EventBus.publish(PresetDao.PRESET_PRE_UPDATED, preset);
		presetMapper.updatePreset(preset);
		EventBus.publish(PresetDao.PRESET_POST_UPDATED, preset);
		return preset;
	}

	@Transactional
	@Override
	public List<Preset> getAll()
	{
		return presetMapper.getPresets();
	}

	@Transactional
	@Override
	public Preset find(final Preset preset)
	{
		return presetMapper.findPreset(preset);
	}
}
