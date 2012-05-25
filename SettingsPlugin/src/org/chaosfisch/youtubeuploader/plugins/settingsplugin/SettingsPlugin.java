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

package org.chaosfisch.youtubeuploader.plugins.settingsplugin;/*
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

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.chaosfisch.plugin.ExtensionPoints.JComponentExtensionPoint;
import org.chaosfisch.plugin.Pluggable;
import org.chaosfisch.plugin.PluginService;
import org.chaosfisch.youtubeuploader.plugins.settingsplugin.view.SettingsViewPanel;

import java.awt.*;
import java.util.ResourceBundle;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 07.03.12
 * Time: 14:38
 * To change this template use File | Settings | File Templates.
 */
public class SettingsPlugin implements Pluggable
{
	private static final String[] DEPENDENCIES = new String[0];
	@Inject private Injector      injector;
	@Inject private PluginService pluginService;

	private final ResourceBundle resourceBundle = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.plugins.settingsplugin.resources.settings"); //NON-NLS

	@Override public String[] getDependencies()
	{
		return DEPENDENCIES;
	}

	@Override public void init()
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override public void onStart()
	{
		if (!GraphicsEnvironment.isHeadless()) {
			final SettingsViewPanel settingsViewPanel = this.injector.getInstance(SettingsViewPanel.class);
			this.pluginService.registerExtension("panel_tabs", new JComponentExtensionPoint(this.resourceBundle.getString("settingsTab.title"), settingsViewPanel.getJPanel())); //NON-NLS
		}
	}

	@Override public void onEnd()
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override public String getName()
	{
		return "SettingsPlugin"; //NON-NLS
	}

	@Override public String getAuthor()
	{
		return "CHAOSFISCH"; //NON-NLS
	}
}
