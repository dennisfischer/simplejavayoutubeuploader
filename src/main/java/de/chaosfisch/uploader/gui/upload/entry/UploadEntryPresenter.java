/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.uploader.gui.upload.entry;

import de.chaosfisch.uploader.gui.models.UploadModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;

public class UploadEntryPresenter {
	@FXML
	public ProgressBar progress;

	@FXML
	private Label title;

	@FXML
	private Label release;

	@FXML
	private Label start;

	@FXML
	private Label end;

	@FXML
	private ToggleButton stopAfter;

	private UploadModel uploadModel;

	public UploadModel getUploadModel() {
		return uploadModel;
	}

	public void setUploadModel(final UploadModel uploadModel) {
		this.uploadModel = uploadModel;
		bindModel();
	}

	private void bindModel() {
		unbindModel();
		title.textProperty().bind(uploadModel.titleProperty());
		release.textProperty().bind(uploadModel.releaseProperty().asString());
		start.textProperty().bind(uploadModel.startProperty().asString());
		end.textProperty().bind(uploadModel.endProperty().asString());
		stopAfter.selectedProperty().bindBidirectional(uploadModel.stopAfterProperty());
		progress.progressProperty().bind(uploadModel.progressProperty());
	}

	private void unbindModel() {
		title.textProperty().unbind();
		release.textProperty().unbind();
		start.textProperty().unbind();
		end.textProperty().unbind();
		stopAfter.selectedProperty().unbind();
		progress.progressProperty().unbind();
	}
}
