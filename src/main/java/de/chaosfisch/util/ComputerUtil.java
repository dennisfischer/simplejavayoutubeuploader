/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.util;

import com.sun.javafx.PlatformUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class ComputerUtil {

	private static final Logger LOGGER            = LoggerFactory.getLogger(ComputerUtil.class);
	private static final int    DEFAULT_WAIT_TIME = 30000;

	/**
	 * Sends this system to hibernation mode
	 */
	public void hibernateComputer() {
		final String command;
		if (PlatformUtil.isWindows()) {
			command = "rundll32 powrprof.dll,SetSuspendState";
		} else if (PlatformUtil.isLinux()) {
			command = "pm-hibernate";
		} else if (PlatformUtil.isMac()) {
			command = "osascript -e 'tell application \"Finder\" to sleep'";
		} else {
			return;
		}

		execute(command);
	}

	private void execute(final String command) {
		try {
			Runtime.getRuntime().exec(command);
		} catch (final IOException e) {
			LOGGER.error(e.getMessage(), e);
		}
		final Thread thread = new Thread(() -> {
			try {
				Thread.sleep(DEFAULT_WAIT_TIME);
			} catch (final InterruptedException ignored) {
			}
			System.exit(0);
		}, "Exitmanager");
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Sends this system to shutdown mode
	 */
	public void shutdownComputer() {
		final String command;
		if (PlatformUtil.isWindows()) {
			command = "shutdown -t 60 -s -f";
		} else if (PlatformUtil.isLinux()) {
			command = "shutdown -t 60 -h -f";
		} else if (PlatformUtil.isMac()) {
			command = "osascript -e 'tell application\"Finder\" to shut down'";
		} else {
			return;
		}

		execute(command);
	}

	public void customCommand(final String command) {
		execute(command);
	}
}
