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

package org.chaosfisch.youtubeuploader.services.settingsservice.impl;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 07.03.12
 * Time: 18:52
 * To change this template use File | Settings | File Templates.
 */
public class SettingsViewComponent
{
	private String     label;
	private JComponent component;
	private String     uniqueKey;

	public SettingsViewComponent(final String uniqueKey, final String label, final JComponent component)
	{
		this.uniqueKey = uniqueKey;
		this.label = label;
		this.component = component;
	}

	public JComponent getComponent()
	{
		return component;
	}

	public void setComponent(final JComponent component)
	{
		this.component = component;
	}

	public String getUniqueKey()
	{
		return uniqueKey;
	}

	public void setUniqueKey(final String uniqueKey)
	{
		this.uniqueKey = uniqueKey;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(final String label)
	{
		this.label = label;
	}
}

