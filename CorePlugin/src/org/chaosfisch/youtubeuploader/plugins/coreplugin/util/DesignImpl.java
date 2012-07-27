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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.util;

import org.chaosfisch.youtubeuploader.designmanager.spi.Design;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 29.02.12
 * Time: 18:14
 * To change this template use File | Settings | File Templates.
 */
public class DesignImpl implements Design
{
	private final Class<? extends LookAndFeel> laf;
	private final String                       shortName;
	private final String                       name;

	public DesignImpl(final Class<? extends LookAndFeel> laf, final String shortName, final String name)
	{
		this.laf = laf;
		this.shortName = shortName;
		this.name = name;
	}

	@Override public Class<? extends LookAndFeel> getLaF()
	{
		return laf;
	}

	@Override public String getName()
	{
		return name;
	}

	@Override public String getShortName()
	{
		return shortName;
	}

	@Override public String toString()
	{
		return getName();
	}
}
