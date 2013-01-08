/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.controller;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import jfxtras.labs.dialogs.MonologFX;
import jfxtras.labs.dialogs.MonologFXButton;
import jfxtras.labs.scene.control.ListSpinner;
import jfxtras.labs.scene.control.ListSpinner.ArrowPosition;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.util.ActiveCellValueFactory;
import org.chaosfisch.util.RefresherUtil;
import org.chaosfisch.util.io.Throttle;
import org.chaosfisch.youtubeuploader.I18nHelper;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.ModelEvents;
import org.chaosfisch.youtubeuploader.models.Upload;
import org.chaosfisch.youtubeuploader.services.youtube.uploader.UploadProgress;
import org.chaosfisch.youtubeuploader.services.youtube.uploader.Uploader;
import org.chaosfisch.youtubeuploader.view.models.UploadViewModel;
import org.javalite.activejdbc.Model;

import com.google.inject.Inject;

public class QueueController implements Initializable
{

	@FXML// fx:id="actionOnFinish"
	private ChoiceBox<String>			actionOnFinish;

	@FXML// fx:id="columnAccount"
	private TableColumn<Upload, String>	columnAccount;

	@FXML// fx:id="columnActions"
	private TableColumn<Upload, Upload>	columnActions;

	@FXML// fx:id="columnCategory"
	private TableColumn<Upload, String>	columnCategory;

	@FXML// fx:id="columnId"
	private TableColumn<Upload, Number>	columnId;

	@FXML// fx:id="columnProgress"
	private TableColumn<Upload, Upload>	columnProgress;

	@FXML// fx:id="columnTitle"
	private TableColumn<Upload, String>	columnTitle;

	@FXML// fx:id="columnStarttime"
	private TableColumn<Upload, Object>	columnStarttime;

	@FXML// fx:id="queueActionsGridpane"
	private GridPane					queueActionsGridpane;

	@FXML// fx:id="queueTableview"
	private TableView<Model>			queueTableview;

	@FXML// fx:id="startQueue"
	private Button						startQueue;

	@FXML// fx:id="stopQueue"
	private Button						stopQueue;

	private final ListSpinner<Integer>	numberOfUploads	= new ListSpinner<Integer>(1, 5).withValue(1)
																.withAlignment(Pos.CENTER_RIGHT)
																.withPostfix(" Upload(s)")
																.withPrefix("max. ")
																.withArrowPosition(ArrowPosition.LEADING);
	private final ListSpinner<Integer>	uploadSpeed		= new ListSpinner<Integer>(0, 10000, 50).withValue(0)
																.withAlignment(Pos.CENTER_RIGHT)
																.withArrowPosition(ArrowPosition.LEADING)
																.withPostfix(" kb/s");

