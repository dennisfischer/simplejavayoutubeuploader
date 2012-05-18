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

/*
 * Main.java
 *
 * Created on January 10, 2007, 1:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.chaosfisch.youtubeuploader;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xbean.finder.ResourceFinder;
import org.chaosfisch.youtubeuploader.designmanager.DesignManager;
import org.chaosfisch.youtubeuploader.services.settingsservice.spi.SettingsService;
import org.chaosfisch.youtubeuploader.view.PluginMainFrame;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The main class of the application.
 */
public class SimpleJavaYoutubeUploader
{
	public SimpleJavaYoutubeUploader(final String[] args)
	{
		PropertyConfigurator.configure(this.getClass().getResource("/META-INF/log4j.properties")); //NON-NLS
		final ResourceFinder finder = new ResourceFinder("META-INF/services/");
		try {
			final List<Class> classes = finder.findAllImplementations(Module.class);

			final ArrayList<Module> modules = new ArrayList<Module>(classes.size());
			for (final Class clazz : classes) {
				modules.add((Module) clazz.newInstance());
			}

			final Injector injector = Guice.createInjector(modules);
			modules.clear();

			final SettingsService settingsService = injector.getInstance(SettingsService.class);
			final DesignManager designManager = injector.getInstance(DesignManager.class);
			designManager.run();
			designManager.changeDesign((String) settingsService.get("application.general.laf", "Substance Graphite Glass"));//NON-NLS
			JFrame.setDefaultLookAndFeelDecorated(true);

			SwingUtilities.invokeLater(new Runnable()
			{
				@Override public void run()
				{
					designManager.registerSettingsExtension();
					final PluginMainFrame pluginMainFrame = injector.getInstance(PluginMainFrame.class);
					pluginMainFrame.run();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (ClassNotFoundException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (InstantiationException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (IllegalAccessException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(final String[] args)
	{
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
		{
			@Override public void uncaughtException(final Thread t, final Throwable e)
			{
				System.out.println(e.getMessage());
				final Logger logger = Logger.getLogger("UNCAUGHT EXCEPTION LOGGER"); //NON-NLS
				logger.warn(e.getMessage(), e);
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override public void run()
					{
						JOptionPane.showMessageDialog(null, e.getMessage(), "Error:", JOptionPane.ERROR_MESSAGE); //NON-NLS
					}
				});
			}
		});
		new SimpleJavaYoutubeUploader(args);
	}
}
