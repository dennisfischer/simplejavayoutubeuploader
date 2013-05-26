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

import org.chaosfisch.youtubeuploader.guice.GuiceBindings;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;
import java.util.prefs.Preferences;

public final class SimpleJavaYoutubeUploader {

	public static void main(final String[] args) {

		initLocale();
		initUpdater();
		final Preferences prefs = Preferences.userNodeForPackage(SimpleJavaYoutubeUploader.class);

		if (prefs.getInt("version", 0) <= 6) {
			try {
				DBConverter.main(args);
			} catch (SQLException | IOException e) {
				e.printStackTrace();
			}

			prefs.putInt("version", ApplicationData.RELEASE);
		}
		GuiUploader.initialize(args, new GuiceBindings("youtubeuploader-v3"));

	}

	private static void initUpdater() {
		new ApplicationUpdater();
	}

	private static void initLocale() {
		final Locale[] availableLocales = {Locale.GERMANY, Locale.GERMAN, Locale.ENGLISH, Locale.ITALY, Locale.ITALIAN};
		if (!Arrays.asList(availableLocales).contains(Locale.getDefault())) {
			Locale.setDefault(Locale.ENGLISH);
		}
	}
}
