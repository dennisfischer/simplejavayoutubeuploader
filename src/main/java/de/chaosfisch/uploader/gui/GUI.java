/*
 * Copyright (c) 2014 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.gui;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.controlsfx.dialog.Dialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GUI extends Application {

	private static final Logger LOGGER = LoggerFactory.getLogger(GUI.class);

	@Override
	public void init() throws Exception {
		Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
			LOGGER.error("Uncaught exception occured", e);
			Platform.runLater(() -> {
				Dialogs.create().owner(null)
						.title("Exception occured!")
						.masthead("Exception message is shown below")
						.showException(e);
			});
		});
	}

	@Override
	public void start(final Stage stage) throws Exception {
		final Parent root = FXMLLoader.load(getClass().getResource("/de/chaosfisch/uploader/gui/edit/edit.fxml"));
		final Scene scene = new Scene(root, 1000, 500);
		scene.getStylesheets().add(getClass().getResource("/de/chaosfisch/uploader/gui/style.css").toExternalForm());
		stage.setScene(scene);
		stage.setTitle("JavaFX Memory Visualizer");
		stage.show();
	}
}
