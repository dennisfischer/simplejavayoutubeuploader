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

package org.chaosfisch.youtubeuploader;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.Reader;
import java.util.ResourceBundle;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSessionFactory;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.chaosfisch.util.plugin.PluginService;
import org.chaosfisch.util.plugin.ExtensionPoints.JComponentExtensionPoint;
import org.chaosfisch.youtubeuploader.controller.QueueController;
import org.chaosfisch.youtubeuploader.controller.UploadController;
import org.chaosfisch.youtubeuploader.services.spi.AccountService;
import org.chaosfisch.youtubeuploader.services.spi.PlaylistService;
import org.chaosfisch.youtubeuploader.services.spi.SettingsService;
import org.chaosfisch.youtubeuploader.services.uploader.Uploader;
import org.chaosfisch.youtubeuploader.view.MenuViewPanel;
import org.chaosfisch.youtubeuploader.view.QueueViewPanel;
import org.chaosfisch.youtubeuploader.view.UploadViewPanel;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class CorePlugin
{
	private static final String[]	DEPENDENCIES	= { "org.chaosfisch.youtubeuploader.plugins.settingsplugin.SettingsPlugin" };
	private final ResourceBundle	resourceBundle	= ResourceBundle
															.getBundle("org.chaosfisch.youtubeuploader.plugins.coreplugin.resources.coreplugin");	// NON-NLS
	private Uploader				uploader;
	@Inject
	private PluginService			pluginService;
	@Inject
	private Injector				injector;
	@Inject
	private SettingsService			settingsService;
	@Inject
	private final SqlSessionFactory	sessionFactory;
	private UploadController		uploadController;
	private QueueController			queueController;
	private AccountService			accountService;
	private PlaylistService			playlistService;

	@Inject
	public CorePlugin(final SqlSessionFactory sessionFactory) throws IOException
	{
		this.sessionFactory = sessionFactory;
		AnnotationProcessor.process(this);
		loadDatabase();
	}

	// uses the new MyBatis style of lookup
	public void loadDatabase() throws IOException
	{
		final Reader schemaReader = Resources.getResourceAsReader("scripts/scheme.sql");// NON-NLS
		final ScriptRunner scriptRunner = new ScriptRunner(sessionFactory.openSession().getConnection());
		scriptRunner.setStopOnError(true);
		scriptRunner.setLogWriter(null);
		scriptRunner.setAutoCommit(true);
		scriptRunner.setDelimiter(";");
		scriptRunner.runScript(schemaReader);
	}

	public void init()
	{
		uploader = injector.getInstance(Uploader.class);

		final JSpinner spinner = new JSpinner(new SpinnerNumberModel(10, 5, 500, 5));
		spinner.setEditor(new JSpinner.NumberEditor(spinner, resourceBundle.getString("chunksize_spinner")));
		spinner.setValue(Integer.parseInt((String) settingsService.get("coreplugin.general.chunk_size", "10"))); // NON-NLS

		settingsService.addSpinner("coreplugin.general.chunk_size", resourceBundle.getString("chunksize_spinner.label"), spinner); // NON-NLS
		settingsService.addCheckbox("coreplugin.general.enddirtitle", resourceBundle.getString("enddircheckbox.label")); // NON-NLS
		if (!GraphicsEnvironment.isHeadless())
		{
			final UploadViewPanel uploadViewPanel = injector.getInstance(UploadViewPanel.class);
			uploadViewPanel.run();
			final MenuViewPanel menuViewPanel = injector.getInstance(MenuViewPanel.class);
			final QueueViewPanel queueViewPanel = injector.getInstance(QueueViewPanel.class);

			if (pluginService != null)
			{
				pluginService.registerExtension("panel_tabs", new JComponentExtensionPoint(resourceBundle.getString("uploadTab.title"),
						uploadViewPanel.getJPanel())); // NON-NLS
				pluginService.registerExtension("panel_tabs",
						new JComponentExtensionPoint(resourceBundle.getString("queueTab.title"), queueViewPanel.getJPanel())); // NON-NLS

				for (final JMenuItem menuItem : uploadViewPanel.getFileMenuItem())
				{
					pluginService.registerExtension("file_menu", new JComponentExtensionPoint("test", menuItem)); // NON-NLS
				}
				for (final JMenu menu : menuViewPanel.getFileMenus())
				{
					pluginService.registerExtension("file_menu", new JComponentExtensionPoint("test", menu)); // NON-NLS
				}
				for (final JMenuItem menuItem : menuViewPanel.getEditMenuItems())
				{
					pluginService.registerExtension("edit_menu", new JComponentExtensionPoint("test", menuItem)); // NON-NLS
				}
				final QueueController queueController = queueViewPanel.getQueueController();
				pluginService.registerExtension("exit", queueController.uploadExitPoint()); // NON-NLS
			}
		} else
		{
			uploadController = injector.getInstance(UploadController.class);
			queueController = injector.getInstance(QueueController.class);
			accountService = injector.getInstance(AccountService.class);
			playlistService = injector.getInstance(PlaylistService.class);
		}
	}

	public void onStart()
	{
		uploader.runStarttimeChecker();
	}

	public void onEnd()
	{
		uploader.stopStarttimeChecker();
		uploader.exit();
	}
}