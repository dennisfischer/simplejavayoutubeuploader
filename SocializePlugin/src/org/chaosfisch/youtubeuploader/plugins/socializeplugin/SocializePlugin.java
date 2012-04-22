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

package org.chaosfisch.youtubeuploader.plugins.socializeplugin;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.chaosfisch.plugin.ExtensionPoints.JComponentExtensionPoint;
import org.chaosfisch.plugin.PluginService;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.view.SocializeView;
import org.chaosfisch.youtubeuploader.services.settingsservice.spi.SettingsService;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 13.04.12
 * Time: 19:38
 * To change this template use File | Settings | File Templates.
 */
public class SocializePlugin implements org.chaosfisch.plugin.Pluggable
{
	private @Inject Injector        injector;
	private @Inject PluginService   pluginService;
	private @Inject SettingsService settingService;

	public SocializePlugin()
	{
	}

	@Override public void init()
	{
		this.settingService.addTextfield("socialize.socialize.facebook", "Facebook Token:"); //NON-NLS
		this.settingService.addTextfield("socialize.socialize.twitter", "Twitter Token:"); //NON-NLS
		this.settingService.addTextfield("socialize.socialize.googleplus", "Google+ Token:"); //NON-NLS
		this.settingService.addTextfield("socialize.socialize.youtube", "Youtube Token:"); //NON-NLS
	}

	@Override public void onStart()
	{
		if (!GraphicsEnvironment.isHeadless()) {
			final SocializeView socializeView = this.injector.getInstance(SocializeView.class);
			this.pluginService.registerExtension("panel_tabs", new JComponentExtensionPoint("Socialize", socializeView.getPanel())); //NON-NLS
		}
	}

	@Override public void onEnd()
	{
	}

	@Override public String getName()
	{
		return "Socialize Plugin";  //NON-NLS
	}

	@Override public String getAuthor()
	{
		return "CHAOSFISCH"; //NON-NLS
	}

	@Override public boolean canBeDisabled()
	{
		return true;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override public String[] getDependencies()
	{
		return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
	}
}
