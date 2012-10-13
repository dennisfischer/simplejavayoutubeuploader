/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.bushe.swing.event.annotation.EventTopicSubscriber;

public class SystemTrayPlugin
{

	public static final String	MESSAGE	= "onMessage";
	private TrayIcon			trayIcon;

	public void onEnd()
	{
		if (SystemTray.isSupported())
		{
			SystemTray.getSystemTray().remove(trayIcon);
		}
	}

	@EventTopicSubscriber(topic = SystemTrayPlugin.MESSAGE)
	public void onMessage(final String topic, final Object o)
	{
		if (trayIcon != null)
		{
			trayIcon.displayMessage(I18nHelper.message("informationMessageLabel"), o.toString(), TrayIcon.MessageType.INFO);
		}
	}

	public void onStart()
	{
		if (!SystemTray.isSupported()) { return; }

		final Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/youtubeuploader/resources/images/film.png"));

		final PopupMenu popup = new PopupMenu();
		final MenuItem itemOpen = new MenuItem(I18nHelper.message("openApplicationLabel"));
		itemOpen.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e)
			{
				// @TODO Show Window
			}
		});
		final MenuItem itemEnd = new MenuItem(I18nHelper.message("closeApplicationLabel"));
		itemEnd.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e)
			{
				// @TODO close application
			}
		});
		popup.add(itemOpen);
		popup.add(itemEnd);
		trayIcon = new TrayIcon(image, I18nHelper.message("application.title"), popup);
		trayIcon.setImageAutoSize(true);
		trayIcon.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent e)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseClicked(MouseEvent e)
			{
				// TODO Auto-generated method stub

			}
		});

		try
		{
			SystemTray.getSystemTray().add(trayIcon);
		} catch (final AWTException e1)
		{
			e1.printStackTrace();
		}
	}
}