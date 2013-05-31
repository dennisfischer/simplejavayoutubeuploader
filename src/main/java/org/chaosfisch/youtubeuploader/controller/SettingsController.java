/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader.controller;

import com.google.inject.Inject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import org.chaosfisch.services.impl.EnddirServiceImpl;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private CheckBox enddirCheckbox;

	@Inject
	private EnddirServiceImpl enddirService;

	@FXML
	void toggleEnddirTitle(final ActionEvent event) {
		enddirService.setEnddirSetting(enddirService.getEnddirSetting());
	}

	@FXML
	void initialize() {
		assert enddirCheckbox != null : "fx:id=\"enddirCheckbox\" was not injected: check your FXML file 'Settings.fxml'.";
		enddirCheckbox.setSelected(enddirService.getEnddirSetting());
	}
}
