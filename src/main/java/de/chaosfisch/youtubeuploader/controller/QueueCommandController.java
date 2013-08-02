/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.youtubeuploader.controller;

import com.google.inject.Inject;
import de.chaosfisch.google.youtube.upload.Uploader;
import de.chaosfisch.youtubeuploader.ActionOnFinish;
import de.chaosfisch.youtubeuploader.command.StartUploadCommand;
import de.chaosfisch.youtubeuploader.command.StopUploadCommand;
import de.chaosfisch.youtubeuploader.controller.renderer.ConfirmDialog;
import de.chaosfisch.youtubeuploader.db.dao.UploadDao;
import de.chaosfisch.youtubeuploader.guice.ICommandProvider;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import jfxtras.labs.dialogs.MonologFXButton;
import jfxtras.labs.scene.control.ListSpinner;
import jfxtras.labs.scene.control.ListSpinner.ArrowPosition;

import java.net.URL;
import java.util.ResourceBundle;

public class QueueCommandController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private ComboBox<ActionOnFinish> actionOnFinish;

	@FXML
	private Button startQueue;

	@FXML
	private Button stopQueue;

	@FXML
	private HBox viewElementsHBox;

	private final ListSpinner<Integer>           numberOfUploads     = new ListSpinner<Integer>(1, 5).withValue(1)
			.withAlignment(Pos.CENTER_RIGHT)
			.withPostfix(" Upload(s)")
			.withPrefix("max. ")
			.withArrowPosition(ArrowPosition.LEADING);
	private final ListSpinner<Integer>           uploadSpeed         = new ListSpinner<Integer>(0, 10000, 10).withValue(0)
			.withAlignment(Pos.CENTER_RIGHT)
			.withArrowPosition(ArrowPosition.LEADING)
			.withPostfix(" kb/s")
			.withEditable(true)
			.withStringConverter(new UploadSpeedStringConverter());
	private final ObservableList<ActionOnFinish> actionOnFinishItems = FXCollections.observableArrayList();

	@Inject
	private Uploader         uploader;
	@Inject
	private Throttle         throttle;
	@Inject
	private ICommandProvider commandProvider;
	@Inject
	private UploadDao        uploadDao;

	@FXML
	void clearQueue(final ActionEvent event) {
		uploadDao.delete(uploadDao.fetchByArchived(true));
	}

	@FXML
	void startQueue(final ActionEvent event) {
		final ConfirmDialog dialog = new ConfirmDialog(resources.getString("dialog.youtubetos.title"), resources.getString("dialog.youtubetos.message"), resources);
		if (MonologFXButton.Type.YES == dialog.showDialog()) {
			final StartUploadCommand command = commandProvider.get(StartUploadCommand.class);
			command.start();
		}
	}

	@FXML
	void stopQueue(final ActionEvent event) {
		final StopUploadCommand command = commandProvider.get(StopUploadCommand.class);
		command.start();
	}

	@FXML
	void initialize() {
		assert null != actionOnFinish : "fx:id=\"actionOnFinish\" was not injected: check your FXML file 'Queue.fxml'.";
		assert null != startQueue : "fx:id=\"startQueue\" was not injected: check your FXML file 'Queue.fxml'.";
		assert null != stopQueue : "fx:id=\"stopQueue\" was not injected: check your FXML file 'Queue.fxml'.";
		assert null != viewElementsHBox : "fx:id=\"viewElementsHBox\" was not injected: check your FXML file 'Queue.fxml'.";

		viewElementsHBox.getChildren().addAll(numberOfUploads, uploadSpeed);

		initBindindings();

		actionOnFinishItems.addAll(ActionOnFinish.values());
		actionOnFinish.setItems(actionOnFinishItems);
		actionOnFinish.getSelectionModel().selectFirst();
	}

	private void initBindindings() {
		startQueue.disableProperty().bind(uploader.inProgressProperty);
		stopQueue.disableProperty().bind(uploader.inProgressProperty.not());
		uploader.actionOnFinish.bind(actionOnFinish.getSelectionModel().selectedItemProperty());
		uploader.maxUploads.bind(numberOfUploads.valueProperty());
		throttle.maxBps.bind(uploadSpeed.valueProperty());
		actionOnFinish.setConverter(new StringConverter<ActionOnFinish>() {
			@Override
			public String toString(final ActionOnFinish actionOnFinish) {
				return actionOnFinish.toString();
			}

			@Override
			public ActionOnFinish fromString(final String command) {
				for (final ActionOnFinish action : ActionOnFinish.values()) {
					if (action.toString().equals(command)) {
						return action;
					}
				}
				return ActionOnFinish.CUSTOM.set(command);
			}
		});
	}

	private final class UploadSpeedStringConverter extends StringConverter<Integer> {
		@Override
		public String toString(final Integer integer) {
			return integer.toString();
		}

		@Override
		public Integer fromString(final String string) {
			try {
				return Integer.parseInt(string);
			} catch (final NumberFormatException e) { // $codepro.audit.disable
				// logExceptions
				return uploadSpeed.getValue();
			}
		}
	}
}
