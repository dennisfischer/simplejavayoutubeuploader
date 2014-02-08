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
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.inject.Inject;
import de.chaosfisch.google.enddir.IEnddirService;
import de.chaosfisch.google.youtube.upload.Uploader;
import de.chaosfisch.services.ExportPostProcessor;
import de.chaosfisch.uploader.gui.renderer.Callback;
import de.chaosfisch.uploader.gui.renderer.DialogHelper;
import de.chaosfisch.uploader.gui.renderer.ProgressNodeRenderer;
import de.chaosfisch.uploader.gui.renderer.TagTextArea;
import de.chaosfisch.uploader.persistence.dao.IPersistenceService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

@FXMLController
public class SettingsController {

	@FXML
	public CheckBox stopQueueOnError;

	@FXML
	public CheckBox createJsonLogfilesCheckbox;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private CheckBox enddirCheckbox;

	@FXML
	public TextField enddirTitle;

	@FXML
	private TextField homeDirTextField;

	@FXML
	private CheckBox progressCheckbox;

	@FXML
	private CheckBox masterPasswordCheckbox;

	@FXML
	public CheckBox oldTagsCheckbox;

	private final Properties vmOptions = new Properties();
	private final File vmOptionsFile = new File("SimpleJavaYoutubeUploader.vmoptions");
	private static final Logger LOGGER = LoggerFactory.getLogger(SettingsController.class);

	private final IPersistenceService persistenceService;
	private final DialogHelper dialogHelper;
	private final Configuration config;

	@Inject
	private SettingsController(final IPersistenceService persistenceService, final DialogHelper dialogHelper, final Configuration config) {
		this.persistenceService = persistenceService;
		this.dialogHelper = dialogHelper;
		this.config = config;
	}

	@FXML
	void enddirTitleTyped(final KeyEvent event) {
		config.setProperty(IEnddirService.TITLE_PROPERTY, Strings.isNullOrEmpty(enddirTitle.getText()) ?
				IEnddirService.TITLE_DEFAULT :
				enddirTitle.getText());
	}

	@FXML
	void openHomeDir(final ActionEvent event) {
		final DirectoryChooser directoryChooser = new DirectoryChooser();
		final File file = directoryChooser.showDialog(null);
		if (null != file && file.isDirectory()) {
			homeDirTextField.setText(file.getAbsolutePath());
			vmOptions.setProperty("user.home", file.getAbsolutePath());
			writeVMOptions();
		}
	}

	@FXML
	void toggleEnddirTitle(final ActionEvent event) {
		config.setProperty(IEnddirService.RENAME_PROPERTY, !config.getBoolean(IEnddirService.RENAME_PROPERTY, false));
	}

	@FXML
	void toggleProgress(final ActionEvent event) {
		config.setProperty(ProgressNodeRenderer.DISPLAY_PROGRESS, progressCheckbox.isSelected());
	}

	@FXML
	void toggleMasterPassword(final ActionEvent event) {
		if (masterPasswordCheckbox.isSelected()) {
			masterPasswordCheckbox.setSelected(!masterPasswordCheckbox.isSelected());
			dialogHelper.showInputDialog("Masterpasswort", "Masterpasswort:", new Callback() {
				@Override
				public void onInput(final InputDialogController controller, final String input) {
					if (Strings.isNullOrEmpty(input)) {
						controller.input.getStyleClass().add("input-invalid");
					} else {
						persistenceService.generateBackup();
						persistenceService.setMasterPassword(input);
						persistenceService.saveToStorage();
						config.setProperty(IPersistenceService.MASTER_PASSWORD, !masterPasswordCheckbox.isSelected());
						masterPasswordCheckbox.setSelected(!masterPasswordCheckbox.isSelected());
						controller.closeDialog(null);
					}
				}
			}, true);
		} else {
			persistenceService.generateBackup();
			persistenceService.setMasterPassword(null);
			persistenceService.saveToStorage();
			config.setProperty(IPersistenceService.MASTER_PASSWORD, masterPasswordCheckbox.isSelected());
		}
		persistenceService.saveToStorage();
	}

	@FXML
	public void toggleOldTags(final ActionEvent actionEvent) {
		config.setProperty(TagTextArea.OLD_TAG_INPUT, oldTagsCheckbox.isSelected());
	}

	@FXML
	public void toggleStopQueueOnError(final ActionEvent actionEvent) {
		config.setProperty(Uploader.STOP_ON_ERROR, stopQueueOnError.isSelected());
	}

	@FXML
	public void toggleJson(final ActionEvent actionEvent) {
		config.setProperty(ExportPostProcessor.JSON_LOGFILES, createJsonLogfilesCheckbox.isSelected());
	}

	@FXML
	void initialize() {
		assert null != enddirTitle : "fx:id=\"enddirTitle\" was not injected: check your FXML file 'Settings.fxml'.";
		assert null != enddirCheckbox : "fx:id=\"enddirCheckbox\" was not injected: check your FXML file 'Settings.fxml'.";
		assert null != homeDirTextField : "fx:id=\"homeDirTextField\" was not injected: check your FXML file 'Settings.fxml'.";
		assert null != masterPasswordCheckbox : "fx:id=\"masterPasswordCheckbox\" was not injected: check your FXML file 'Settings.fxml'.";
		assert null != oldTagsCheckbox : "fx:id=\"oldTagsCheckbox\" was not injected: check your FXML file 'Settings.fxml'.";
		assert null != progressCheckbox : "fx:id=\"progressCheckbox\" was not injected: check your FXML file 'Settings.fxml'.";
		assert null != stopQueueOnError : "fx:id=\"stopQueueOnError\" was not injected: check your FXML file 'Settings.fxml'.";
		assert null != createJsonLogfilesCheckbox : "fx:id=\"createJsonLogfilesCheckbox\" was not injected: check your FXML file 'Settings.fxml'.";

		loadValuesFromConfig();
		loadVMOptions();
	}

	private void loadValuesFromConfig() {
		enddirTitle.setText(config.getString(IEnddirService.TITLE_PROPERTY, IEnddirService.TITLE_DEFAULT));
		enddirCheckbox.setSelected(config.getBoolean(IEnddirService.RENAME_PROPERTY, false));
		progressCheckbox.setSelected(config.getBoolean(ProgressNodeRenderer.DISPLAY_PROGRESS, false));
		masterPasswordCheckbox.setSelected(config.getBoolean(IPersistenceService.MASTER_PASSWORD, false));
		oldTagsCheckbox.setSelected(config.getBoolean(TagTextArea.OLD_TAG_INPUT, false));
		stopQueueOnError.setSelected(config.getBoolean(Uploader.STOP_ON_ERROR, false));
		createJsonLogfilesCheckbox.setSelected(config.getBoolean(ExportPostProcessor.JSON_LOGFILES, false));
	}

	private void writeVMOptions() {
		try {
			vmOptions.store(Files.newWriter(vmOptionsFile, Charsets.UTF_8), "");
		} catch (final IOException e) {
			LOGGER.error("Failed writing vmoptions", e);
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
		} catch (final IOException e) {
			LOGGER.error("Failed loading vmoptions", e);
		}
	}
}
