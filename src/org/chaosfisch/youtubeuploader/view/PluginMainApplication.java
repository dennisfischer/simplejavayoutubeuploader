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

import asg.cliche.CLIException;
import asg.cliche.Shell;
import asg.cliche.ShellFactory;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import org.apache.log4j.Logger;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.plugin.ExtensionPoints.ExitExtensionPoint;
import org.chaosfisch.plugin.ExtensionPoints.ExtensionPoint;
import org.chaosfisch.plugin.ExtensionPoints.JComponentExtensionPoint;
import org.chaosfisch.plugin.Pluggable;
import org.chaosfisch.plugin.PluginManager;
import org.chaosfisch.plugin.PluginService;
import org.chaosfisch.util.Computer;
import org.chaosfisch.youtubeuploader.designmanager.DesignManager;
import org.chaosfisch.youtubeuploader.services.settingsservice.spi.SettingsService;
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
import java.util.Collection;
import java.util.ResourceBundle;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 29.12.11
 * Time: 16:08
 * To change this template use File | Settings | File Templates.
 */
public class PluginMainApplication
{

	private static final String   MENU_BAR         = "menuBar"; //NON-NLS
	private static final String[] DISABLED_PLUGINS = new String[0];

	private                                              JTabbedPane     tabbedPane;
	private                                              JMenuBar        menuBar;
	@Inject(optional = true) @Named("mainFrame") private JFrame          mainFrame;
	@Inject private                                      PluginManager   pluginManager;
	@Inject private                                      PluginService   pluginService;
	@Inject private                                      PluginLoader    pluginLoader;
	@Inject private                                      DesignManager   designManager;
	@Inject private                                      SettingsService settingsService;
	@Inject private                                      Injector        injector;
	private final ResourceBundle resourceBundle = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.resources.application"); //NON-NLS
	@InjectLogger private Logger logger;

	public PluginMainApplication()
	{
		AnnotationProcessor.process(this);
	}

	public void run(final String... args)
	{
		initComponents();
		initPlugins();
		showFrame();
		initCommandline(args);
	}

	private void initCommandline(final String... args)
	{
		if (GraphicsEnvironment.isHeadless()) {
			try {
				final Shell shell = ShellFactory.createConsoleShell("sjy-uploader", "Simple Java Youtube Uploader by CHAOSFISCH: CLI Interface", this);
				for (final Pluggable plugin : pluginManager.getPlugins().values()) {
					shell.addMainHandler(plugin, String.format("%s-", plugin.getCLIName()));
				}
				for (final String line : args) {
					shell.processLine(line);
				}
				shell.commandLoop(); // and three.
			} catch (IOException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			} catch (CLIException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
		}
	}

	private void showFrame()
	{
		if (GraphicsEnvironment.isHeadless()) {
			return;
		}
		mainFrame.setTitle(resourceBundle.getString("application.title"));
		mainFrame.setJMenuBar(menuBar);
		final Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/youtubeuploader/resources/images/film.png"));//NON-NLS
		mainFrame.setIconImage(image); //NON-NLS
		mainFrame.add(tabbedPane);

		// Get the size of the screen
		final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

		// Move the window
		mainFrame.setLocation((dim.width - mainFrame.getSize().width) / 2, (dim.height - mainFrame.getSize().height) / 2);
		mainFrame.pack();
		mainFrame.setVisible(true);
	}

	private void initPlugins()
	{
		final Collection<Pluggable> pluggableList = pluginManager.loadPlugins(PluginMainApplication.DISABLED_PLUGINS); //NON-NLS
		for (final Pluggable p : pluggableList) {
			p.init();
		}

		logger.debug("Start Plugins"); //NON-NLS
		pluginManager.startPlugins();
		logger.debug("Process Extensionpoints"); //NON-NLS
		if (!GraphicsEnvironment.isHeadless()) {

			for (final ExtensionPoint tabs : pluginService.getExtensions("panel_tabs")) { //NON-NLS
				logger.debug("Extension point"); //NON-NLS
				if ((tabs instanceof JComponentExtensionPoint) && (((JComponentExtensionPoint) tabs).getJComponent() instanceof JPanel)) {
					logger.debug(String.format("Adding panel_tab: %s", ((JComponentExtensionPoint) tabs).getTitle())); //NON-NLS
					tabbedPane.addTab(((JComponentExtensionPoint) tabs).getTitle(), ((JComponentExtensionPoint) tabs).getJComponent());
				}
			}

			final JMenu fileMenu = new JMenu(resourceBundle.getString("application.fileLabel"));

			for (final ExtensionPoint fileMenuItem : pluginService.getExtensions("file_menu")) { //NON-NLS
				if (fileMenuItem instanceof JComponentExtensionPoint) {
					if (((JComponentExtensionPoint) fileMenuItem).getJComponent() instanceof JMenuItem) {
						fileMenu.add((JMenuItem) ((JComponentExtensionPoint) fileMenuItem).getJComponent());
					} else if (((JComponentExtensionPoint) fileMenuItem).getJComponent() instanceof JMenu) {
						fileMenu.add((JMenuItem) ((JComponentExtensionPoint) fileMenuItem).getJComponent());
					}
				}
			}
			final JMenuItem exitMenuItem = new JMenuItem();
			exitMenuItem.setIcon(new ImageIcon(getClass().getResource("/youtubeuploader/resources/images/delete.png"))); //NON-NLS
			exitMenuItem.setText(resourceBundle.getString("exitMenuItem.text"));
			exitMenuItem.setName("exitMenuItem"); //NON-NLS
			exitMenuItem.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					final Window w = mainFrame;
					w.getToolkit().getSystemEventQueue().postEvent(new WindowEvent(w, WindowEvent.WINDOW_CLOSING));
				}
			});
			fileMenu.add(exitMenuItem);
			menuBar.add(fileMenu);

