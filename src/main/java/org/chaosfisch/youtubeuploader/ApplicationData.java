/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader;

import com.sun.javafx.PlatformUtil;

public final class ApplicationData {
	public static final String DEVELOPER_KEY = "AI39si6EquMrdMz_oKMFk9rNBHqOQTUEG-kJ4I33xveO-W40U95XjJAL3-Fa9voJ3bPxkMwsT7IQKc39M3tw0o2fHswYRN0Chg";
	public static final String GDATA_VERSION = "2";
	public static final String VERSION       = "3.0.0.8";
	public static final int    RELEASE       = 7;
	public static final String BASEURL       = "http://youtubeuploader.square7.ch/nightly/jupidator/update.xml";
	public static final String HOME;
	public static final String DATA_DIR;

	static {
		String userHome = System.getProperty("user.home");
		if (PlatformUtil.isMac()) {
			userHome += "/Library/Application Support/";
		}
		System.setProperty("user.home", userHome);
		HOME = System.getProperty("user.home");
		DATA_DIR = HOME + "/SimpleJavaYoutubeUploader";
	}
}