	@Inject Uploader					uploader;
	@Inject Throttle					throttle;
	@Inject UploadViewModel				uploadViewModel;

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(final URL fxmlFileLocation, final ResourceBundle resources)
	{
		assert actionOnFinish != null : "fx:id=\"actionOnFinish\" was not injected: check your FXML file 'Queue.fxml'.";
		assert columnAccount != null : "fx:id=\"columnAccount\" was not injected: check your FXML file 'Queue.fxml'.";
		assert columnActions != null : "fx:id=\"columnActions\" was not injected: check your FXML file 'Queue.fxml'.";
		assert columnCategory != null : "fx:id=\"columnCategory\" was not injected: check your FXML file 'Queue.fxml'.";
		assert columnId != null : "fx:id=\"columnId\" was not injected: check your FXML file 'Queue.fxml'.";
		assert columnProgress != null : "fx:id=\"columnProgress\" was not injected: check your FXML file 'Queue.fxml'.";
		assert columnTitle != null : "fx:id=\"columnTitle\" was not injected: check your FXML file 'Queue.fxml'.";
		assert queueTableview != null : "fx:id=\"queueTableview\" was not injected: check your FXML file 'Queue.fxml'.";
		assert startQueue != null : "fx:id=\"startQueue\" was not injected: check your FXML file 'Queue.fxml'.";
		assert stopQueue != null : "fx:id=\"stopQueue\" was not injected: check your FXML file 'Queue.fxml'.";
		assert columnStarttime != null : "fx:id=\"columnStarttime\" was not injected: check your FXML file 'Queue.fxml'.";
		assert queueActionsGridpane != null : "fx:id=\"queueActionsGridpane\" was not injected: check your FXML file 'Queue.fxml'.";

		// initialize your logic here: all @FXML variables will have been
		// injected

		queueActionsGridpane.add(numberOfUploads, 7, 1);
		queueActionsGridpane.add(uploadSpeed, 8, 1);

		columnId.setCellValueFactory(new ActiveCellValueFactory<Upload, Number>("id"));
		columnTitle.setCellValueFactory(new ActiveCellValueFactory<Upload, String>("title"));
		columnCategory.setCellValueFactory(new ActiveCellValueFactory<Upload, String>("category"));
		columnAccount.setCellValueFactory(new ActiveCellValueFactory<Upload, String>("name", Account.class));
		columnProgress.setCellValueFactory(new ActiveCellValueFactory<Upload, Upload>("this"));
		columnActions.setCellValueFactory(new ActiveCellValueFactory<Upload, Upload>("this"));
		columnStarttime.setCellValueFactory(new ActiveCellValueFactory<Upload, Object>("started"));
		columnStarttime.setCellFactory(new Callback<TableColumn<Upload, Object>, TableCell<Upload, Object>>() {

			@Override
			public TableCell<Upload, Object> call(final TableColumn<Upload, Object> param)
			{
				final TableCell<Upload, Object> cell = new TableCell<Upload, Object>() {

					@Override
					public void updateItem(final Object date, final boolean empty)
					{
						super.updateItem(date, empty);
						if (empty)
						{
							setGraphic(null);
							setContentDisplay(null);
						} else
						{
							if (date instanceof Date)
							{
								setText(new SimpleDateFormat("dd.MM.yyyy hh:mm").format((Date) date));
							} else if (date instanceof Timestamp)
							{
								setText(new SimpleDateFormat("dd.MM.yyyy hh:mm").format(((Timestamp) date).getTime()));
							}
						}
					}
				};
				return cell;
			}
		});

		columnProgress.setCellFactory(new Callback<TableColumn<Upload, Upload>, TableCell<Upload, Upload>>() {

			@Override
			public TableCell<Upload, Upload> call(final TableColumn<Upload, Upload> param)
			{
				final TableCell<Upload, Upload> cell = new TableCell<Upload, Upload>() {

					@Override
					public void updateItem(final Upload queue, final boolean empty)
					{
						super.updateItem(queue, empty);
						if (empty)
						{
							setGraphic(null);
							setContentDisplay(null);
						} else
						{
							final HBox hbox = new HBox(10);

							final ProgressIndicator progressIndicator = new ProgressIndicator(queue.getBoolean("archived") == true ? 100 : 0);
							progressIndicator.setId("queue-" + queue.getLongId());

							final Label label = new Label("");
							label.setId("queue-text-" + queue.getLongId());
							if (queue.getBoolean("archived"))
							{
								label.setText("http://youtu.be/" + queue.getString("videoid"));
								label.setOnMouseClicked(new EventHandler<MouseEvent>() {

									@Override
									public void handle(final MouseEvent mouseEvent)
									{

										if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && (mouseEvent.getClickCount() == 2)
												&& Desktop.isDesktopSupported())
										{
											try
											{
												Desktop.getDesktop().browse(new URI("http://youtu.be/" + queue.getString("videoid")));
											} catch (final URISyntaxException | IOException ignored)
											{}
										}

									}
								});
								progressIndicator.setProgress(100);
							} else if (queue.getBoolean("failed"))
							{
								label.setText("Fehlgeschlagen");
								progressIndicator.setProgress(0);
							}
							hbox.getChildren().addAll(progressIndicator, label);
							setGraphic(hbox);
							setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
						}
					}
				};
				return cell;
			}
		});

