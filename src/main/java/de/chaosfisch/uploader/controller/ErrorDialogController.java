/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ErrorDialogController extends UndecoratedDialogController {
	@FXML
	private Label title;
	@FXML
	private Label message;

	@FXML
	public void initialize() {
		assert null != title : "fx:id=\"title\" was not injected: check your FXML file 'ConfirmDialog.fxml'.";
		assert null != message : "fx:id=\"message\" was not injected: check your FXML file 'ConfirmDialog.fxml'.";
	}

	public void setTitle(final String title) {
		this.title.setText(title);
	}

	public void setMessage(final String message) {
		this.message.setText(message);
	}

	public void onAccept(final ActionEvent actionEvent) {
		closeDialog(actionEvent);
	}
}
