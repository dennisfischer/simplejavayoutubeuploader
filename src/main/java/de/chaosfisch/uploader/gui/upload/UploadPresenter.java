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

import de.chaosfisch.google.youtube.upload.Status;
import de.chaosfisch.uploader.gui.DataModel;
import de.chaosfisch.uploader.gui.models.UploadModel;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

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
		uploads.itemsProperty().bindBidirectional(dataModel.uploadsProperty());
	}

	@FXML
	public void openFiles() {
		final FileChooser fileChooser = new FileChooser();
		handleInsertedFiles(fileChooser.showOpenMultipleDialog(null));
	}

	@FXML
	public void mouseEntered(final MouseEvent mouseEvent) {
		((Node) mouseEvent.getSource()).setCursor(Cursor.HAND);
	}


	@FXML
	public void filesDragDropped(final DragEvent event) {
		final Dragboard db = event.getDragboard();

		if (db.hasFiles()) {
			handleInsertedFiles(db.getFiles());
			event.setDropCompleted(true);
		} else {
			event.setDropCompleted(false);
		}
		event.consume();
	}

	@FXML
	public void filesDragOver(final DragEvent event) {
		final Dragboard db = event.getDragboard();
		if (db.hasFiles()) {
			event.acceptTransferModes(TransferMode.COPY);
		}
		event.consume();
	}

	private void handleInsertedFiles(final List<File> files) {
		if (null == files) {
			return;
		}
		for (final File file : files) {
			final UploadModel model = new UploadModel();
			model.setStatus(Status.WAITING);
			model.setTitle(file.getName());
			dataModel.addUpload(model);
		}
	}

	@FXML
	public void filesDragEntered() {
		dropzone.setStyle("-fx-border-style: solid; -fx-border-insets: 3px");
	}

	@FXML
	public void filesDragExited() {
		dropzone.setStyle("-fx-border-style: dotted");
	}

}