		columnActions.setCellFactory(new Callback<TableColumn<Upload, Upload>, TableCell<Upload, Upload>>() {

			@Override
			public TableCell<Upload, Upload> call(final TableColumn<Upload, Upload> param)
			{
				final TableCell<Upload, Upload> cell = new TableCell<Upload, Upload>() {

					@Override
					public void updateItem(final Upload item, final boolean empty)
					{
						super.updateItem(item, empty);
						if (empty)
						{
							setGraphic(null);
							setContentDisplay(null);
						} else
						{
							final Button btnRemove = new Button();
							btnRemove.setId("removeUpload");
							btnRemove.setOnAction(new EventHandler<ActionEvent>() {

								@Override
								public void handle(final ActionEvent event)
								{
									if ((item == null) || item.getBoolean("inprogress")) { return; }
									final MonologFX dialog = new MonologFX(MonologFX.Type.QUESTION);
									dialog.setTitleText("Upload entfernen");
									dialog.setMessage("Wirklich diesen Upload l�schen?");
									MonologFXButton yesButton = new MonologFXButton();
									yesButton.setType(MonologFXButton.Type.YES);
									yesButton.setLabel("Yes");
									MonologFXButton noButton = new MonologFXButton();
									noButton.setType(MonologFXButton.Type.NO);
									noButton.setLabel("No");
									dialog.addButton(yesButton);
									dialog.addButton(noButton);
									if ((item != null) && (dialog.showDialog() == MonologFXButton.Type.YES))
									{
										item.delete();
									}
								}
							});
							btnRemove.setDisable(item.getBoolean("inprogress"));

							final Button btnEdit = new Button();
							btnEdit.setId("editUpload");
							btnEdit.setOnAction(new EventHandler<ActionEvent>() {

								@Override
								public void handle(final ActionEvent event)
								{
									if ((item == null) || item.getBoolean("inprogress")) { return; }
									uploadViewModel.fromUpload(item);
								}

							});
							btnEdit.setDisable(item.getBoolean("inprogress") || item.getBoolean("archived"));

							final Button btnAbort = new Button("Abbrechen");
							btnAbort.setId("abortUpload");
							btnAbort.setOnAction(new EventHandler<ActionEvent>() {

								@Override
								public void handle(final ActionEvent arg0)
								{
									if ((item == null) || !item.getBoolean("inprogress")) { return; }
									final MonologFX dialog = new MonologFX(MonologFX.Type.QUESTION);
									dialog.setTitleText("Upload abbrechen");
									dialog.setMessage("Wirklich diesen Upload abbrechen?");
									if (dialog.showDialog() == MonologFXButton.Type.OK)
									{
										EventBus.publish(Uploader.ABORT, item);
									}
								}
							});
							btnAbort.setDisable(!item.getBoolean("inprogress") || item.getBoolean("archived"));

							final ToggleButton btnPauseOnFinish = new ToggleButton();
							btnPauseOnFinish.setId("pauseOnFinishQueue");
							btnPauseOnFinish.setOnAction(new EventHandler<ActionEvent>() {

								@Override
								public void handle(final ActionEvent arg0)
								{
									item.setBoolean("pauseOnFinish", btnPauseOnFinish.selectedProperty().get());
									item.saveIt();
								}
							});
							btnPauseOnFinish.selectedProperty().set(item.getBoolean("pauseOnFinish"));

							final HBox hbox = new HBox(3d);
							hbox.getChildren().addAll(btnRemove, btnEdit, btnAbort, btnPauseOnFinish);
							setGraphic(hbox);
							setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
						}
					}
				};
				return cell;
			}

		});

		queueTableview.setItems(FXCollections.observableArrayList(Model.findAll()));

