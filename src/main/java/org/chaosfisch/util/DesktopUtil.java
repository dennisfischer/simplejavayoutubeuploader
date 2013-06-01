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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;

public final class DesktopUtil {

	static final Logger logger = LoggerFactory.getLogger(DesktopUtil.class);

	public static boolean openBrowser(final String url) {
		final Desktop d = Desktop.getDesktop();
		if (!d.isSupported(Desktop.Action.BROWSE)) {
			return false;
		}
		try {
			final URI address = URI.create(url);
			d.browse(address);
			return true;
		} catch (IOException e) {
			logger.error("Browser IOException at {}", url, e);
			return false;
		}
	}

	public static boolean openDirectory(final String dirString) {
		final File dir = new File(dirString);

		if (!dir.isDirectory()) {
			return false;
		}

		final Desktop d = Desktop.getDesktop();
		if (!d.isSupported(Desktop.Action.OPEN)) {
			return false;
		}
		try {
			d.open(dir);
			return true;
		} catch (IOException e) {
			logger.error("Directory IOException at {}", dir.getAbsolutePath(), e);
			return false;
		}
	}
}
