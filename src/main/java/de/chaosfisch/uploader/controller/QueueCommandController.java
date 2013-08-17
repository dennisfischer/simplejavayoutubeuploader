/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.controller;

import com.cathive.fx.guice.FXMLController;
import com.cathive.fx.guice.GuiceFXMLLoader;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import de.chaosfisch.google.youtube.upload.IUploadService;
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.google.youtube.upload.events.UploadFinishedEvent;
import de.chaosfisch.uploader.ActionOnFinish;
import de.chaosfisch.util.ComputerUtil;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageBuilder;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import jfxtras.labs.scene.control.ListSpinner;
import jfxtras.labs.scene.control.ListSpinner.ArrowPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@FXMLController
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

	@Inject
	private GuiceFXMLLoader fxmlLoader;

	private static final Logger logger           = LoggerFactory.getLogger(QueueCommandController.class);
	private static final int    MAX_UPLOAD_SPEED = 10000;

	private final ListSpinner<Integer>           numberOfUploads     = new ListSpinner<Integer>(1, 5).withValue(1)
			.withAlignment(Pos.CENTER_RIGHT)
			.withPostfix(" Upload(s)")
			.withPrefix("max. ")
			.withArrowPosition(ArrowPosition.LEADING);
	private final ListSpinner<Integer>           uploadSpeed         = new ListSpinner<Integer>(0, MAX_UPLOAD_SPEED, 10)
			.withValue(0)
			.withAlignment(Pos.CENTER_RIGHT)
			.withArrowPosition(ArrowPosition.LEADING)
			.withPostfix(" kb/s")
			.withEditable(true)
			.withStringConverter(new UploadSpeedStringConverter());
	private final ObservableList<ActionOnFinish> actionOnFinishItems = FXCollections.observableArrayList();

	@Inject
	private IUploadService uploadService;
	@Inject
	private ComputerUtil   computerUtil;

	@FXML
	void clearQueue(final ActionEvent event) {
		final List<Upload> uploads = uploadService.fetchByArchived(true);
		for (final Upload upload : uploads) {
			uploadService.delete(upload);
		}
	}

	@FXML
	void startQueue(final ActionEvent event) {
		try {
			final GuiceFXMLLoader.Result result = fxmlLoader.load(getClass().getResource("/de/chaosfisch/uploader/view/ConfirmDialog.fxml"), resources);
			final ConfirmDialogController controller = result.getController();
			controller.setTitle(resources.getString("dialog.youtubetos.title"));
			controller.setMessage(resources.getString("dialog.youtubetos.message"));

			final Parent parent = result.getRoot();
			final Scene scene = SceneBuilder.create().root(parent).build();
			final Stage stage = StageBuilder.create().scene(scene).build();
			stage.initStyle(StageStyle.UNDECORATED);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.showAndWait();
			stage.requestFocus();
			if (controller.ask()) {
				uploadService.startUploading();
			}
		} catch (IOException e) {
			logger.error("Couldn't load ConfirmDialog", e);
		}
	}

	@FXML
	void stopQueue(final ActionEvent event) {
		uploadService.stopUploading();
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

		numberOfUploads.valueProperty().addListener(new ChangeListener<Integer>() {
			@Override
			public void changed(final ObservableValue<? extends Integer> observableValue, final Integer oldMaxUploads, final Integer newMaxUploads) {
				uploadService.setMaxUploads(null == newMaxUploads ? 0 : newMaxUploads);
			}
		});
	}

	private void initBindindings() {
		startQueue.disableProperty().bind(uploadService.runningProperty());
		stopQueue.disableProperty().bind(uploadService.runningProperty().not());
		/*
		throttle.maxBps.bind(uploadSpeed.valueProperty());          */

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
			} catch (final NumberFormatException e) {
				return uploadSpeed.getValue();
			}
		}
	}

	@Subscribe
	public void onUploadsFinished(final UploadFinishedEvent event) {
		final ActionOnFinish action = actionOnFinish.getSelectionModel().getSelectedItem();
		switch (action) {
			default:
			case NOTHING:
				return;
			case CLOSE:
				logger.info("CLOSING APPLICATION");
				Platform.exit();
				break;
			case SHUTDOWN:
				logger.info("SHUTDOWN COMPUTER");
				computerUtil.shutdownComputer();
				break;
			case SLEEP:
				logger.info("HIBERNATE COMPUTER");
				computerUtil.hibernateComputer();
				break;
			case CUSTOM:
				logger.info("Custom command: {}", action.getCommand());
				computerUtil.customCommand(action.getCommand());
				break;
		}
	}
}