			final JMenu editMenu = new JMenu(resourceBundle.getString("application.editLabel"));

			for (final ExtensionPoint editMenuItem : pluginService.getExtensions("edit_menu")) { //NON-NLS
				if (editMenuItem instanceof JComponentExtensionPoint) {
					if (((JComponentExtensionPoint) editMenuItem).getJComponent() instanceof JMenuItem) {
						editMenu.add((JMenuItem) ((JComponentExtensionPoint) editMenuItem).getJComponent());
					} else if (((JComponentExtensionPoint) editMenuItem).getJComponent() instanceof JMenu) {
						editMenu.add((JMenuItem) ((JComponentExtensionPoint) editMenuItem).getJComponent());
					}
				}
			}
			if (editMenu.getItemCount() > 0) {
				menuBar.add(editMenu);
			}

			final JMenuItem wikiMenuItem = new JMenuItem(resourceBundle.getString("application.wikiLabel"));
			wikiMenuItem.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					if (Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().browse(new URI(resourceBundle.getString("wikiURI")));
						} catch (IOException e1) {
							e1.printStackTrace();
						} catch (URISyntaxException e1) {
							e1.printStackTrace();
						}
					}
				}
			});
			final JMenuItem changelogMenuItem = new JMenuItem(resourceBundle.getString("application.changelogLabel"));
			changelogMenuItem.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					if (Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().browse(new URI(resourceBundle.getString("changeLog")));
						} catch (IOException e1) {
							e1.printStackTrace();
						} catch (URISyntaxException e1) {
							e1.printStackTrace();
						}
					}
				}
			});
			final JMenuItem aboutMenuItem = new JMenuItem(resourceBundle.getString("application.aboutLabel"), new ImageIcon(getClass().getResource("/youtubeuploader/resources/images/application_home.png"))); //NON-NLS
			aboutMenuItem.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					final AboutDialog aboutDialog = new AboutDialog();
					aboutDialog.setTitle(resourceBundle.getString("application.title"));
					aboutDialog.pack();
					aboutDialog.setLocationRelativeTo(mainFrame);
					aboutDialog.setVisible(true);
				}
			});

			final JMenuItem pluginMenuItem = new JMenuItem(resourceBundle.getString("application.pluginsLabel"));
			pluginMenuItem.addActionListener(new ActionListener()
			{
				@Override public void actionPerformed(final ActionEvent e)
				{
					final PluginViewPanel pluginViewPanel = injector.getInstance(PluginViewPanel.class);
					pluginViewPanel.run();
					pluginViewPanel.setPluggableList(pluggableList);
					pluginViewPanel.setTitle(resourceBundle.getString("pluginDialogTitle")); //NON-NLS
					pluginViewPanel.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/youtubeuploader/resources/images/film.png"))); //NON-NLS
					pluginViewPanel.pack();
					pluginViewPanel.setLocationRelativeTo(mainFrame);
					pluginViewPanel.setVisible(true);
				}
			});
			pluginMenuItem.setEnabled(true);

			final JMenu helpMenu = new JMenu(resourceBundle.getString("application.helpLabel"));
			helpMenu.add(wikiMenuItem);
			helpMenu.add(changelogMenuItem);
			helpMenu.add(aboutMenuItem);
			helpMenu.add(pluginMenuItem);
			menuBar.add(helpMenu);
		}
	}

	private void initComponents()
	{
		if (GraphicsEnvironment.isHeadless()) {
			return;
		}
		menuBar = new JMenuBar();
		menuBar.setMinimumSize(new Dimension(111, 21));
		menuBar.setName(PluginMainApplication.MENU_BAR);

		mainFrame.setPreferredSize(new Dimension(1050, 575));
		mainFrame.setMinimumSize(new Dimension(1050, 575));
		mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(final WindowEvent e)
			{
				boolean flag = true;
				for (final ExtensionPoint element : pluginService.getExtensions("exit")) { //NON-NLS
					final ExitExtensionPoint exitExtensionPoint = (ExitExtensionPoint) element;
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
			final InputMap im = (InputMap) UIManager.get("TextField.focusInputMap"); //NON-NLS
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
		}
		injector.getInstance(JFileChooser.class).setCurrentDirectory(new File(System.getProperty("user.home")));
	}

	@EventTopicSubscriber(topic = DesignManager.UPDATE_UI)
	public void onDesignChanged(final String topic, final String o)
	{
		if (GraphicsEnvironment.isHeadless()) {
			return;
		}
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override public void run()
			{
				try {
					SwingUtilities.updateComponentTreeUI(mainFrame);
				} catch (Exception ignored) {
					throw new RuntimeException("This shouldn't happen");
				}
			}
		});
	}
}

