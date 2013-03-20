package org.chaosfisch.youtubeuploader.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import jfxtras.labs.dialogs.MonologFX;
import jfxtras.labs.dialogs.MonologFXButton;
import jfxtras.labs.scene.control.ListSpinner;
import jfxtras.labs.scene.control.ListSpinner.ArrowPosition;

import org.chaosfisch.io.Throttle;
import org.chaosfisch.youtubeuploader.I18nHelper;
import org.chaosfisch.youtubeuploader.command.StartUploadCommand;
import org.chaosfisch.youtubeuploader.command.StopUploadCommand;
import org.chaosfisch.youtubeuploader.guice.ICommandProvider;
import org.chaosfisch.youtubeuploader.services.uploader.Uploader;

import com.google.inject.Inject;

public class QueueCommandController {

	@FXML
	private ResourceBundle					resources;

	@FXML
	private URL								location;

	@FXML
	private ChoiceBox<String>				actionOnFinish;

	@FXML
	private Button							startQueue;

	@FXML
	private Button							stopQueue;

	@FXML
	private HBox							viewElementsHBox;

	private final ListSpinner<Integer>		numberOfUploads		= new ListSpinner<Integer>(1,
																	5).withValue(1)
																	.withAlignment(Pos.CENTER_RIGHT)
																	.withPostfix(" Upload(s)")
																	.withPrefix("max. ")
																	.withArrowPosition(ArrowPosition.LEADING);
	private final ListSpinner<Integer>		uploadSpeed			= new ListSpinner<Integer>(0,
																	10000,
																	10).withValue(0)
																	.withAlignment(Pos.CENTER_RIGHT)
																	.withArrowPosition(ArrowPosition.LEADING)
																	.withPostfix(" kb/s")
																	.withEditable(true)
																	.withStringConverter(new StringConverter<Integer>() {

																		@Override
																		public String toString(final Integer arg0) {
																			return arg0.toString();
																		}

																		@Override
																		public Integer fromString(final String string) {
																			try {
																				final Integer number = Integer.parseInt(string);
																				return number;
																			} catch (final NumberFormatException e) {
																				return uploadSpeed.getValue();
																			}
																		}
																	});
	private final ObservableList<String>	actionOnFinishItems	= FXCollections.observableArrayList();

	@Inject
	private Uploader						uploader;
	@Inject
	private Throttle						throttle;
	@Inject
	private ICommandProvider				commandProvider;

	@FXML
	void startQueue(final ActionEvent event) {
		// TODO RESTRUCTURE DIALOGS
		final MonologFX dialog = new MonologFX(MonologFX.Type.ACCEPT);
		dialog.setTitleText(I18nHelper.message("dialog.youtubetos.title"));
		dialog.setMessage(I18nHelper.message("dialog.youtubetos.message"));
		final MonologFXButton yesButton = new MonologFXButton();
		yesButton.setType(MonologFXButton.Type.YES);
		yesButton.setLabel("Yes");
		final MonologFXButton noButton = new MonologFXButton();
		noButton.setType(MonologFXButton.Type.NO);
		noButton.setLabel("No");
		dialog.addButton(yesButton);
		dialog.addButton(noButton);
		if (dialog.showDialog() == MonologFXButton.Type.YES) {
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
		assert actionOnFinish != null : "fx:id=\"actionOnFinish\" was not injected: check your FXML file 'Queue.fxml'.";
		assert startQueue != null : "fx:id=\"startQueue\" was not injected: check your FXML file 'Queue.fxml'.";
		assert stopQueue != null : "fx:id=\"stopQueue\" was not injected: check your FXML file 'Queue.fxml'.";
		assert viewElementsHBox != null : "fx:id=\"viewElementsHBox\" was not injected: check your FXML file 'Queue.fxml'.";
		viewElementsHBox.getChildren()
			.addAll(numberOfUploads, uploadSpeed);

		initBindindings();

		actionOnFinishItems.addAll(I18nHelper.message("queuefinishedlist.donothing"),
			I18nHelper.message("queuefinishedlist.closeapplication"), I18nHelper.message("queuefinishedlist.shutdown"),
			I18nHelper.message("queuefinishedlist.hibernate"));
		actionOnFinish.setItems(actionOnFinishItems);
		actionOnFinish.getSelectionModel()
			.selectFirst();
	}

	private void initBindindings() {
		startQueue.disableProperty()
			.bind(uploader.inProgressProperty);
		stopQueue.disableProperty()
			.bind(uploader.inProgressProperty.not());
		uploader.actionOnFinish.bind(actionOnFinish.getSelectionModel()
			.selectedIndexProperty());
		uploader.maxUploads.bind(numberOfUploads.valueProperty());
		throttle.maxBps.bind(uploadSpeed.valueProperty());

	}

}
