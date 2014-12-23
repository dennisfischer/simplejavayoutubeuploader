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

import de.chaosfisch.youtube.upload.Status;
import de.chaosfisch.youtube.upload.UploadModel;
import javafx.beans.binding.StringBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.util.StringConverter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class UploadEntryPresenter {

	@FXML
	private ProgressBar progress;

	@FXML
	private Label progressLabel;

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
		final ZonedDateTimeStringConverter dateTimeStringConverter = new ZonedDateTimeStringConverter("EEE, dd.MM.yyyy 'um' HH:mm");
		release.textProperty().bindBidirectional(uploadModel.dateTimeOfReleaseProperty(), dateTimeStringConverter);
		start.textProperty().bindBidirectional(uploadModel.dateTimeOfStartProperty(), dateTimeStringConverter);
		end.textProperty().bindBidirectional(uploadModel.dateTimeOfEndProperty(), dateTimeStringConverter);
		title.textProperty().bind(uploadModel.metadataProperty().get().titleProperty());
		stopAfter.selectedProperty().bindBidirectional(uploadModel.stopAfterProperty());
		progress.progressProperty().bind(uploadModel.progressProperty());
		progress.visibleProperty().bind(uploadModel.statusProperty().isEqualTo(Status.RUNNING));

		progressLabel.textProperty().bind(new StringBinding() {
			{
				bind(uploadModel.statusProperty());
				bind(uploadModel.progressProperty());
			}

			@Override
			protected String computeValue() {
				switch (uploadModel.getStatus()) {
					case ABORTED:
						return "Aborted by user!";
					case FINISHED:
						return "Upload finished!";
					case FAILED:
						return "Upload failed!";
					case RUNNING:
						return String.format("%.2f%%", uploadModel.getProgress());
					case WAITING:
						return "Wating for start";
					default:
						return "Illegal status received!";
				}
			}
		});
	}

	private void unbindModel() {
		title.textProperty().unbind();
		release.textProperty().unbind();
		start.textProperty().unbind();
		end.textProperty().unbind();
		stopAfter.selectedProperty().unbind();
		progress.progressProperty().unbind();
	}

	private static class ZonedDateTimeStringConverter extends StringConverter<ZonedDateTime> {

		final DateTimeFormatter dateTimeFormatter;

		public ZonedDateTimeStringConverter(final String format) {
			dateTimeFormatter = DateTimeFormatter.ofPattern(format);
		}

		@Override
		public String toString(final ZonedDateTime localDateTime) {
			return null == localDateTime ? "" : localDateTime.format(dateTimeFormatter);
		}

		@Override
		public ZonedDateTime fromString(final String string) {
			return null != string && string.isEmpty() ? null : ZonedDateTime.parse(string, dateTimeFormatter);
		}
	}
}