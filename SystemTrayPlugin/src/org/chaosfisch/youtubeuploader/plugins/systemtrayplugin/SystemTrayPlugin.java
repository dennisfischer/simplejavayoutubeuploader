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

package org.chaosfisch.youtubeuploader.plugins.systemtrayplugin;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.plugin.Pluggable;
import org.chaosfisch.plugin.PluginService;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;

public class SystemTrayPlugin implements Pluggable
{
	private static final String[] DEPENDENCIES = new String[0];
	@Inject private                             PluginService pluginService;
	@Inject @Named(value = "mainFrame") private JFrame        mainFrame;
	private                                     TrayIcon      trayIcon;
	public static final String MESSAGE = "onMessage"; //NON-NLS

	private final ResourceBundle resourceBundle = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.plugins.systemtrayplugin.resources.systemtrayplugin"); //NON-NLS

	public SystemTrayPlugin()
	{
		AnnotationProcessor.process(this);
	}

	@Override public boolean canBeDisabled()
	{
		return true;
	}

	@Override public String[] getDependencies()
	{
		return DEPENDENCIES;
	}

	@Override public String getAuthor()
	{
		return "CHAOSFISCH"; //NON-NLS
	}

	@Override public String getName()
	{
		return "SystemTray Plugin"; //NON-NLS
	}

	@Override
	public void init()
	{
	}

	@Override
	public void onStart()
	{
		if (!SystemTray.isSupported()) {
			return;
		}

		final Image image = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/youtubeuploader/resources/images/film.png")); //NON-NLS

		final PopupMenu popup = new PopupMenu();
		final MenuItem itemOpen = new MenuItem(this.resourceBundle.getString("openApplicationLabel"));
		itemOpen.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(final ActionEvent e)
			{
				SystemTrayPlugin.this.mainFrame.setVisible(true);
				SystemTrayPlugin.this.mainFrame.setExtendedState(JFrame.NORMAL);
				SystemTrayPlugin.this.mainFrame.setAlwaysOnTop(true);
				SystemTrayPlugin.this.mainFrame.requestFocus();
				SystemTrayPlugin.this.mainFrame.setAlwaysOnTop(false);
			}
		});
		final MenuItem itemEnd = new MenuItem(this.resourceBundle.getString("closeApplicationLabel"));
		itemEnd.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(final ActionEvent e)
			{
				SystemTrayPlugin.this.mainFrame.dispose();
			}
		});
		popup.add(itemOpen);
		popup.add(itemEnd);
		this.trayIcon = new TrayIcon(image, this.mainFrame.getTitle(), popup);
		this.trayIcon.setImageAutoSize(true);
		this.trayIcon.addMouseListener(new MouseInputAdapter()
		{

			@Override
			public void mouseClicked(final MouseEvent e)
			{
				if (e.getButton() == MouseEvent.BUTTON1) {
					SystemTrayPlugin.this.mainFrame.setVisible(true);
					SystemTrayPlugin.this.mainFrame.setExtendedState(JFrame.NORMAL);
					SystemTrayPlugin.this.mainFrame.setAlwaysOnTop(true);
					SystemTrayPlugin.this.mainFrame.requestFocus();
					SystemTrayPlugin.this.mainFrame.setAlwaysOnTop(false);
				}
			}
		});
		try {
			SystemTray.getSystemTray().add(this.trayIcon);
			this.mainFrame.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowIconified(final WindowEvent e)
				{
					SystemTrayPlugin.this.trayIcon.displayMessage(SystemTrayPlugin.this.resourceBundle.getString("informationMessageLabel"),
							SystemTrayPlugin.this.resourceBundle.getString("applicationMinimizedMessage"), TrayIcon.MessageType.INFO);
					SystemTrayPlugin.this.mainFrame.setVisible(false);
				}
			});
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onEnd()
	{
		if (SystemTray.isSupported()) {
			SystemTray.getSystemTray().remove(this.trayIcon);
		}
	}

	@EventTopicSubscriber(topic = SystemTrayPlugin.MESSAGE)
	public void onMessage(final String topic, final Object o)
	{
		this.trayIcon.displayMessage(this.resourceBundle.getString("informationMessageLabel"), o.toString(), TrayIcon.MessageType.INFO);
	}
}
