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
import org.chaosfisch.youtubeuploader.guice.GuiceBindings;

import java.util.Arrays;
import java.util.Locale;

public final class SimpleJavaYoutubeUploader {

    public static void main(final String[] args) {

        initLocale();
        initSavedir();
        initUpdater();

        GuiUploader.initialize(args, new GuiceBindings("youtubeuploader"));
    }

    private static void initUpdater() {
        new ApplicationUpdater();
    }

    private static void initSavedir() {
        String userHome = System.getProperty("user.home");
        if (PlatformUtil.isMac()) {
            userHome += "/Library/Application Support/";
        }
        System.setProperty("user.home", userHome);

    }

    private static void initLocale() {
        final Locale[] availableLocales = {Locale.GERMANY, Locale.GERMAN, Locale.ENGLISH};
        if (!Arrays.asList(availableLocales)
                .contains(Locale.getDefault())) {
            Locale.setDefault(Locale.ENGLISH);
        }
    }
}
