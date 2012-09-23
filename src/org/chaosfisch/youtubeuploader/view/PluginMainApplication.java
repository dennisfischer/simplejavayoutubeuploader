/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Dennis Fischer
 ******************************************************************************/

package org.chaosfisch.youtubeuploader.view;

import java.awt.event.KeyEvent;

import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;

import org.apache.log4j.Logger;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.chaosfisch.util.Computer;
import org.chaosfisch.util.logger.InjectLogger;
import org.chaosfisch.youtubeuploader.services.SettingsService;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class PluginMainApplication
{
	@Inject
	private SettingsService	settingsService;
	@Inject
	private Injector		injector;
	@InjectLogger
	private Logger			logger;

	public PluginMainApplication()
	{
		AnnotationProcessor.process(this);
	}

	public void run(final String... args)
	{
		initComponents();
		updateApplication();
	}

	private void updateApplication()
	{

		if (settingsService.get("version-20rc2.0", "null").equals("null"))
		{
			EventBus.publish("UPDATE_APPLICATION", "2.0");

			settingsService.set("version-20rc2.0", "true");
			settingsService.save();
		}

		if (settingsService.get("version-20rc2.1", "null").equals("null"))
		{
			EventBus.publish("UPDATE_APPLICATION", "2.1");

			settingsService.set("version-20rc2.1", "true");
			settingsService.save();
		}

		if (settingsService.get("version-20rc2.2", "null").equals("null"))
		{
			EventBus.publish("UPDATE_APPLICATION", "2.2");

			settingsService.set("version-20rc2.2", "true");
			settingsService.save();
		}

		if (settingsService.get("version-20rc2.3", "null").equals("null"))
		{
			EventBus.publish("UPDATE_APPLICATION", "2.3");

			settingsService.set("version-20rc2.3", "true");
			settingsService.save();
		}
	}

	private void initComponents()
	{
		if (Computer.isMac())
		{
			final InputMap im = (InputMap) UIManager.get("TextField.focusInputMap");
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
		}
	}
}
