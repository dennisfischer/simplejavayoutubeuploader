/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Locale;

import org.chaosfisch.youtubeuploader.db.generated.Tables;
import org.chaosfisch.youtubeuploader.guice.GuiceBindings;
import org.jooq.impl.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.javafx.PlatformUtil;

public class SimpleJavaYoutubeUploader {

	private static boolean	server	= false;
	private static Logger	logger	= LoggerFactory.getLogger(SimpleJavaYoutubeUploader.class);
	/**
	 * The application DI injector
	 */
	static Injector			injector;

	public static void main(String[] args) {
		args = new String[] { "-test" };
		if (args.length > 0) {
			// server = true;
		}

		initLogger();
		initLocale();
		initSavedir();
		initUpdater();
		injector = Guice.createInjector(new GuiceBindings("youtubeuploader" + (server ? "-server" : "")));

		if (!server) {
			System.out.println("CLIENT");
			GuiUploader.initialize(args, injector);
		} else {
			System.out.println("SERVER");
			ConsoleUploader.initialize(args, injector);
		}
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
		final Locale[] availableLocales = { Locale.GERMANY, Locale.GERMAN, Locale.ENGLISH };
		if (!Arrays.asList(availableLocales).contains(Locale.getDefault())) {
			Locale.setDefault(Locale.ENGLISH);
		}
	}

	private static void initLogger() {
		System.setOut(new PrintStream(System.out) {
			@Override
			public void print(final String s) {
				logger.info(s);
			}
		});
		System.setErr(new PrintStream(System.err) {
			@Override
			public void print(final String s) {
				logger.error(s);
			}
		});
	}

	public static void initDatabase() {
		final Executor exec = injector.getInstance(Executor.class);
		exec.update(Tables.UPLOAD).set(Tables.UPLOAD.INPROGRESS, false).execute();
	}
}
