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

package org.chaosfisch.youtubeuploader.plugins.directoryplugin.models;

import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.IModel;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Preset;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 07.01.12
 * Time: 18:33
 * To change this template use File | Settings | File Templates.
 */
public class Directory implements IModel
{
	public            String  directory;
	public            boolean active;
	public            boolean locked;
	private transient int     identity;
	public            Preset  preset;

	@Override public int getIdentity()
	{
		return this.identity;
	}
}
