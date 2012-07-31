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

package org.chaosfisch.youtubeuploader.plugins.directoryplugin;/*
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
import com.google.inject.name.Named;
import org.apache.log4j.Logger;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicPatternSubscriber;
import org.bushe.swing.event.annotation.ReferenceStrength;
import org.chaosfisch.plugin.ExtensionPoints.JComponentExtensionPoint;
import org.chaosfisch.plugin.Pluggable;
import org.chaosfisch.plugin.PluginService;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.models.Directory;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.view.DirectoryViewPanel;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.worker.DirectoryWorker;
import org.chaosfisch.youtubeuploader.util.logger.InjectLogger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 02.03.12
 * Time: 17:00
 * To change this template use File | Settings | File Templates.
 */
public class DirectoryPlugin implements Pluggable
{
	private static final String[]       DEPENDENCIES   = {"org.chaosfisch.youtubeuploader.plugins.settingsplugin.SettingsPlugin", "org.chaosfisch.youtubeuploader.plugins.coreplugin.CorePlugin"};
	private final        ResourceBundle resourceBundle = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.plugins.directoryplugin.resources.directoryplugin", Locale.getDefault());//NON-NLS
	@Inject private                              PluginService   pluginService;
	@Inject(optional = true) @Named("mainFrame") JFrame          mainFrame;
	@Inject                                      DirectoryWorker directoryWorker;
	@Inject                                      Injector        injector;
	@InjectLogger                                Logger          logger;

	public DirectoryPlugin()
	{
		AnnotationProcessor.process(this);
	}

	@Override public String[] getDependencies()
	{
		return DirectoryPlugin.DEPENDENCIES.clone();
	}

	@Override public String getCLIName()
	{
		return "directory"; //NON-NLS
	}

	@Override public String getName()
	{
		return "Ordnerüberwachungsplugin"; //NON-NLS
	}

	@Override public String getAuthor()
	{
		return "CHAOSFISCH"; //NON-NLS
	}

	@Override public void init()
	{
	}

	@Override public void onStart()
	{

		final JMenuItem menuItem = new JMenuItem(resourceBundle.getString("directoryobserver.label"), new ImageIcon(getClass().getResource("/youtubeuploader/resources/images/folder_explore.png")));//NON-NLS
		menuItem.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				final DirectoryViewPanel directoryViewPanel = injector.getInstance(DirectoryViewPanel.class);
				directoryViewPanel.pack();
				directoryViewPanel.setLocationRelativeTo(mainFrame);
				directoryViewPanel.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/youtubeuploader/resources/images/folder_explore.png")));//NON-NLS
				directoryViewPanel.setTitle(resourceBundle.getString("directoryobserver.label"));
				directoryViewPanel.setVisible(true);
			}
		});
		pluginService.registerExtension("edit_menu", new JComponentExtensionPoint("test", menuItem)); //NON-NLS
		directoryWorker.start();
	}

	@Override public void onEnd()
	{
		directoryWorker.interrupt();
	}

	@EventTopicPatternSubscriber(topicPattern = "onDirectory(.*)", referenceStrength = ReferenceStrength.WEAK)
	public void refreshDirectoryWorker(final String topic, final Directory directory)
	{
		logger.debug("Refreshing directory worker!"); //NON-NLS
		directoryWorker.stopActions();
		directoryWorker.interrupt();
		directoryWorker = injector.getInstance(DirectoryWorker.class);
		directoryWorker.start();
	}
}