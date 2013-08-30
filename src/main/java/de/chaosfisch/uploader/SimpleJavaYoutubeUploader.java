/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;

public final class SimpleJavaYoutubeUploader {

	private SimpleJavaYoutubeUploader() {
	}

	public static void main(final String[] args) {
		loadVMOptions();
		initApplication(args);
	}

	private static void initApplication(final String[] args) {
		final Preferences prefs = Preferences.userNodeForPackage(SimpleJavaYoutubeUploader.class);

		if (6 >= prefs.getInt("version", 0)) {
			DBConverter.main(args);

			prefs.putInt("version", ApplicationData.RELEASE);
		}
		GuiUploader.initialize(args);
	}

	private static void loadVMOptions() {

		final File file = new File("SimpleJavaYoutubeUploader.vmoptions");
		if (!file.exists()) {
			return;
		}
		try {
			final Properties custom = new Properties();
			custom.load(Files.newReader(file, Charsets.UTF_8));
			for (final Map.Entry<Object, Object> entry : custom.entrySet()) {
				if (!Strings.isNullOrEmpty(entry.getValue().toString())) {
					System.setProperty(entry.getKey().toString(), entry.getValue().toString());
				}
			}
		} catch (Exception e) {
			final Logger logger = LoggerFactory.getLogger(SimpleJavaYoutubeUploader.class);
			logger.warn("VMOptions ignored", e);
		}
	}
}
