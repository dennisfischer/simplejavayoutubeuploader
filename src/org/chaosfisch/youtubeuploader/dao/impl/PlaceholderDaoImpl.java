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
import org.chaosfisch.youtubeuploader.dao.mappers.PlaceholderMapper;
import org.chaosfisch.youtubeuploader.dao.spi.PlaceholderDao;
import org.chaosfisch.youtubeuploader.models.Placeholder;
import org.mybatis.guice.transactional.Transactional;

import com.google.inject.Inject;

public class PlaceholderDaoImpl implements PlaceholderDao
{
	@Inject
	PlaceholderMapper	placeholderMapper;

	@Transactional
	@Override
	public Placeholder create(final Placeholder placeholder)
	{
		EventBus.publish(PlaceholderDao.PLACEHOLDER_PRE_ADDED, placeholder);
		placeholderMapper.createPlaceholder(placeholder);
		EventBus.publish(PlaceholderDao.PLACEHOLDER_POST_ADDED, placeholder);
		return placeholder;
	}

	@Transactional
	@Override
	public void delete(final Placeholder placeholder)
	{
		EventBus.publish(PlaceholderDao.PLACEHOLDER_PRE_REMOVED, placeholder);
		placeholderMapper.deletePlaceholder(placeholder);
		EventBus.publish(PlaceholderDao.PLACEHOLDER_POST_REMOVED, placeholder);
	}

	@Transactional
	@Override
	public Placeholder update(final Placeholder placeholder)
	{
		EventBus.publish(PlaceholderDao.PLACEHOLDER_PRE_UPDATED, placeholder);
		placeholderMapper.updatePlaceholder(placeholder);
		EventBus.publish(PlaceholderDao.PLACEHOLDER_POST_UPDATED, placeholder);
		return placeholder;
	}

	@Transactional
	@Override
	public Placeholder find(final Placeholder placeholder)
	{
		return placeholderMapper.findPlaceholder(placeholder);
	}

	@Transactional
	@Override
	public List<Placeholder> getAll()
	{
		return placeholderMapper.getPlaceholders();
	}
}
