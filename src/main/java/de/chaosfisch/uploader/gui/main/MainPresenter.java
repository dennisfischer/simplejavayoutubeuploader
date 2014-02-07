/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.uploader.gui.main;


import dagger.Lazy;
import de.chaosfisch.uploader.gui.DataModel;
import de.chaosfisch.uploader.gui.edit.EditView;
import de.chaosfisch.uploader.gui.models.UploadModel;
import de.chaosfisch.uploader.gui.upload.UploadView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MainPresenter {
	@Inject
	protected Lazy<EditView> editViewLazy;
	@Inject
	protected Lazy<UploadView> uploadsViewLazy;
	@Inject
	protected DataModel dataModel;
	@FXML
	private BorderPane mainFrame;
	@FXML
	private Button startButton;
	@FXML
	private Button stopButton;
	@FXML
	private ComboBox actionOnFinishList;

	public void initialize() {
		mainFrame.setCenter(editViewLazy.get().getView());
	}

	@FXML
	public void removeUploads(final ActionEvent actionEvent) {
		// @BUG had to add workaround by converting observable list to array
		// otherwise only one element is removed and not N selected elements
		final UploadModel[] uploads = new UploadModel[dataModel.getSelectedUploads().size()];
		dataModel.getSelectedUploads().toArray(uploads);
		for (final UploadModel upload : uploads) {
			dataModel.removeUpload(upload);
		}
	}
}
