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

package org.chaosfisch.youtubeuploader.designmanager;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.log4j.Logger;
import org.chaosfisch.youtubeuploader.util.logger.InjectLogger;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 29.02.12
 * Time: 16:41
 * To change this template use File | Settings | File Templates.
 */
public class DesignManager
{
	private final Map<String, Design> designMap = new HashMap<String, Design>();
	@SuppressWarnings("DuplicateStringLiteralInspection") @Inject @Named(value = "mainFrame") private JFrame mainFrame;
	@InjectLogger private                                                                             Logger logger;

	public DesignManager()
	{
	}

	public void run()
	{
		this.logger.debug("Loading designMaps"); //NON-NLS
		final ServiceLoader<DesignMap> designServiceLoader = ServiceLoader.load(DesignMap.class);
		this.logger.debug("Parsing designMaps"); //NON-NLS
		for (final DesignMap mapList : designServiceLoader) {
			this.logger.debug("Parsing designs of designMap"); //NON-NLS
			for (final Design design : mapList) {
				this.logger.debug("Design found"); //NON-NLS
				if (design.getShortName() != null && design.getName() != null && this.classExists(design.getLaF())) {
					//noinspection StringConcatenation
					this.logger.debug("Adding Design " + design.getShortName()); //NON-NLS
					this.designMap.put(design.getShortName(), design);
				}
			}
		}
		this.crossPlatformDesign();
	}

	private boolean classExists(final String className)
	{
		try {
			Class.forName(className);
			return true;
		} catch (ClassNotFoundException exception) {
			return false;
		}
	}

	public void changeDesign(final String design)
	{
		//noinspection StringConcatenation
		this.logger.debug("Changing design to " + design); //NON-NLS
		if (!this.designMap.containsKey(design)) {
			//noinspection StringConcatenation
			this.logger.debug("Design not found: " + design); //NON-NLS
			return;
		}

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override public void run()
			{
				try {
					UIManager.setLookAndFeel(DesignManager.this.designMap.get(design).getLaF());
				} catch (UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				} catch (InstantiationException e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				} catch (IllegalAccessException e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				}

				if (!(DesignManager.this.mainFrame == null)) {

					SwingUtilities.updateComponentTreeUI(DesignManager.this.mainFrame);
					DesignManager.this.mainFrame.pack();
				}
			}
		});
	}

	void crossPlatformDesign()
	{
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (InstantiationException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (IllegalAccessException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}
}
