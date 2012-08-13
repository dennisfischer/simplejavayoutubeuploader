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
import org.chaosfisch.youtubeuploader.plugins.coreplugin.mappers.PlaceholderMapper;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Placeholder;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PlaceholderService;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 05.07.12
 * Time: 21:14
 * To change this template use File | Settings | File Templates.
 */
public class PlaceholderServiceImpl implements PlaceholderService
{
	@Inject PlaceholderMapper placeholderMapper;

	@Override public Placeholder create(final Placeholder placeholder)
	{
		placeholderMapper.createPlaceholder(placeholder);
		EventBus.publish(PlaceholderService.PLACEHOLDER_ADDED, placeholder);
		return placeholder;
	}

	@Override public void delete(final Placeholder placeholder)
	{
		placeholderMapper.deletePlaceholder(placeholder);
		EventBus.publish(PlaceholderService.PLACEHOLDER_REMOVED, placeholder);
	}

	@Override public Placeholder update(final Placeholder placeholder)
	{
		placeholderMapper.updatePlaceholder(placeholder);
		EventBus.publish(PlaceholderService.PLACEHOLDER_UPDATED, placeholder);
		return placeholder;
	}

	@Override public Placeholder find(final Placeholder placeholder)
	{
		return placeholderMapper.findPlaceholder(placeholder);
	}

	@Override public List<Placeholder> getAll()
	{
		return placeholderMapper.getPlaceholders();
	}
}
