/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.guice;

import javafx.fxml.FXMLLoader;
import javafx.util.Callback;

import com.google.inject.Injector;

/**
 * A JavaFX controller factory for constructing controllers via Guice DI. To
 * install this in the {@link FXMLLoader},
 * pass it as a parameter to {@link FXMLLoader#setControllerFactory(Callback)}.
 * <p>
 * Once set, make sure you do <b>not</b> use the static methods on
 * {@link FXMLLoader} when creating your JavaFX node.
 */
public class GuiceControllerFactory implements Callback<Class<?>, Object> {

	private final Injector	injector;

	public GuiceControllerFactory(final Injector anInjector) {
		injector = anInjector;
	}

	@Override
	public Object call(final Class<?> aClass) {
		return injector.getInstance(aClass);
	}
}
