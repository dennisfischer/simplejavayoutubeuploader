/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.controller.renderer;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class ImageButton extends Button {

	private static final String STYLE_NORMAL  = "-fx-background-color: transparent; -fx-padding: 3, 3, 3, 3; ";
	private static final String STYLE_PRESSED = "-fx-background-color: transparent; -fx-padding: 4 2 2 4;";

	private final SimpleObjectProperty<Image> image = new SimpleObjectProperty<>();

	public ImageButton() {
		setStyle(STYLE_NORMAL);
		setOnMousePressed(new MousePressedHandler());
		setOnMouseReleased(new MouseReleasedHandler());
		image.addListener(new InvalidationListener() {
			@Override
			public void invalidated(final Observable observable) {
				setGraphic(new ImageView(image.getValue()));
			}
		});
	}

	public Image getImage() {
		return image.getValue();
	}

	public void setImage(final Image image) {
		this.image.setValue(image);
	}

	public SimpleObjectProperty<Image> imageProperty() {
		return image;
	}

	private class MousePressedHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(final MouseEvent event) {
			setStyle(STYLE_PRESSED);
		}
	}

	private class MouseReleasedHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(final MouseEvent event) {
			setStyle(STYLE_NORMAL);
		}
	}
}