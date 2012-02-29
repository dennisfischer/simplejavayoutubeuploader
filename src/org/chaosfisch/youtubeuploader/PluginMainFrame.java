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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.log4j.Logger;
import org.chaosfisch.plugin.ExtensionPoints.ExitExtensionPoint;
import org.chaosfisch.plugin.ExtensionPoints.ExtensionPoint;
import org.chaosfisch.plugin.ExtensionPoints.JComponentExtensionPoint;
import org.chaosfisch.plugin.Pluggable;
import org.chaosfisch.plugin.PluginManager;
import org.chaosfisch.plugin.PluginService;
import org.chaosfisch.util.Computer;
import org.chaosfisch.youtubeuploader.util.PluginLoader;
import org.chaosfisch.youtubeuploader.util.logger.InjectLogger;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 29.12.11
 * Time: 16:08
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("ALL")
public class PluginMainFrame extends JComponent
{

	private static final String MENU_BAR = "menuBar";

	private                             JTabbedPane   tabbedPane;
	private                             JMenuBar      menuBar;
	@Inject @Named("mainFrame") private JFrame        mainFrame;
	@Inject private                     PluginManager pluginManager;
	@Inject private                     PluginService pluginService;
	@Inject private                     PluginLoader  pluginLoader;
	private final ResourceBundle resourceBundle = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.resources.application");
	@InjectLogger private Logger logger;

	public PluginMainFrame()
	{
	}

	public void run()
	{
		initComponents();
		initPlugins();
		showFrame();
		repaintLaf();
	}

	private void showFrame()
	{
		mainFrame.setTitle(resourceBundle.getString("application.title"));
		mainFrame.setJMenuBar(menuBar);
		mainFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/youtubeuploader/resources/images/film.png")));
		mainFrame.add(tabbedPane);
		mainFrame.setVisible(true);
	}

	private void repaintLaf()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				if (!(mainFrame == null)) {
					SwingUtilities.updateComponentTreeUI(mainFrame.getRootPane());
					mainFrame.pack();
				}
			}
		});
	}

	private void initPlugins()
	{
		List<Pluggable> pluggableList = pluginManager.loadPlugins(new File("plugins/"));
		for (final Pluggable p : pluggableList) {
			p.init();
		}

		logger.debug("Start Plugins");
		pluginManager.startPlugins();
		logger.debug("Process Extensionpoints");
		for (ExtensionPoint tabs : pluginService.getExtensions("panel_tabs")) {
			logger.debug("Extension point");
			if (tabs instanceof JComponentExtensionPoint && ((JComponentExtensionPoint) tabs).getJComponent() instanceof JPanel) {
				logger.debug("Adding panel_tab: " + ((JComponentExtensionPoint) tabs).getTitle());
				tabbedPane.addTab(((JComponentExtensionPoint) tabs).getTitle(), (JPanel) ((JComponentExtensionPoint) tabs).getJComponent());
			}
		}

		JMenu fileMenu = new JMenu("Datei");

		for (ExtensionPoint fileMenuItem : pluginService.getExtensions("file_menu")) {
			if (fileMenuItem instanceof JComponentExtensionPoint) {
				if (((JComponentExtensionPoint) fileMenuItem).getJComponent() instanceof JMenuItem) {
					fileMenu.add((JMenuItem) ((JComponentExtensionPoint) fileMenuItem).getJComponent());
				} else if (((JComponentExtensionPoint) fileMenuItem).getJComponent() instanceof JMenu) {
					fileMenu.add((JMenu) ((JComponentExtensionPoint) fileMenuItem).getJComponent());
				}
			}
		}
		JMenuItem exitMenuItem = new JMenuItem();
		exitMenuItem.setIcon(new ImageIcon(getClass().getResource("/youtubeuploader/resources/images/delete.png")));
		exitMenuItem.setText(resourceBundle.getString("exitMenuItem.text"));
		exitMenuItem.setName("exitMenuItem");
		exitMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				System.exit(0);
			}
		});
		fileMenu.add(exitMenuItem);
		menuBar.add(fileMenu);

		JMenu editMenu = new JMenu("Bearbeiten");

		for (ExtensionPoint editMenuItem : pluginService.getExtensions("edit_menu")) {
			if (editMenuItem instanceof JComponentExtensionPoint) {
				if (((JComponentExtensionPoint) editMenuItem).getJComponent() instanceof JMenuItem) {
					editMenu.add((JMenuItem) ((JComponentExtensionPoint) editMenuItem).getJComponent());
				} else if (((JComponentExtensionPoint) editMenuItem).getJComponent() instanceof JMenu) {
					editMenu.add((JMenu) ((JComponentExtensionPoint) editMenuItem).getJComponent());
				}
			}
		}
		menuBar.add(editMenu);

		JMenuItem settingsMenuItem = new JMenuItem("Einstellungen", new ImageIcon(getClass().getResource("/youtubeuploader/resources/images/pencil.png")));
		settingsMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{

			}
		});
		settingsMenuItem.setEnabled(false);
		JMenuItem wikiMenuItem = new JMenuItem("Wiki");
		wikiMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().browse(new URI(resourceBundle.getString("wikiURI")));
					} catch (IOException e1) {
						e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					} catch (URISyntaxException e1) {
						e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					}
				}
			}
		});
		JMenuItem changelogMenuItem = new JMenuItem("Changelog");
		changelogMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().browse(new URI(resourceBundle.getString("changeLog")));
					} catch (IOException e1) {
						e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					} catch (URISyntaxException e1) {
						e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					}
				}
			}
		});
		JMenuItem aboutMenuItem = new JMenuItem("Über", new ImageIcon(getClass().getResource("/youtubeuploader/resources/images/application_home.png")));
		aboutMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				AboutDialog aboutDialog = new AboutDialog();
				aboutDialog.setTitle(resourceBundle.getString("application.title"));
				aboutDialog.pack();
				aboutDialog.setVisible(true);
			}
		});

		JMenu helpMenu = new JMenu("Hilfe");
		helpMenu.add(settingsMenuItem);
		helpMenu.add(wikiMenuItem);
		helpMenu.add(changelogMenuItem);
		helpMenu.add(aboutMenuItem);
		menuBar.add(helpMenu);
	}

	private void initComponents()
	{

		menuBar = new JMenuBar();
		menuBar.setMinimumSize(new Dimension(111, 21));
		menuBar.setName(MENU_BAR);

		mainFrame.setPreferredSize(new Dimension(1200, 600));
		mainFrame.setMinimumSize(new Dimension(1200, 600));
		mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				boolean flag = true;
				for (ExtensionPoint element : pluginService.getExtensions("exit")) {
					ExitExtensionPoint exitExtensionPoint = (ExitExtensionPoint) element;
					flag = exitExtensionPoint.canExit();
					if (!flag) {
						break;
					}
				}
				if (flag) {
					pluginManager.endPlugins();
					System.exit(0);
				}
			}
		});
		if (Computer.isMac()) {
			InputMap im = (InputMap) UIManager.get("TextField.focusInputMap");
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
		}
	}
}

