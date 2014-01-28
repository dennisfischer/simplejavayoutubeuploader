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

import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public abstract class FXMLView extends Region {

	public static final String DEFAULT_ENDING = "view";
	private static Callback<Class<?>, Object> controllerFactory;

	private FXMLLoader loader;

	public FXMLView() {
		init(getClass(), getFXMLName());
	}

	static String stripEnding(final String clazz) {
		if (!clazz.endsWith(DEFAULT_ENDING)) {
			return clazz;
		}
		final int viewIndex = clazz.lastIndexOf(DEFAULT_ENDING);
		return clazz.substring(0, viewIndex);
	}

	static ResourceBundle getResourceBundle(final String name) {
		try {
			return ResourceBundle.getBundle(name);
		} catch (final MissingResourceException ex) {
			return null;
		}
	}

	public static void setControllerFactory(final Callback<Class<?>, Object> controllerFactory) {
		FXMLView.controllerFactory = controllerFactory;
	}

	private void init(final Class clazz, final String conventionalName) {
		final URL resource = clazz.getResource(conventionalName);
		final String bundleName = getBundleName();
		final ResourceBundle bundle = getResourceBundle(bundleName);
		loader = loadAsynchronously(resource, bundle);

		getChildren().add(loader.getRoot());
	}

	@Override
	protected void layoutChildren() {
		for (final Node node : getChildren()) {
			layoutInArea(node, 0, 0, getWidth(), getHeight(), 0, HPos.LEFT, VPos.TOP);
		}
	}

	FXMLLoader loadAsynchronously(final URL resource, final ResourceBundle bundle) throws IllegalStateException {
		final FXMLLoader loader = new FXMLLoader(resource, bundle);
		if (null != controllerFactory) {
			loader.setControllerFactory(controllerFactory::call);
		}

		try {
			loader.load();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		return loader;
	}

	public Parent getView() {
		final Parent parent = getLoader().getRoot();
		addCSSIfAvailable(parent);
		return parent;
	}

	void addCSSIfAvailable(final Parent parent) {
		final URL uri = getClass().getResource(getStyleSheetName());
		if (null == uri) {
			return;
		}
		final String uriToCss = uri.toExternalForm();
		parent.getStylesheets().add(uriToCss);
	}

	String getStyleSheetName() {
		return getConventionalName(".css");
	}

	public Object getPresenter() {
		return getLoader().getController();
	}

	String getConventionalName(final String ending) {
		return getConventionalName() + ending;
	}

	String getConventionalName() {
		final String clazz = getClass().getSimpleName().toLowerCase();
		return stripEnding(clazz);
	}

	String getBundleName() {
		final String conventionalName = getConventionalName();
		return String.format("%s.%s", getClass().getPackage().getName(), conventionalName);
	}

	final String getFXMLName() {
		return getConventionalName(".fxml");
	}

	FXMLLoader getLoader() {
		return loader;
	}
}