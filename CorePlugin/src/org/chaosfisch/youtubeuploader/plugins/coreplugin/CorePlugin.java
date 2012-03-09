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

package org.chaosfisch.youtubeuploader.plugins.coreplugin;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.chaosfisch.plugin.ExtensionPoints.JComponentExtensionPoint;
import org.chaosfisch.plugin.Pluggable;
import org.chaosfisch.plugin.PluginService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.controller.QueueController;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.Uploader;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.view.MenuViewPanel;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.view.QueueViewPanel;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.view.UploadViewPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

@SuppressWarnings({"WeakerAccess", "DuplicateStringLiteralInspection"})
public class CorePlugin implements Pluggable
{
	private final ResourceBundle resourceBundle = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.plugins.coreplugin.resources.plugin"); //NON-NLS
	private         Uploader      uploader;
	@Inject private PluginService pluginService;
	@Inject private Injector      injector;

	public CorePlugin()
	{
	}

	@Override public String getName()
	{
		return "Coreplugin"; //NON-NLS
	}

	@Override public String getAuthor()
	{
		return "CHAOSFISCH"; //NON-NLS
	}

	@Override
	public void init()
	{
		this.uploader = this.injector.getInstance(Uploader.class);
		if (!GraphicsEnvironment.isHeadless()) {
			final UploadViewPanel uploadViewPanel = this.injector.getInstance(UploadViewPanel.class);
			uploadViewPanel.run();
			final MenuViewPanel menuViewPanel = this.injector.getInstance(MenuViewPanel.class);
			final QueueViewPanel queueViewPanel = this.injector.getInstance(QueueViewPanel.class);

			if (this.pluginService != null) {
				this.pluginService.registerExtension("panel_tabs", new JComponentExtensionPoint(this.resourceBundle.getString("uploadTab.title"), uploadViewPanel.getJPanel())); //NON-NLS
				this.pluginService.registerExtension("panel_tabs", new JComponentExtensionPoint(this.resourceBundle.getString("queueTab.title"), queueViewPanel.getJPanel())); //NON-NLS

				for (final JMenuItem menuItem : uploadViewPanel.getFileMenuItem()) {
					this.pluginService.registerExtension("file_menu", new JComponentExtensionPoint("test", menuItem)); //NON-NLS
				}
				for (final JMenu menu : menuViewPanel.getFileMenus()) {
					this.pluginService.registerExtension("file_menu", new JComponentExtensionPoint("test", menu)); //NON-NLS
				}
				for (final JMenuItem menuItem : menuViewPanel.getEditMenuItems()) {
					this.pluginService.registerExtension("edit_menu", new JComponentExtensionPoint("test", menuItem)); //NON-NLS
				}
				final QueueController queueController = queueViewPanel.getQueueController();
				this.pluginService.registerExtension("exit", queueController.uploadExitPoint()); //NON-NLS
			}
		}
	}

	@Override
	public void onStart()
	{
		this.uploader.runStarttimeChecker();
	}

	@Override
	public void onEnd()
	{
		this.uploader.stopStarttimeChecker();
		this.uploader.exit();
	}
}