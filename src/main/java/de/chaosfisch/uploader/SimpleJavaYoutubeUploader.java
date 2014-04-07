/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.uploader;

import de.chaosfisch.uploader.gui.GUI;
import de.chaosfisch.util.Directories;
import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public final class SimpleJavaYoutubeUploader {

	private static final int    LAUNCHER_UPDATE_DELAY = 5000;
	private static final Logger LOGGER                = LoggerFactory.getLogger(SimpleJavaYoutubeUploader.class);

	private SimpleJavaYoutubeUploader() {
	}

	public static void main(final String[] args) {
		logVMInfo();
		updateLauncher();
		Application.launch(GUI.class, args);
	}

	private static void updateLauncher() {
		final Path launcherUpdatePath = Paths.get("").toAbsolutePath().resolve(ApplicationData.VERSION).resolve("launcher/");
		LOGGER.info("Checking for new launcher version at {}", launcherUpdatePath.toString());
		if (Files.exists(launcherUpdatePath)) {
			updateLauncher(launcherUpdatePath);
		}
	}

	private static void updateLauncher(final Path launcherUpdatePath) {
		LOGGER.info("Launcher update exists");
		final Thread thread = new Thread(() -> {
			try {
				Thread.sleep(LAUNCHER_UPDATE_DELAY);
				LOGGER.info("Copying launcher");
				Directories.copyDirectory(launcherUpdatePath, Paths.get("").toAbsolutePath());
				LOGGER.info("Deleting existing launcher update");
				Directories.delete(launcherUpdatePath);
			} catch (InterruptedException | IOException e) {
				LOGGER.error("Exception in updating launcher", e);
			}
		}, "Launcher-Update-Thread");
		thread.setDaemon(true);
		thread.start();
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
		LOGGER.info("# Date:      " + LocalDateTime.now().toString());
		LOGGER.info("# Data dir:  " + ApplicationData.DATA_DIR);
		LOGGER.info("# Version:   " + ApplicationData.VERSION);
		LOGGER.info("####################################################################");
	}
}
