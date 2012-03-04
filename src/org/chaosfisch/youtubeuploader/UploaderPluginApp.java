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
import org.apache.log4j.PropertyConfigurator;
import org.chaosfisch.plugin.JARFileFilter;
import org.chaosfisch.youtubeuploader.designmanager.DesignManager;
import org.chaosfisch.youtubeuploader.util.ClasspathLoader;
import org.chaosfisch.youtubeuploader.view.PluginMainFrame;

import javax.swing.*;
import java.io.File;
import java.util.ServiceLoader;

/**
 * The main class of the application.
 */
@SuppressWarnings("ALL")
public class UploaderPluginApp
{

	public UploaderPluginApp(String[] args)
	{
		ClasspathLoader.loadLibaries(new File("./").listFiles(new JARFileFilter()));
		ClasspathLoader.loadLibaries(new File("./libs/").listFiles(new JARFileFilter()));
		ClasspathLoader.loadLibaries(new File("./plugins/").listFiles(new JARFileFilter()));

		PropertyConfigurator.configure(getClass().getResource("/META-INF/log4j.properties"));
		final ServiceLoader<Module> modules = ServiceLoader.load(Module.class);
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override public void run()
			{
				Injector injector = Guice.createInjector(modules);
				PluginMainFrame pluginMainFrame = injector.getInstance(PluginMainFrame.class);
				DesignManager designManager = injector.getInstance(DesignManager.class);
				designManager.run();
				designManager.changeDesign("SubstanceGraphiteGlassLookAndFeel");
				pluginMainFrame.run();
			}
		});
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args)
	{
		new UploaderPluginApp(args);
	}
}
