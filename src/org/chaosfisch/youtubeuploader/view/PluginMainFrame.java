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

package org.chaosfisch.youtubeuploader.view;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import org.apache.log4j.Logger;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.chaosfisch.plugin.ExtensionPoints.ExitExtensionPoint;
import org.chaosfisch.plugin.ExtensionPoints.ExtensionPoint;
import org.chaosfisch.plugin.ExtensionPoints.JComponentExtensionPoint;
import org.chaosfisch.plugin.Pluggable;
import org.chaosfisch.plugin.PluginManager;
import org.chaosfisch.plugin.PluginService;
import org.chaosfisch.util.Computer;
import org.chaosfisch.youtubeuploader.designmanager.DesignManager;
import org.chaosfisch.youtubeuploader.services.settingsservice.SettingsService;
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
public class PluginMainFrame
{

	private static final String MENU_BAR = "menuBar"; //NON-NLS

	private                             JTabbedPane     tabbedPane;
	private                             JMenuBar        menuBar;
	@Inject @Named("mainFrame") private JFrame          mainFrame;
	@Inject private                     PluginManager   pluginManager;
	@Inject private                     PluginService   pluginService;
	@Inject private                     PluginLoader    pluginLoader;
	@Inject private                     DesignManager   designManager;
	@Inject private                     SettingsService settingsService;
	@Inject private                     Injector        injector;
	private final ResourceBundle resourceBundle = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.resources.application"); //NON-NLS
	@InjectLogger private Logger logger;

	public PluginMainFrame()
	{
		AnnotationProcessor.process(this);
	}

	public void run()
	{
		this.initComponents();
		this.initPlugins();
		this.showFrame();
	}

	private void showFrame()
	{
		this.mainFrame.setTitle(this.resourceBundle.getString("application.title"));
		this.mainFrame.setJMenuBar(this.menuBar);
		this.mainFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/youtubeuploader/resources/images/film.png"))); //NON-NLS
		this.mainFrame.add(this.tabbedPane);
		this.mainFrame.setVisible(true);
	}

