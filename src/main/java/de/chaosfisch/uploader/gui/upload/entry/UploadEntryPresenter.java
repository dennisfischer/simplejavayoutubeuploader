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

import de.chaosfisch.google.youtube.upload.Status;
import de.chaosfisch.uploader.gui.models.UploadModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.util.StringConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
		final LocalDateTimeStringConverter dateTimeStringConverter = new LocalDateTimeStringConverter("EEE, dd.MM.yyyy 'um' HH:mm");
		release.textProperty().bindBidirectional(uploadModel.releaseProperty(), dateTimeStringConverter);
		start.textProperty().bindBidirectional(uploadModel.startProperty(), dateTimeStringConverter);
		end.textProperty().bindBidirectional(uploadModel.endProperty(), dateTimeStringConverter);
		title.textProperty().bind(uploadModel.titleProperty());
		stopAfter.selectedProperty().bindBidirectional(uploadModel.stopAfterProperty());
		progress.progressProperty().bind(uploadModel.progressProperty());
		uploadModel.statusProperty().addListener((observableValue, oldStatus, newStatus) -> {
			statusChange(newStatus);

		});
		statusChange(uploadModel.getStatus());
	}

	private void statusChange(final Status status) {
		progress.setVisible(Status.RUNNING == status);

		switch (status) {
			case ABORTED:
				break;
			case ARCHIVED:
				break;
			case FAILED:
				break;
			case RUNNING:
				break;
			case WAITING:
				break;
			case LOCKED:
				break;
		}
	}

	private void unbindModel() {
		title.textProperty().unbind();
		release.textProperty().unbind();
		start.textProperty().unbind();
		end.textProperty().unbind();
		stopAfter.selectedProperty().unbind();
		progress.progressProperty().unbind();
	}

	private static class LocalDateTimeStringConverter extends StringConverter<LocalDateTime> {

		final DateTimeFormatter dateTimeFormatter;

		public LocalDateTimeStringConverter(final String format) {
			dateTimeFormatter = DateTimeFormatter.ofPattern(format);
		}

		@Override
		public String toString(final LocalDateTime localDateTime) {
			return null == localDateTime ? "" : localDateTime.format(dateTimeFormatter);
		}

		@Override
		public LocalDateTime fromString(final String string) {
			return null != string && string.isEmpty() ? null : LocalDateTime.parse(string, dateTimeFormatter);
		}
	}
}
