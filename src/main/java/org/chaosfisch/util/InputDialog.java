/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.util;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class InputDialog extends Stage {
	private final Button submit = new Button("Okay");

	public InputDialog(final String title, final Object[] data) {
		initOwner(null);

		setTitle(title);

		if (0 != data.length % 2) {
			throw new IllegalArgumentException("Object data must be even!");
		}

		final Group root = new Group();
		final Scene scene = new Scene(root);
		setScene(scene);

		final GridPane gridpane = new GridPane();
		gridpane.setPadding(new Insets(5));
		gridpane.setHgap(5);
		gridpane.setVgap(5);

		for (int i = 0; i < data.length; i++) {
			if (0 == i % 2) {
				final Label label = new Label((String) data[i]);
				gridpane.add(label, 0, i / 2);
			} else {
				gridpane.add((Node) data[i], 1, (i - 1) / 2);
			}
		}

		gridpane.add(submit, 1, data.length / 2 + 1);
		GridPane.setHalignment(submit, HPos.RIGHT);
		root.getChildren().add(gridpane);
		sizeToScene();
		show();
	}

	public void setCallback(final EventHandler<ActionEvent> callback) {
		submit.setOnAction(callback);
	}
}
