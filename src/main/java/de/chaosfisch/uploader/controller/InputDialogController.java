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

import com.google.common.base.Strings;
import de.chaosfisch.uploader.renderer.Callback;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class InputDialogController extends UndecoratedDialogController {
	@FXML
	public Label title;

	@FXML
	public Label inputLabel;

	@FXML
	public TextField input;

	private Callback callback;

	@FXML
	public void initialize() {
		assert null != title : "fx:id=\"title\" was not injected: check your FXML file 'InputDialog.fxml'.";
		assert null != input : "fx:id=\"input\" was not injected: check your FXML file 'InputDialog.fxml'.";
		assert null != inputLabel : "fx:id=\"inputLabel\" was not injected: check your FXML file 'InputDialog.fxml'.";
	}

	@FXML
	public void okayAction(final ActionEvent actionEvent) {
		if (Strings.isNullOrEmpty(input.getText())) {
			input.getStyleClass().add("input-invalid");
		} else {
			input.getStyleClass().remove("input-invalid");
			callback.onInput(input.getText());
			closeDialog(actionEvent);
		}
	}

	public void setTitle(final String title) {
		this.title.setText(title);
	}

	public void setCallback(final Callback callback) {
		this.callback = callback;
	}

	public void setInput(final String input) {
		inputLabel.setText(input);
	}
}
