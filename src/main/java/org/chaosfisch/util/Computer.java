/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.util;

import com.google.inject.Inject;
import com.sun.javafx.PlatformUtil;
import org.chaosfisch.slf4j.Log;
import org.slf4j.Logger;

import java.io.IOException;

public final class Computer {

	private final Runtime runtime;
	@Log
	private       Logger  logger;

	@Inject
	public Computer() {
		runtime = Runtime.getRuntime();
	}

	/** Sends this system to hibernation mode */
	public void hibernateComputer() {
		String command = "";
		if (PlatformUtil.isWindows()) {
			command = "rundll32 powrprof.dll,SetSuspendState";
		} else if (PlatformUtil.isLinux()) {
			command = "pm-hibernate";
		} else if (PlatformUtil.isMac()) {
			command = "osascript -e 'tell application \"Finder\" to sleep'";
		}

		try {
			runtime.exec(command);
		} catch (final IOException e) {
			logger.error(e.getMessage(), e);
		}
		System.exit(0);
	}

	/** Sends this system to shutdown mode */
	public void shutdownComputer() {
		String command = "";
		if (PlatformUtil.isWindows()) {
			command = "shutdown -t 60 -s -f";
		} else if (PlatformUtil.isLinux()) {
			command = "shutdown -t 60 -h -f";
		} else if (PlatformUtil.isMac()) {
			command = "osascript -e 'tell application\"Finder\" to shut down'";
		}

		try {
			runtime.exec(command);
		} catch (final IOException e) {
			logger.error(e.getMessage(), e);
		}
		System.exit(0);
	}

	public void customCommand(final String command) {
		try {
			runtime.exec(command);
		} catch (final IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
