/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import name.antonsmirnov.javafx.dialog.Dialog;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.util.ActiveCellValueFactory;
import org.chaosfisch.youtubeuploader.I18nHelper;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.ModelEvents;
import org.chaosfisch.youtubeuploader.models.Queue;
import org.chaosfisch.youtubeuploader.services.uploader.UploadProgress;
import org.chaosfisch.youtubeuploader.services.uploader.Uploader;
import org.javalite.activejdbc.Model;

import com.google.inject.Inject;

public class QueueController implements Initializable
{

	@FXML// fx:id="actionOnFinish"
	private ChoiceBox<String>				actionOnFinish;

	@FXML// fx:id="columnAccount"
	private TableColumn<Queue, String>		columnAccount;

	@FXML// fx:id="columnActions"
	private TableColumn<Queue, Queue>		columnActions;

	@FXML// fx:id="columnCategory"
	private TableColumn<Queue, String>		columnCategory;

	@FXML// fx:id="columnId"
	private TableColumn<Queue, Number>		columnId;

	@FXML// fx:id="columnProgress"
	private TableColumn<Queue, Queue>		columnProgress;

	@FXML// fx:id="columnTitle"
	private TableColumn<Queue, String>		columnTitle;

	@FXML// fx:id="columnStarttime"
	private TableColumn<Queue, Timestamp>	columnStarttime;

	@FXML// fx:id="queueTableview"
	private TableView<Model>				queueTableview;

	@FXML// fx:id="startQueue"
	private Button							startQueue;

	@FXML// fx:id="stopQueue"
	private Button							stopQueue;

	@Inject Uploader						uploader;

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

		// initialize your logic here: all @FXML variables will have been
		// injected

		columnId.setCellValueFactory(new ActiveCellValueFactory<Queue, Number>("id"));
		columnTitle.setCellValueFactory(new ActiveCellValueFactory<Queue, String>("title"));
		columnCategory.setCellValueFactory(new ActiveCellValueFactory<Queue, String>("category"));
		columnAccount.setCellValueFactory(new ActiveCellValueFactory<Queue, String>("name", Account.class));
		columnProgress.setCellValueFactory(new ActiveCellValueFactory<Queue, Queue>("this"));
		columnActions.setCellValueFactory(new ActiveCellValueFactory<Queue, Queue>("this"));
		columnStarttime.setCellValueFactory(new ActiveCellValueFactory<Queue, Timestamp>("started"));
		columnStarttime.setCellFactory(new Callback<TableColumn<Queue, Timestamp>, TableCell<Queue, Timestamp>>() {

			@Override
			public TableCell<Queue, Timestamp> call(final TableColumn<Queue, Timestamp> param)
			{
				final TableCell<Queue, Timestamp> cell = new TableCell<Queue, Timestamp>() {

					@Override
					public void updateItem(final Timestamp date, final boolean empty)
					{
						super.updateItem(date, empty);
						if (empty)
						{
							setGraphic(null);
							setContentDisplay(null);
						} else
						{
							setText(new SimpleDateFormat("dd.MM.yyyy hh:mm").format(date.getTime()));
						}
					}
				};
				return cell;
			}
		});

		columnProgress.setCellFactory(new Callback<TableColumn<Queue, Queue>, TableCell<Queue, Queue>>() {

			@Override
			public TableCell<Queue, Queue> call(final TableColumn<Queue, Queue> param)
			{
				final TableCell<Queue, Queue> cell = new TableCell<Queue, Queue>() {

					@Override
					public void updateItem(final Queue queue, final boolean empty)
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

		columnActions.setCellFactory(new Callback<TableColumn<Queue, Queue>, TableCell<Queue, Queue>>() {

			@Override
			public TableCell<Queue, Queue> call(final TableColumn<Queue, Queue> param)
			{
				final TableCell<Queue, Queue> cell = new TableCell<Queue, Queue>() {

					@Override
					public void updateItem(final Queue item, final boolean empty)
					{
						super.updateItem(item, empty);
						if (empty)
						{
							setGraphic(null);
							setContentDisplay(null);
						} else
						{
							final Button btnRemove = new Button();
							btnRemove.setId("removeQueue");
							btnRemove.setOnAction(new EventHandler<ActionEvent>() {

								@Override
								public void handle(final ActionEvent event)
								{
									param.getTableView().getSelectionModel().select(getIndex());
									if (item != null)
									{
										item.delete();
									}
								}

							});

							final Button btnEdit = new Button();
							btnEdit.setId("editQueue");
							btnEdit.setOnAction(new EventHandler<ActionEvent>() {

								@Override
								public void handle(final ActionEvent event)
								{
								}

							});

							final Button btnAbort = new Button("Abbrechen");
							btnAbort.setId("abortQueue");
							btnAbort.setOnAction(new EventHandler<ActionEvent>() {

								@Override
								public void handle(final ActionEvent arg0)
								{
								}
							});

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

		queueTableview.setItems(FXCollections.observableArrayList(Queue.findAll()));

		actionOnFinish.setItems(FXCollections.observableArrayList(new String[] { I18nHelper.message("queuefinishedlist.donothing"),
				I18nHelper.message("queuefinishedlist.closeapplication"), I18nHelper.message("queuefinishedlist.shutdown"),
				I18nHelper.message("queuefinishedlist.hibernate") }));
		actionOnFinish.getSelectionModel().selectFirst();

		stopQueue.setDisable(true);

		queueTableview.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		AnnotationProcessor.process(this);
	}

	@EventTopicSubscriber(topic = ModelEvents.MODEL_POST_ADDED)
	public void onAdded(final String topic, final Model model)
	{
		Platform.runLater(new Runnable() {

			@Override
			public void run()
			{
				if (model instanceof Queue)
				{
					queueTableview.getItems().add(model);
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
				final ProgressIndicator progressIndicator = (ProgressIndicator) queueTableview.getScene().lookup("#queue-"
						+ uploadProgress.getQueue().getLongId());
				progressIndicator.setProgress(uploadProgress.getTotalBytesUploaded() / uploadProgress.getFileSize());

				final Label label = (Label) queueTableview.getScene().lookup("#queue-text-" + uploadProgress.getQueue().getLongId());
				label.setText(String.format("%d MB/%d MB %dkbps",
											(int) (uploadProgress.getTotalBytesUploaded() / 1048576),
											(int) (uploadProgress.getFileSize() / 1048576),
											(int) (uploadProgress.getDiffBytes() / uploadProgress.getDiffTime())));

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
				if (model instanceof Queue)
				{
					queueTableview.getItems().remove(model);
				}
			}
		});
	}

	// Handler for Button[fx:id="startQueue"] onAction
	public void startQueue(final ActionEvent event)
	{
		final Dialog dialog = Dialog.buildConfirmation(	I18nHelper.message("youtube.confirmdialog.title"),
														I18nHelper.message("upload.confirmdialog.message"))
				.addYesButton(new EventHandler<Event>() {

					@Override
					public void handle(final Event event)
					{
						uploader.start();
						toggleQueueButtons();
					}
				})
				.addCancelButton(new EventHandler<Event>() {

					@Override
					public void handle(final Event event)
					{
						uploader.stop();
						toggleQueueButtons();
					}
				})
				.build();

		dialog.show();

	}

	// Handler for Button[fx:id="stopQueue"] onAction
	public void stopQueue(final ActionEvent event)
	{
		toggleQueueButtons();
	}

	private void toggleQueueButtons()
	{
		startQueue.setDisable(!startQueue.isDisabled());
		stopQueue.setDisable(!stopQueue.isDisabled());
	}
}