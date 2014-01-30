/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.uploader.gui.upload;

import de.chaosfisch.uploader.gui.DataModel;
import de.chaosfisch.uploader.gui.models.UploadModel;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.HBox;

import javax.inject.Inject;

public class UploadPresenter {

	@FXML
	public ListView<UploadModel> uploads;

	@FXML
	public HBox dropzone;

	@Inject
	protected DataModel dataModel;

	@FXML
	public void initialize() {
		uploads.setCellFactory(new UploadCellFactory());
		uploads.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		dataModel.setSelectedUploads(uploads.getSelectionModel().getSelectedItems());
		uploads.setItems(dataModel.uploadObservableList());
	}
}