	private void initPlugins()
	{
		final List<Pluggable> pluggableList = this.pluginManager.loadPlugins(new File("plugins/")); //NON-NLS
		for (final Pluggable p : pluggableList) {
			p.init();
		}

		this.logger.debug("Start Plugins"); //NON-NLS
		this.pluginManager.startPlugins();
		this.logger.debug("Process Extensionpoints"); //NON-NLS
		for (final ExtensionPoint tabs : this.pluginService.getExtensions("panel_tabs")) { //NON-NLS
			this.logger.debug("Extension point"); //NON-NLS
			if (tabs instanceof JComponentExtensionPoint && ((JComponentExtensionPoint) tabs).getJComponent() instanceof JPanel) {
				this.logger.debug(String.format("Adding panel_tab: %s", ((JComponentExtensionPoint) tabs).getTitle())); //NON-NLS
				this.tabbedPane.addTab(((JComponentExtensionPoint) tabs).getTitle(), ((JComponentExtensionPoint) tabs).getJComponent());
			}
		}

		final JMenu fileMenu = new JMenu(this.resourceBundle.getString("application.fileLabel"));

		for (final ExtensionPoint fileMenuItem : this.pluginService.getExtensions("file_menu")) { //NON-NLS
			if (fileMenuItem instanceof JComponentExtensionPoint) {
				if (((JComponentExtensionPoint) fileMenuItem).getJComponent() instanceof JMenuItem) {
					fileMenu.add((JMenuItem) ((JComponentExtensionPoint) fileMenuItem).getJComponent());
				} else if (((JComponentExtensionPoint) fileMenuItem).getJComponent() instanceof JMenu) {
					fileMenu.add((JMenu) ((JComponentExtensionPoint) fileMenuItem).getJComponent());
				}
			}
		}
		final JMenuItem exitMenuItem = new JMenuItem();
		exitMenuItem.setIcon(new ImageIcon(this.getClass().getResource("/youtubeuploader/resources/images/delete.png"))); //NON-NLS
		exitMenuItem.setText(this.resourceBundle.getString("exitMenuItem.text"));
		exitMenuItem.setName("exitMenuItem"); //NON-NLS
		exitMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				PluginMainFrame.this.mainFrame.dispose();
			}
		});
		fileMenu.add(exitMenuItem);
		this.menuBar.add(fileMenu);

		final JMenu editMenu = new JMenu(this.resourceBundle.getString("application.editLabel"));

		for (final ExtensionPoint editMenuItem : this.pluginService.getExtensions("edit_menu")) { //NON-NLS
			if (editMenuItem instanceof JComponentExtensionPoint) {
				if (((JComponentExtensionPoint) editMenuItem).getJComponent() instanceof JMenuItem) {
					editMenu.add((JMenuItem) ((JComponentExtensionPoint) editMenuItem).getJComponent());
				} else if (((JComponentExtensionPoint) editMenuItem).getJComponent() instanceof JMenu) {
					editMenu.add((JMenu) ((JComponentExtensionPoint) editMenuItem).getJComponent());
				}
			}
		}
		this.menuBar.add(editMenu);

		final JMenuItem wikiMenuItem = new JMenuItem(this.resourceBundle.getString("application.wikiLabel"));
		wikiMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().browse(new URI(PluginMainFrame.this.resourceBundle.getString("wikiURI")));
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (URISyntaxException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		final JMenuItem changelogMenuItem = new JMenuItem(this.resourceBundle.getString("application.changelogLabel"));
		changelogMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().browse(new URI(PluginMainFrame.this.resourceBundle.getString("changeLog")));
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (URISyntaxException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		final JMenuItem aboutMenuItem = new JMenuItem(this.resourceBundle.getString("application.aboutLabel"),
				new ImageIcon(this.getClass().getResource("/youtubeuploader/resources/images/application_home.png"))); //NON-NLS
		aboutMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final AboutDialog aboutDialog = new AboutDialog();
				aboutDialog.setTitle(PluginMainFrame.this.resourceBundle.getString("application.title"));
				aboutDialog.pack();
				aboutDialog.setVisible(true);
			}
		});

		final JMenuItem pluginMenuItem = new JMenuItem(this.resourceBundle.getString("application.pluginsLabel"));
		pluginMenuItem.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				final PluginViewPanel pluginViewPanel = PluginMainFrame.this.injector.getInstance(PluginViewPanel.class);
				pluginViewPanel.setPluggableList(pluggableList);
				pluginViewPanel.run();
				pluginViewPanel.setTitle(PluginMainFrame.this.resourceBundle.getString("pluginDialogTitle")); //NON-NLS
				pluginViewPanel.setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/youtubeuploader/resources/images/film.png"))); //NON-NLS
				pluginViewPanel.pack();
				pluginViewPanel.setVisible(true);
			}
		});
		pluginMenuItem.setEnabled(true);

		final JMenu helpMenu = new JMenu(this.resourceBundle.getString("application.helpLabel"));
		helpMenu.add(wikiMenuItem);
		helpMenu.add(changelogMenuItem);
		helpMenu.add(aboutMenuItem);
		helpMenu.add(pluginMenuItem);
		this.menuBar.add(helpMenu);
	}

	private void initComponents()
	{
		this.menuBar = new JMenuBar();
		this.menuBar.setMinimumSize(new Dimension(111, 21));
		this.menuBar.setName(MENU_BAR);

		this.mainFrame.setPreferredSize(new Dimension(1000, 600));
		this.mainFrame.setMinimumSize(new Dimension(1000, 500));
		this.mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.mainFrame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(final WindowEvent e)
			{
				boolean flag = true;
				for (final ExtensionPoint element : PluginMainFrame.this.pluginService.getExtensions("exit")) { //NON-NLS
					final ExitExtensionPoint exitExtensionPoint = (ExitExtensionPoint) element;
					flag = exitExtensionPoint.canExit();
					if (!flag) {
						break;
					}
				}
				if (flag) {
					PluginMainFrame.this.pluginManager.endPlugins();
					System.exit(0);
				}
			}
		});
		if (Computer.isMac()) {
			final InputMap im = (InputMap) UIManager.get("TextField.focusInputMap"); //NON-NLS
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
		}
		this.injector.getInstance(JFileChooser.class).setCurrentDirectory(new File(System.getProperty("user.home")));
	}
}

