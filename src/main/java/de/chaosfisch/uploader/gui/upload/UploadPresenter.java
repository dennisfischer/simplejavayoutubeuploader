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

import de.chaosfisch.controls.NumberStringFormatConverter;
import de.chaosfisch.controls.spinner.NumberSpinner;
import de.chaosfisch.uploader.gui.DataModel;
import de.chaosfisch.uploader.gui.Tab;
import de.chaosfisch.uploader.gui.edit.EditDataModel;
import de.chaosfisch.youtube.upload.UploadModel;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
import java.util.regex.Pattern;

public class UploadPresenter {

	@Inject
	protected DataModel dataModel;

	@Inject
	protected EditDataModel         editDataModel;
	@Inject
	protected UploadDataModel       uploadDataModel;
	@FXML
	private   NumberSpinner         maxUploads;
	@FXML
	private   NumberSpinner         maxSpeed;
	@FXML
	private   ListView<UploadModel> uploads;
	@FXML
	private   HBox                  dropzone;
	@FXML
	private   Button                startButton;
	@FXML
	private   Button                stopButton;
	@FXML
	private   ComboBox              actionOnFinishList;

	@FXML
	public void initialize() {
		uploads.setCellFactory(new UploadCellFactory());
		uploads.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		maxUploads.setNumberStringConverter(new NumberStringFormatConverter("%d max Upload(s)", Pattern.compile("[^0-9]+")));
		maxSpeed.setNumberStringConverter(new NumberStringFormatConverter("%d kByte/s", Pattern.compile("[^0-9]+")));

		uploadDataModel.setSelectedUploads(uploads.getSelectionModel().getSelectedItems());

		uploads.itemsProperty().bindBidirectional(uploadDataModel.uploadsProperty());
		maxSpeed.valueProperty().bindBidirectional(uploadDataModel.maxSpeedProperty());
		maxUploads.valueProperty().bindBidirectional(uploadDataModel.maxUploadsProperty());
		startButton.disableProperty().bind(uploadDataModel.runningProperty().not());
		stopButton.disableProperty().bind(uploadDataModel.runningProperty());
	}

	@FXML
	public void openFiles() {
		final FileChooser fileChooser = new FileChooser();
		handleInsertedFiles(fileChooser.showOpenMultipleDialog(null));
	}

	private void handleInsertedFiles(final List<File> files) {
		if (null == files) {
			return;
		}
		for (final File file : files) {
			editDataModel.addFile(file);
			dataModel.setActiveTabProperty(Tab.EDIT);
		}
	}

	@FXML
	public void removeUploads() {
		// @BUG had to add workaround by converting observable list to array
		// otherwise only one element is removed and not N selected elements
		final UploadModel[] uploads = new UploadModel[uploadDataModel.getSelectedUploads().size()];
		uploadDataModel.getSelectedUploads().toArray(uploads);
		for (final UploadModel upload : uploads) {
			uploadDataModel.removeUpload(upload);
		}
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

	@FXML
	public void filesDragEntered() {
		dropzone.setStyle("-fx-border-style: solid; -fx-border-insets: 3px");
	}

	@FXML
	public void filesDragExited() {
		dropzone.setStyle("-fx-border-style: dotted");
	}

}
