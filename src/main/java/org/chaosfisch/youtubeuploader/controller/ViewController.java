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

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import org.chaosfisch.util.InputDialog;
import org.chaosfisch.youtubeuploader.I18nHelper;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.models.Template;
import org.chaosfisch.youtubeuploader.services.youtube.spi.PlaylistService;
import org.javalite.activejdbc.Model;

import com.google.inject.Inject;

public class ViewController implements Initializable
{

	@FXML// fx:id="content_pane"
	private AnchorPane					content_pane;

	@FXML// fx:id="grid_pane"
	private GridPane					grid_pane;

	@FXML// fx:id="menuAddPlaylist"
	private MenuItem					menuAddPlaylist;

	@FXML// fx:id="menuAddTemplate"
	private MenuItem					menuAddTemplate;

	@FXML// fx:id="menuClose"
	private MenuItem					menuClose;

	@FXML// fx:id="menuLogfile"
	private MenuItem					menuLogfile;
	@FXML// fx:id="menuOpenFile"
	private MenuItem					menuOpenFile;

	@Inject private PlaylistService		playlistService;
	@Inject private UploadController	uploadController;

	public static final Template		standardTemplate	= Template.create(	"embed", true, "mobile", true, "commentvote", true, "rate", true,
																				"comment", 0, "category", 0, "visibility", 0, "videoresponse", 1,
																				"license", 0);

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(final URL fxmlFileLocation, final ResourceBundle resources)
	{
		assert content_pane != null : "fx:id=\"content_pane\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert grid_pane != null : "fx:id=\"grid_pane\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuAddPlaylist != null : "fx:id=\"menuAddPlaylist\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuAddTemplate != null : "fx:id=\"menuAddTemplate\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuClose != null : "fx:id=\"menuClose\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuLogfile != null : "fx:id=\"menuLogfile\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuOpenFile != null : "fx:id=\"menuOpenFile\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";

		// initialize your logic here: all @FXML variables will have been
		// injected
	}

	// Handler for AnchorPane[fx:id="content_pane"] onDragDropped
	public void fileDragDropped(final DragEvent event)
	{
		/* data dropped */
		final Dragboard db = event.getDragboard();

		if (db.hasFiles())
		{
			uploadController.addUploadFiles(db.getFiles());
			event.setDropCompleted(true);
		} else
		{
			event.setDropCompleted(false);
		}

		event.consume();
	}

	// Handler for AnchorPane[fx:id="content_pane"] onDragOver
	public void fileDragOver(final DragEvent event)
	{
		/* data is dragged over the target */
		final Dragboard db = event.getDragboard();
		if (db.hasFiles())
		{
			event.acceptTransferModes(TransferMode.COPY);
		}

		event.consume();
	}

	// Handler for MenuItem[fx:id="menuAddPlaylist"] onAction
	public void menuAddPlaylist(final ActionEvent event)
	{
		// PLAYLIST ADD
		final TextField title = new TextField();
		final CheckBox playlistPrivate = new CheckBox();
		final TextArea summary = new TextArea();
		final ChoiceBox<Model> accounts = new ChoiceBox<Model>();
		accounts.setItems(FXCollections.observableList(Model.find("type = ?", Account.Type.YOUTUBE.name())));
		accounts.getSelectionModel().selectFirst();

		final Object[] message = { I18nHelper.message("playlistDialog.playlistLabel"), title, I18nHelper.message("playlistDialog.descriptionLabel"),
				summary, I18nHelper.message("playlistDialog.playlistPrivate"), playlistPrivate, I18nHelper.message("playlistDialog.playlistAccount"),
				accounts };
		final InputDialog myDialog = new InputDialog(I18nHelper.message("playlistDialog.addPlaylistLabel"), message);

		myDialog.setCallback(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event)
			{
				if (!title.getText().isEmpty() && !accounts.getSelectionModel().isEmpty())
				{
					playlistService.addYoutubePlaylist((Playlist) Model.create(	"title", title.getText(), "summary", summary.getText(),
																					"private",
																					playlistPrivate.isSelected(), "account_id", accounts.getValue()
																							.getLongId()));
					myDialog.close();
				}
			}
		});
	}

	// Handler for MenuItem[fx:id="menuAddTemplate"] onAction
	public void menuAddTemplate(final ActionEvent event)
	{
		// PRESET ADD
		final TextField textfield = new TextField();
		final Object[] message = { I18nHelper.message("templateDialog.templateLabel"), textfield };

		final InputDialog myDialog = new InputDialog(I18nHelper.message("templateDialog.addTemplateLabel"), message);

		myDialog.setCallback(new EventHandler<ActionEvent>() {

			@Override
			public void handle(final ActionEvent event)
			{
				if (!textfield.getText().isEmpty())
				{

					final Template preset = Model.create();
					preset.copyFrom(standardTemplate);
					preset.setString("name", textfield.getText());
					preset.save();
					myDialog.close();
				}
			}
		});
	}

	// Handler for MenuItem[fx:id="menuClose"] onAction
	public void menuClose(final ActionEvent event)
	{
		((Stage) content_pane.getScene().getWindow()).hide();
	}

	// Handler for MenuItem[fx:id="menuOpenFile"] onAction
	public void menuOpen(final ActionEvent event)
	{
		uploadController.openFiles(event);
	}

	// Handler for MenuItem[fx:id="menuLogfile"] onAction
	public void sendLogfiles(final ActionEvent event)
	{
		// handle the event here
	}

}