		actionOnFinish.setItems(FXCollections.observableArrayList(new String[] { I18nHelper.message("queuefinishedlist.donothing"),
				I18nHelper.message("queuefinishedlist.closeapplication"), I18nHelper.message("queuefinishedlist.shutdown"),
				I18nHelper.message("queuefinishedlist.hibernate") }));
		actionOnFinish.getSelectionModel().selectFirst();

		// Bindings
		uploader.actionOnFinish.bind(actionOnFinish.getSelectionModel().selectedIndexProperty());
		startQueue.disableProperty().bind(uploader.inProgressProperty);
		stopQueue.disableProperty().bind(uploader.inProgressProperty.not());
		uploader.maxUploads.bind(numberOfUploads.valueProperty());
		throttle.maxBps.bind(uploadSpeed.valueProperty());

		queueTableview.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

		AnnotationProcessor.process(this);
	}

	@EventTopicSubscriber(topic = ModelEvents.MODEL_POST_SAVED)
	public void onAdded(final String topic, final Model model)
	{
		Platform.runLater(new Runnable() {

			@Override
			public void run()
			{
				if (model instanceof Upload)
				{
					if (!queueTableview.getItems().contains(model))
					{
						queueTableview.getItems().add(model);
					} else
					{
						queueTableview.getItems().set(queueTableview.getItems().indexOf(model), model);
						RefresherUtil.refresh(queueTableview, queueTableview.getItems());
					}
				}
			}
		});
	}

	@EventTopicSubscriber(topic = Uploader.PROGRESS)
	public void onProgress(final String topic, final UploadProgress uploadProgress)
	{
		Platform.runLater(new Runnable() {

			@Override
			public void run()
			{
				final ProgressIndicator progressIndicator = (ProgressIndicator) queueTableview.getScene()
						.lookup("#queue-" + uploadProgress.getQueue().getLongId());
				if (progressIndicator == null) { return; }
				progressIndicator.setProgress(uploadProgress.getTotalBytesUploaded() / uploadProgress.getFileSize());

				final Label label = (Label) queueTableview.getScene().lookup("#queue-text-" + uploadProgress.getQueue().getLongId());
				label.setText(String.format("%d MB/%d MB %dkbps", (int) (uploadProgress.getTotalBytesUploaded() / 1048576),
											(int) (uploadProgress.getFileSize() / 1048576),
											(int) (uploadProgress.getDiffBytes() / uploadProgress.getDiffTime())));
				System.out.println((int) (uploadProgress.getDiffBytes() / uploadProgress.getDiffTime()));

			}
		});
	}

	@EventTopicSubscriber(topic = ModelEvents.MODEL_PRE_REMOVED)
	public void onRemoved(final String topic, final Model model)
	{
		Platform.runLater(new Runnable() {

			@Override
			public void run()
			{
				if (model instanceof Upload)
				{
					queueTableview.getItems().remove(model);
				}
			}
		});
	}

	// Handler for Button[fx:id="startQueue"] onAction
	public void startQueue(final ActionEvent event)
	{
		final MonologFX dialog = new MonologFX(MonologFX.Type.ACCEPT);
		dialog.setTitleText(I18nHelper.message("youtube.confirmdialog.title"));
		dialog.setMessage(I18nHelper.message("upload.confirmdialog.message"));
		MonologFXButton yesButton = new MonologFXButton();
		yesButton.setType(MonologFXButton.Type.YES);
		yesButton.setLabel("Yes");
		MonologFXButton noButton = new MonologFXButton();
		noButton.setType(MonologFXButton.Type.NO);
		noButton.setLabel("No");
		dialog.addButton(yesButton);
		dialog.addButton(noButton);
		if (dialog.showDialog() == MonologFXButton.Type.YES)
		{
			uploader.start();
		}
	}

	// Handler for Button[fx:id="stopQueue"] onAction
	public void stopQueue(final ActionEvent event)
	{
		uploader.stop();
	}
}
