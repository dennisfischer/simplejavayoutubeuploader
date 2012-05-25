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
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicPatternSubscriber;
import org.bushe.swing.event.annotation.ReferenceStrength;
import org.chaosfisch.plugin.ExtensionPoints.JComponentExtensionPoint;
import org.chaosfisch.plugin.Pluggable;
import org.chaosfisch.plugin.PluginService;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.models.Directory;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.view.DirectoryViewPanel;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.worker.DirectoryWorker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 02.03.12
 * Time: 17:00
 * To change this template use File | Settings | File Templates.
 */
public class DirectoryPlugin implements Pluggable
{
	private static final String[] DEPENDENCIES = new String[]{"org.chaosfisch.youtubeuploader.plugins.settingsplugin.SettingsPlugin", "org.chaosfisch.youtubeuploader.plugins.coreplugin.CorePlugin"};
	@Inject private PluginService   pluginService;
	@Inject private Injector        injector;
	@Inject         DirectoryWorker directoryWorker;

	public DirectoryPlugin()
	{
		AnnotationProcessor.process(this);
	}

	@Override public String[] getDependencies()
	{
		return DEPENDENCIES;
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

		final JMenuItem menuItem = new JMenuItem("Ordnerüberwachung", new ImageIcon(this.getClass().getResource("/youtubeuploader/resources/images/folder_explore.png")));
		menuItem.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				final DirectoryViewPanel directoryViewPanel = DirectoryPlugin.this.injector.getInstance(DirectoryViewPanel.class);
				directoryViewPanel.run();
				directoryViewPanel.pack();
				directoryViewPanel.setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/youtubeuploader/resources/images/folder_explore.png")));
				directoryViewPanel.setTitle("Ordnerüberwachung");
				directoryViewPanel.setVisible(true);
			}
		});
		this.pluginService.registerExtension("edit_menu", new JComponentExtensionPoint("test", menuItem)); //NON-NLS

		this.directoryWorker.setIntervall(30000);
		this.directoryWorker.start();
	}

	@Override public void onEnd()
	{
		this.directoryWorker.interrupt();
	}

	@EventTopicPatternSubscriber(topicPattern = "onDirectoryEntry(.*)", referenceStrength = ReferenceStrength.WEAK)
	public void refreshDirectoryWorker(final String topic, final Directory directory)
	{
		this.directoryWorker.interrupt();
		this.directoryWorker = this.injector.getInstance(DirectoryWorker.class);
		this.directoryWorker.setIntervall(30000);
		this.directoryWorker.start();
	}
}