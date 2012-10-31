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

import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import name.antonsmirnov.javafx.dialog.Dialog;

import org.chaosfisch.youtubeuploader.I18nHelper;
import org.chaosfisch.youtubeuploader.models.Queue;
import org.chaosfisch.youtubeuploader.services.uploader.Uploader;
import org.javalite.activejdbc.Model;

import com.google.inject.Inject;

public class QueueController implements Initializable
{

	@FXML// fx:id="abortQueue"
	private Button				abortQueue;	// Value injected by FXMLLoader

	@FXML// fx:id="actionOnFinish"
	private ChoiceBox<String>	actionOnFinish; // Value injected by FXMLLoader

	@FXML// fx:id="editQueue"
	private Button				editQueue;		// Value injected by FXMLLoader

	@FXML// fx:id="queueBottom"
	private Button				queueBottom;	// Value injected by FXMLLoader

	@FXML// fx:id="queueDown"
	private Button				queueDown;		// Value injected by FXMLLoader

	@FXML// fx:id="queueTop"
	private Button				queueTop;		// Value injected by FXMLLoader

	@FXML// fx:id="queueUp"
	private Button				queueUp;		// Value injected by FXMLLoader
	@FXML// fx:id="queueTableview"
	private TableView<Model>	queueTableview; // Value injected by FXMLLoader
	@FXML// fx:id="removeQueue"
	private Button				removeQueue;	// Value injected by FXMLLoader

	@FXML// fx:id="startQueue"
	private Button				startQueue;	// Value injected by FXMLLoader

	@FXML// fx:id="stopQueue"
	private Button				stopQueue;		// Value injected by FXMLLoader

	@Inject private Uploader	uploader;

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(final URL fxmlFileLocation, final ResourceBundle resources)
	{
		assert abortQueue != null : "fx:id=\"abortQueue\" was not injected: check your FXML file 'Queue.fxml'.";
		assert actionOnFinish != null : "fx:id=\"actionOnFinish\" was not injected: check your FXML file 'Queue.fxml'.";
		assert editQueue != null : "fx:id=\"editQueue\" was not injected: check your FXML file 'Queue.fxml'.";
		assert queueBottom != null : "fx:id=\"queueBottom\" was not injected: check your FXML file 'Queue.fxml'.";
		assert queueDown != null : "fx:id=\"queueDown\" was not injected: check your FXML file 'Queue.fxml'.";
		assert queueTop != null : "fx:id=\"queueTop\" was not injected: check your FXML file 'Queue.fxml'.";
		assert queueUp != null : "fx:id=\"queueUp\" was not injected: check your FXML file 'Queue.fxml'.";
		assert removeQueue != null : "fx:id=\"removeQueue\" was not injected: check your FXML file 'Queue.fxml'.";
		assert startQueue != null : "fx:id=\"startQueue\" was not injected: check your FXML file 'Queue.fxml'.";
		assert stopQueue != null : "fx:id=\"stopQueue\" was not injected: check your FXML file 'Queue.fxml'.";

		// initialize your logic here: all @FXML variables will have been
		// injected

		@SuppressWarnings("unchecked")
		final List<String> metamodel = Queue.getMetaModel().getAttributeNamesSkip("account_id", "playlists_id", "claim", "monetize", "asset",
				"monetizeOverlay", "monetizeTrueview", "monetizeProduct", "monetizeInstream", "claimpolicy", "claimtype", "archived", "tveidr",
				"tvisan", "tvnotes", "tvdescription", "webtitle", "webdescription", "webnotes", "episodenb", "seasonnb", "episodetitle", "failed",
				"locked", "movieIsan", "movieEIDR", "movieTitle", "movieDescription", "movieId", "movieNotes", "movietmsid", "partnerOverlay",
				"partnerInstream", "partnerTrueview", "partnerProduct", "created_at", "updated_at", "sequence", "showtitle", "tvid", "webid",
				"tvtmsid");

		Collections.sort(metamodel, new Comparator<String>() {

			@Override
			public int compare(final String arg0, final String arg1)
			{
				if (arg0.equals("id")) { return -1; }
				if (arg1.equals("id")) { return 1; }
				return arg0.compareToIgnoreCase(arg1);
			}
		});
		for (final String model : metamodel)
		{
			final TableColumn<Model, Object> tableColumn = new TableColumn<Model, Object>(I18nHelper.message("queuetable." + model));
			tableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Model, Object>, ObservableValue<Object>>() {

				@Override
				public ObservableValue<Object> call(final CellDataFeatures<Model, Object> param)
				{
					return new ReadOnlyObjectWrapper<Object>(param.getValue().get(model));
				}
			});

			queueTableview.getColumns().add(tableColumn);

		}
		queueTableview.autosize();

		queueTableview.setItems(FXCollections.observableArrayList(Queue.findAll()));

		actionOnFinish.setItems(FXCollections.observableArrayList(new String[] { I18nHelper.message("queuefinishedlist.donothing"),
				I18nHelper.message("queuefinishedlist.closeapplication"), I18nHelper.message("queuefinishedlist.shutdown"),
				I18nHelper.message("queuefinishedlist.hibernate") }));
		actionOnFinish.getSelectionModel().selectFirst();

		stopQueue.setDisable(true);
	}

	// Handler for Button[fx:id="startQueue"] onAction
	public void startQueue(final ActionEvent event)
	{
		final Dialog dialog = Dialog
				.buildConfirmation(I18nHelper.message("youtube.confirmdialog.title"), I18nHelper.message("upload.confirmdialog.message"))
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