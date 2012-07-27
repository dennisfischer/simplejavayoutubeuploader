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
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSessionFactory;
import org.chaosfisch.plugin.ExtensionPoints.JComponentExtensionPoint;
import org.chaosfisch.plugin.Pluggable;
import org.chaosfisch.plugin.PluginService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.controller.QueueController;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.Uploader;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.view.MenuViewPanel;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.view.QueueViewPanel;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.view.UploadViewPanel;
import org.chaosfisch.youtubeuploader.services.settingsservice.spi.SettingsService;
import org.intellij.lang.annotations.Language;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.shell.Global;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.Reader;
import java.util.ResourceBundle;

@SuppressWarnings({"WeakerAccess", "DuplicateStringLiteralInspection"})
public class CorePlugin implements Pluggable
{
	private static final String[]       DEPENDENCIES   = {"org.chaosfisch.youtubeuploader.plugins.settingsplugin.SettingsPlugin"};
	private final        ResourceBundle resourceBundle = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.plugins.coreplugin.resources.coreplugin"); //NON-NLS
	private               Uploader          uploader;
	@Inject private       PluginService     pluginService;
	@Inject private       Injector          injector;
	@Inject private       SettingsService   settingService;
	@Inject private final SqlSessionFactory sessionFactory;

	@Inject
	public CorePlugin(final SqlSessionFactory sessionFactory) throws IOException
	{
		this.sessionFactory = sessionFactory;
		loadDatabase();
	}

	// uses the new MyBatis style of lookup
	public void loadDatabase() throws IOException
	{
		final Reader schemaReader = Resources.getResourceAsReader("scheme.sql");//NON-NLS
		final ScriptRunner scriptRunner = new ScriptRunner(sessionFactory.openSession().getConnection());
		scriptRunner.setStopOnError(true);
		scriptRunner.setLogWriter(null);
		scriptRunner.setAutoCommit(true);
		scriptRunner.setDelimiter(";");
		scriptRunner.runScript(schemaReader);
	}

	@Override public String[] getDependencies()
	{
		return CorePlugin.DEPENDENCIES.clone();
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
		uploader = injector.getInstance(Uploader.class);

		final JSpinner spinner = new JSpinner(new SpinnerNumberModel(10, 5, 500, 5));
		spinner.setEditor(new JSpinner.NumberEditor(spinner, resourceBundle.getString("chunksize_spinner")));
		spinner.setValue(Integer.parseInt((String) settingService.get("coreplugin.general.chunk_size", "10")));

		settingService.addSpinner("coreplugin.general.chunk_size", resourceBundle.getString("chunksize_spinner.label"), spinner);
		if (!GraphicsEnvironment.isHeadless()) {
			final UploadViewPanel uploadViewPanel = injector.getInstance(UploadViewPanel.class);
			uploadViewPanel.run();
			final MenuViewPanel menuViewPanel = injector.getInstance(MenuViewPanel.class);
			final QueueViewPanel queueViewPanel = injector.getInstance(QueueViewPanel.class);

			if (pluginService != null) {
				pluginService.registerExtension("panel_tabs", new JComponentExtensionPoint(resourceBundle.getString("uploadTab.title"), uploadViewPanel.getJPanel())); //NON-NLS
				pluginService.registerExtension("panel_tabs", new JComponentExtensionPoint(resourceBundle.getString("queueTab.title"), queueViewPanel.getJPanel())); //NON-NLS

				for (final JMenuItem menuItem : uploadViewPanel.getFileMenuItem()) {
					pluginService.registerExtension("file_menu", new JComponentExtensionPoint("test", menuItem)); //NON-NLS
				}
				for (final JMenu menu : menuViewPanel.getFileMenus()) {
					pluginService.registerExtension("file_menu", new JComponentExtensionPoint("test", menu)); //NON-NLS
				}
				for (final JMenuItem menuItem : menuViewPanel.getEditMenuItems()) {
					pluginService.registerExtension("edit_menu", new JComponentExtensionPoint("test", menuItem)); //NON-NLS
				}
				final QueueController queueController = queueViewPanel.getQueueController();
				pluginService.registerExtension("exit", queueController.uploadExitPoint()); //NON-NLS
			}
		}
	}

	@Override
	public void onStart()
	{
		uploader.runStarttimeChecker();

		@Language("JavaScript") String script = "load('F:/env.js');";

		try {
			final Context cx = Context.enter();
			final Global scope = new Global();
			scope.init(cx);
			final Scriptable scriptable = cx.newObject(scope);
			cx.evaluateString(scope, script, "script", 1, null);
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} finally {
			Context.exit();
		}
	}

	@Override
	public void onEnd()
	{
		uploader.stopStarttimeChecker();
		uploader.exit();
	}
}