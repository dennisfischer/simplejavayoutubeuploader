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

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.inject.Inject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import org.chaosfisch.services.impl.EnddirServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class SettingsController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private CheckBox enddirCheckbox;

	@FXML
	private TextField homeDirTextField;

	@Inject
	private EnddirServiceImpl enddirService;

	final                Properties vmOptions     = new Properties();
	final                File       vmOptionsFile = new File("SimpleJavaYoutubeUploader.vmoptions");
	private static final Logger     logger        = LoggerFactory.getLogger(SettingsController.class);

	@FXML
	void openHomeDir(final ActionEvent event) {
		final DirectoryChooser directoryChooser = new DirectoryChooser();
		final File file = directoryChooser.showDialog(null);
		if (file != null && file.isDirectory()) {
			homeDirTextField.setText(file.getAbsolutePath());
			vmOptions.setProperty("user.home", file.getAbsolutePath());
			writeVMOptions();
		}
	}

	@FXML
	void toggleEnddirTitle(final ActionEvent event) {
		enddirService.setEnddirSetting(enddirService.getEnddirSetting());
	}

	@FXML
	void initialize() {
		assert enddirCheckbox != null : "fx:id=\"enddirCheckbox\" was not injected: check your FXML file 'Settings.fxml'.";
		assert homeDirTextField != null : "fx:id=\"homeDirTextField\" was not injected: check your FXML file 'Settings.fxml'.";
		enddirCheckbox.setSelected(enddirService.getEnddirSetting());

		loadVMOptions();
	}

	private void writeVMOptions() {
		try {
			vmOptions.store(Files.newWriter(vmOptionsFile, Charsets.UTF_8), "");
		} catch (IOException e) {
			logger.error("Failed writing vmoptions", e);
		}
	}

	private void loadVMOptions() {
		if (!vmOptionsFile.exists()) {
			return;
		}
		try {
			vmOptions.clear();
			vmOptions.load(Files.newReader(vmOptionsFile, Charsets.UTF_8));
			homeDirTextField.setText(vmOptions.getProperty("user.home", ""));
		} catch (IOException e) {
			logger.error("Failed loading vmoptions", e);
		}
	}
}
