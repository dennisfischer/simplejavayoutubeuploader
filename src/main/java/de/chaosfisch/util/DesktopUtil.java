/*
 * Copyright (c) 2014 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.util;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;

public final class DesktopUtil {

	private static final Logger LOOGER = LoggerFactory.getLogger(DesktopUtil.class);
	private final Desktop desktop;

	@Inject
	private DesktopUtil() {
		desktop = Desktop.getDesktop();
	}

	public boolean openBrowser(final String url) {
		if (!desktop.isSupported(Desktop.Action.BROWSE)) {
			return false;
		}
		try {
			final URI address = URI.create(url);
			desktop.browse(address);
			return true;
		} catch (final IOException e) {
			LOOGER.error("Browser IOException at {}", url, e);
			return false;
		}
	}

	public boolean openDirectory(final String dirString) {
		final File dir = new File(dirString);

		if (!dir.isDirectory()) {
			return false;
		}

		if (!desktop.isSupported(Desktop.Action.OPEN)) {
			return false;
		}
		try {
			desktop.open(dir);
			return true;
		} catch (final IOException e) {
			LOOGER.error("Directory IOException at {}", dir.getAbsolutePath(), e);
			return false;
		}
	}
}
