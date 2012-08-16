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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi;

import org.chaosfisch.util.CRUDService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Preset;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 10.01.12
 * Time: 21:47
 * To change this template use File | Settings | File Templates.
 */
public interface PresetService extends CRUDService<Preset>
{
	String PRESET_PRE_ADDED   = "presetPreAdded"; //NON-NLS
	String PRESET_ADDED       = "presetAdded"; //NON-NLS
	String PRESET_PRE_REMOVED = "presetPreRemoved"; //NON-NLS
	String PRESET_REMOVED     = "presetRemoved"; //NON-NLS
	String PRESET_PRE_UPDATED = "presetPreUpdated"; //NON-NLS
	String PRESET_UPDATED     = "presetUpdated"; //NON-NLS
}
