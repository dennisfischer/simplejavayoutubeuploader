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

import com.sun.javafx.PlatformUtil;

public final class ApplicationData {
	public static final String VERSION = "3.1.0.10";
	public static final int    RELEASE = 22;
	public static final String BASEURL = "http://dev.chaosfisch.com/nightly/jupidator/update.xml";
	public static final String HOME;
	public static final String DATA_DIR;

	static {
		String userHome = System.getProperty("user.home");
		if (PlatformUtil.isMac()) {
			userHome += "/Library/Application Support/";
		}
		System.setProperty("user.home", userHome);
		HOME = System.getProperty("user.home") + '/';
		DATA_DIR = HOME + "/SimpleJavaYoutubeUploader";
	}

	private ApplicationData() {
	}
}
