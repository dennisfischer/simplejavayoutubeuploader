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
import java.util.Date;
import java.util.Map;
import java.util.Properties;

@SuppressWarnings("WeakerAccess")
public final class SimpleJavaYoutubeUploader {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleJavaYoutubeUploader.class);

	private SimpleJavaYoutubeUploader() {
	}

	public static void main(final String[] args) {
		loadVMOptions();
		logVMInfo();
		new ApplicationUpdater();
		GuiUploader.initialize(args);
	}

	private static void logVMInfo() {
		LOGGER.info("############################# VM INFO #############################");
		LOGGER.info("# OS Name:   " + System.getProperty("os.name"));
		LOGGER.info("# OS Arch:   " + System.getProperty("os.arch"));
		LOGGER.info("# OS Vers:   " + System.getProperty("os.version"));
		LOGGER.info("# Java Vers: " + System.getProperty("java.version"));
		LOGGER.info("# Java Home: " + System.getProperty("java.home"));
		LOGGER.info("# User Name: " + System.getProperty("user.name"));
		LOGGER.info("# User Home: " + System.getProperty("user.home"));
		LOGGER.info("# Cur dir:   " + System.getProperty("user.dir"));
		LOGGER.info("# Date:      " + new Date().toString());
		LOGGER.info("# Data dir:  " + ApplicationData.DATA_DIR);
		LOGGER.info("# Version:   " + ApplicationData.VERSION);
		LOGGER.info("####################################################################");
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
