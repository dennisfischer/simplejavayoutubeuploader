/*
 * Copyright (c) 2014 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.gui.controller;

import com.cathive.fx.guice.FXMLController;
import com.google.inject.Inject;
import de.chaosfisch.uploader.gui.renderer.DialogHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.net.URL;
import java.util.ResourceBundle;

@FXMLController
public class AccountAddController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	private final DialogHelper dialogHelper;

	@Inject
	public AccountAddController(final DialogHelper dialogHelper) {
		this.dialogHelper = dialogHelper;
	}

	@FXML
	void addAccount(final ActionEvent event) {
		dialogHelper.showAccountPermissionsDialog();
	}
}
