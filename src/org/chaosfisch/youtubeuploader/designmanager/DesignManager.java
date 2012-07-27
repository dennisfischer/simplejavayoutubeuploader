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
import org.apache.log4j.Logger;
import org.apache.xbean.finder.ResourceFinder;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.youtubeuploader.designmanager.spi.Design;
import org.chaosfisch.youtubeuploader.designmanager.spi.DesignMap;
import org.chaosfisch.youtubeuploader.services.settingsservice.spi.SettingsService;
import org.chaosfisch.youtubeuploader.util.logger.InjectLogger;

import javax.swing.*;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 29.02.12
 * Time: 16:41
 * To change this template use File | Settings | File Templates.
 */
public class DesignManager
{
	private final Map<String, Design> designMap = new HashMap<String, Design>(50);
	@InjectLogger private Logger          logger;
	@Inject               SettingsService settingsService;
	public static final String UPDATE_UI = "onUpdateUI"; //NON-NLS

	public DesignManager()
	{
		AnnotationProcessor.process(this);
	}

	public void run()
	{
		logger.debug("Loading designMaps"); //NON-NLS
		final ResourceFinder finder = new ResourceFinder("META-INF/services/"); //NON-NLS
		try {
			@SuppressWarnings("rawtypes") final List<Class> classes = finder.findAllImplementations(DesignMap.class);

			logger.debug("Parsing designMaps"); //NON-NLS
			for (final Class<?> mapList : classes) {
				logger.debug("Parsing designs of designMap"); //NON-NLS
				for (final Design design : (DesignMap) mapList.newInstance()) {
					logger.debug("Design found"); //NON-NLS
					if ((design.getShortName() != null) && (design.getName() != null)) {
						//noinspection StringConcatenation
						logger.debug("Adding Design " + design.getName()); //NON-NLS
						designMap.put(design.getName(), design);
					}
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (InstantiationException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (IllegalAccessException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	private Design getDesign(final String lookAndFeel)
	{
		if (!designMap.containsKey(lookAndFeel)) {
			//noinspection StringConcatenation
			logger.debug("Design not found: " + lookAndFeel); //NON-NLS
			return null;
		}
		return designMap.get(lookAndFeel);
	}

	public void changeDesign(final String design)
	{
		//noinspection StringConcatenation
		logger.debug("Changing design to " + design); //NON-NLS
		if (!designMap.containsKey(design)) {
			//noinspection StringConcatenation
			logger.debug("Design not found: " + design); //NON-NLS
			return;
		}

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override public void run()
			{
				try {
					UIManager.setLookAndFeel(designMap.get(design).getLaF().getCanonicalName());
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

	public void registerSettingsExtension()
	{
		final MutableComboBoxModel desingListModel = new DefaultComboBoxModel();
		final List<Design> designCollection = new ArrayList<Design>(designMap.size());
		designCollection.addAll(designMap.values());
		Collections.sort(designCollection, new Comparator<Object>()
		{
			@Override public int compare(final Object o1, final Object o2)
			{
				final Design design_1 = (Design) o1;
				final Design design_2 = (Design) o2;

				return design_1.getName().compareTo(design_2.getName());
			}
		});
		for (final Design design : designCollection) {
			if ((design.getShortName() != null) && (design.getName() != null)) {
				desingListModel.addElement(design);
			}
		}
		desingListModel.setSelectedItem(getDesign((String) settingsService.get("application.general.laf", "SubstanceGraphiteGlassLookAndFeel"))); //NON-NLS
		settingsService.addCombobox("application.general.laf", "Design:", desingListModel); //NON-NLS
	}

	@EventTopicSubscriber(topic = SettingsService.SETTINGS_SAVED)
	public void onDesignChanged(final String topic, final String o)
	{
		// noinspection CallToStringEquals
		if (o.equals("application.general.laf")) {
			changeDesign((String) settingsService.get("application.general.laf", "SubstanceGraphiteGlassLookAndFeel"));
			EventBus.publish(DesignManager.UPDATE_UI, null);
		}
	}
}
