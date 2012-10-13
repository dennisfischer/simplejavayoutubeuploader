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

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.google.atom.AtomCategory;
import org.chaosfisch.util.InputDialog;
import org.chaosfisch.youtubeuploader.I18nHelper;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.ModelEvents;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.models.Preset;
import org.chaosfisch.youtubeuploader.services.PlaylistService;
import org.chaosfisch.youtubeuploader.services.youtube.spi.CategoryService;
import org.javalite.activejdbc.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class ViewController implements Initializable
{

	@FXML// fx:id="abortQueue"
	private Button						abortQueue;

	@FXML// fx:id="accountList"
	private ChoiceBox<Model>			accountList;

	@FXML// fx:id="actionOnFinish"
	private ChoiceBox<String>			actionOnFinish;

	@FXML// fx:id="addUpload"
	private Button						addUpload;

	@Inject private CategoryService		categoryService;

	@FXML// fx:id="content_pane"
	private AnchorPane					content_pane;

	@Inject private DirectoryChooser	directoryChooser;

	@FXML// fx:id="editQueue"
	private Button						editQueue;

	@Inject private FileChooser			fileChooser;

	@FXML// fx:id="grid_pane"
	private GridPane					grid_pane;

	@FXML// fx:id="loading_pane"
	private GridPane					loading_pane;

	private final Logger				logger			= LoggerFactory.getLogger(getClass());

	@FXML// fx:id="menuAbout"
	private MenuItem					menuAbout;

	@FXML// fx:id="menuAddAccount"
	private MenuItem					menuAddAccount;

	@FXML// fx:id="menuAddPlaylist"
	private MenuItem					menuAddPlaylist;

	@FXML// fx:id="menuAddPreset"
	private MenuItem					menuAddPreset;

	@FXML// fx:id="menuChangelog"
	private MenuItem					menuChangelog;

	@FXML// fx:id="menuClose"
	private MenuItem					menuClose;

	@FXML// fx:id="menuExportAccount"
	private MenuItem					menuExportAccount;

	@FXML// fx:id="menuExportPreset"
	private MenuItem					menuExportPreset;

	@FXML// fx:id="menuExportQueue"
	private MenuItem					menuExportQueue;

	@FXML// fx:id="menuImportAccount"
	private MenuItem					menuImportAccount;

	@FXML// fx:id="menuImportPreset"
	private MenuItem					menuImportPreset;

	@FXML// fx:id="menuImportQueue"
	private MenuItem					menuImportQueue;

	@FXML// fx:id="menuLogfile"
	private MenuItem					menuLogfile;

	@FXML// fx:id="menuOpenFile"
	private MenuItem					menuOpenFile;

	@FXML// fx:id="menuPlugins"
	private MenuItem					menuPlugins;

	@FXML// fx:id="menuWiki"
	private MenuItem					menuWiki;

	@FXML// fx:id="openDefaultdir"
	private Button						openDefaultdir;

	@FXML// fx:id="openEnddir"
	private Button						openEnddir;

	@FXML// fx:id="openFiles"
	private Button						openFiles;

	@FXML// fx:id="playlistList"
	private ChoiceBox<Model>			playlistList;

	@FXML// fx:id="presetList"
	private ChoiceBox<Model>			presetList;

	@FXML// fx:id="queueBottom"
	private Button						queueBottom;

	@FXML// fx:id="queueDown"
	private Button						queueDown;

	@FXML// fx:id="queueTop"
	private Button						queueTop;

	@FXML// fx:id="queueUp"
	private Button						queueUp;

	@FXML// fx:id="queueView"
	private ChoiceBox<String>			queueView;

	@FXML// fx:id="refreshPlaylists"
	private Button						refreshPlaylists;

	@FXML// fx:id="removePreset"
	private Button						removePreset;

	@FXML// fx:id="removeQueue"
	private Button						removeQueue;

	@FXML// fx:id="resetUpload"
	private Button						resetUpload;

	@FXML// fx:id="savePreset"
	private Button						savePreset;

	@FXML// fx:id="startQueue"
	private Button						startQueue;

	@FXML// fx:id="stopQueue"
	private Button						stopQueue;

	@FXML// fx:id="uploadCategory"
	private ChoiceBox<AtomCategory>		uploadCategory;

	@FXML// fx:id="uploadComment"
	private ChoiceBox<String>			uploadComment;

	@FXML// fx:id="uploadCommentvote"
	private CheckBox					uploadCommentvote;

	@FXML// fx:id="uploadDefaultdir"
	private TextField					uploadDefaultdir;

	@FXML// fx:id="uploadDescription"
	private TextArea					uploadDescription;

	@FXML// fx:id="uploadEmbed"
	private CheckBox					uploadEmbed;

	@FXML// fx:id="uploadEnddir"
	private TextField					uploadEnddir;

	@FXML// fx:id="uploadFile"
	private ChoiceBox<File>				uploadFile;

	@FXML// fx:id="uploadLicense"
	private ChoiceBox<?>				uploadLicense;

	@FXML// fx:id="uploadMobile"
	private CheckBox					uploadMobile;

	@FXML// fx:id="uploadRate"
	private CheckBox					uploadRate;

	@FXML// fx:id="uploadTags"
	private TextArea					uploadTags;

	@FXML// fx:id="uploadTitle"
	private TextField					uploadTitle;

	@FXML// fx:id="uploadVideoresponse"
	private ChoiceBox<String>			uploadVideoresponse;

	@FXML// fx:id="uploadVisibility"
	private ChoiceBox<String>			uploadVisibility;

	@FXML// fx:id="x1"
	private TitledPane					x1;

	@Inject PlaylistService				playlistService;

	private final ObservableList<Model>	accountItems	= FXCollections.observableArrayList();

	protected ObservableList<Model>		playlistItems	= FXCollections.observableArrayList();

	private final ObservableList<Model>	presetItems		= FXCollections.observableArrayList();

	// Handler for Button[fx:id="addUpload"] onAction
	public void addUpload(final ActionEvent event)
	{
		// handle the event here
	}

	@SuppressWarnings("unchecked")
	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(final URL fxmlFileLocation, final ResourceBundle resources)
	{
		assert abortQueue != null : "fx:id=\"abortQueue\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert accountList != null : "fx:id=\"accountList\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert actionOnFinish != null : "fx:id=\"actionOnFinish\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert addUpload != null : "fx:id=\"addUpload\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert content_pane != null : "fx:id=\"content_pane\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert editQueue != null : "fx:id=\"editQueue\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert grid_pane != null : "fx:id=\"grid_pane\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert loading_pane != null : "fx:id=\"loading_pane\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuAbout != null : "fx:id=\"menuAbout\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuAddAccount != null : "fx:id=\"menuAddAccount\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuAddPlaylist != null : "fx:id=\"menuAddPlaylist\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuAddPreset != null : "fx:id=\"menuAddPreset\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuChangelog != null : "fx:id=\"menuChangelog\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuClose != null : "fx:id=\"menuClose\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuExportAccount != null : "fx:id=\"menuExportAccount\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuExportPreset != null : "fx:id=\"menuExportPreset\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuExportQueue != null : "fx:id=\"menuExportQueue\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuImportAccount != null : "fx:id=\"menuImportAccount\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuImportPreset != null : "fx:id=\"menuImportPreset\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuImportQueue != null : "fx:id=\"menuImportQueue\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuLogfile != null : "fx:id=\"menuLogfile\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuOpenFile != null : "fx:id=\"menuOpenFile\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuPlugins != null : "fx:id=\"menuPlugins\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuWiki != null : "fx:id=\"menuWiki\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert openDefaultdir != null : "fx:id=\"openDefaultdir\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert openEnddir != null : "fx:id=\"openEnddir\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert openFiles != null : "fx:id=\"openFiles\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert playlistList != null : "fx:id=\"playlistList\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert presetList != null : "fx:id=\"presetList\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert queueBottom != null : "fx:id=\"queueBottom\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert queueDown != null : "fx:id=\"queueDown\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert queueTop != null : "fx:id=\"queueTop\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert queueUp != null : "fx:id=\"queueUp\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert queueView != null : "fx:id=\"queueView\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert refreshPlaylists != null : "fx:id=\"refreshPlaylists\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert removePreset != null : "fx:id=\"removePreset\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert removeQueue != null : "fx:id=\"removeQueue\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert resetUpload != null : "fx:id=\"resetUpload\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert savePreset != null : "fx:id=\"savePreset\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert startQueue != null : "fx:id=\"startQueue\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert stopQueue != null : "fx:id=\"stopQueue\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert uploadCategory != null : "fx:id=\"uploadCategory\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert uploadComment != null : "fx:id=\"uploadComment\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert uploadCommentvote != null : "fx:id=\"uploadCommentvote\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert uploadDefaultdir != null : "fx:id=\"uploadDefaultdir\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert uploadDescription != null : "fx:id=\"uploadDescription\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert uploadEmbed != null : "fx:id=\"uploadEmbed\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert uploadEnddir != null : "fx:id=\"uploadEnddir\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert uploadFile != null : "fx:id=\"uploadFile\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert uploadLicense != null : "fx:id=\"uploadLicense\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert uploadMobile != null : "fx:id=\"uploadMobile\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert uploadRate != null : "fx:id=\"uploadRate\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert uploadTags != null : "fx:id=\"uploadTags\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert uploadTitle != null : "fx:id=\"uploadTitle\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert uploadVideoresponse != null : "fx:id=\"uploadVideoresponse\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert uploadVisibility != null : "fx:id=\"uploadVisibility\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert x1 != null : "fx:id=\"x1\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";

		// initialize your logic here: all @FXML variables will have been
		// injected

		AnnotationProcessor.process(this);

		accountItems.addAll(Account.where("type = ?", Account.Type.YOUTUBE.name()).include(Playlist.class));
		accountList.setItems(accountItems);
		playlistList.setItems(playlistItems);
		accountList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Model>() {

			@Override
			public void changed(final ObservableValue<? extends Model> observable, final Model oldValue, final Model newValue)
			{
				playlistList.getItems().clear();

				if (newValue != null && newValue.get("playlists") != null)
				{
					playlistItems.clear();
					playlistItems.addAll((Collection<Model>) newValue.get("playlists"));
					playlistList.getSelectionModel().selectFirst();
				}
			}
		});
		accountList.getSelectionModel().selectFirst();

		uploadCategory.setItems(FXCollections.observableList(categoryService.load()));
		uploadCategory.getSelectionModel().selectFirst();

		presetItems.addAll(Preset.findAll().include(Account.class, Playlist.class));
		presetList.setItems(presetItems);
		presetList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Model>() {

			@Override
			public void changed(final ObservableValue<? extends Model> observable, final Model oldValue, final Model newValue)
			{
				Account account;
				if ((newValue != null) && ((account = newValue.parent(Account.class)) != null))
				{
					accountList.getSelectionModel().select(accountList.getItems().indexOf(account));
					if (newValue.parent(Playlist.class) != null)
					{
						playlistList.getSelectionModel().select(newValue.parent(Playlist.class));
					}
				}
			}
		});
		presetList.getSelectionModel().selectFirst();
	}

	@EventTopicSubscriber(topic = ModelEvents.MODEL_POST_REMOVED)
	public void onRemoved(final String topic, final Model model)
	{
		Platform.runLater(new Runnable() {

			@Override
			public void run()
			{
				if (model instanceof Account)
				{
					accountItems.remove(model);
					if (accountList.getSelectionModel().isEmpty())
					{
						accountList.getSelectionModel().selectFirst();
					}
				} else if (model instanceof Preset)
				{
					presetItems.remove(model);
					if (presetList.getSelectionModel().isEmpty())
					{
						presetList.getSelectionModel().selectFirst();
					}
				} else if (model instanceof Playlist)
				{
					playlistItems.remove(model);
					if (playlistList.getSelectionModel().isEmpty())
					{
						playlistList.getSelectionModel().selectFirst();
					}
				}
			}
		});
	}

	@EventTopicSubscriber(topic = ModelEvents.MODEL_POST_ADDED)
	public void onAdded(final String topic, final Model model)
	{
		Platform.runLater(new Runnable() {

			@Override
			public void run()
			{
				if (model instanceof Account && model.get("type").equals(Account.Type.YOUTUBE.name()))
				{
					accountItems.add(model);
					if (accountList.getValue() == null)
					{
						accountList.getSelectionModel().selectFirst();
					}
				} else if (model instanceof Preset)
				{
					presetItems.add(model);
					if (presetList.getValue() == null)
					{
						presetList.getSelectionModel().selectFirst();
					}
				} else if (model instanceof Playlist && model.parent(Account.class).equals(accountList.getValue()))
				{
					playlistItems.add(model);
					if (playlistList.getValue() == null)
					{
						playlistList.getSelectionModel().selectFirst();
					}
				}
			}
		});
	}

	// Handler for Button[fx:id="openDefaultdir"] onAction
	public void openDefaultdir(final ActionEvent event)
	{
		final File directory = directoryChooser.showDialog(null);
		if (directory != null)
		{
			uploadDefaultdir.setText(directory.getAbsolutePath());
		}
	}

	// Handler for Button[fx:id="openEnddir"] onAction
	public void openEnddir(final ActionEvent event)
	{
		final File directory = directoryChooser.showDialog(null);
		if (directory != null)
		{
			uploadEnddir.setText(directory.getAbsolutePath());
		}
	}

	// Handler for Button[fx:id="openFiles"] onAction
	public void openFiles(final ActionEvent event)
	{
		final List<File> files = fileChooser.showOpenMultipleDialog(null);
		if (files != null)
		{
			uploadFile.setItems(FXCollections.observableList(files));
			uploadFile.getSelectionModel().selectFirst();
		}
	}

	// Handler for Button[fx:id="refreshPlaylists"] onAction
	public void refreshPlaylists(final ActionEvent event)
	{
		// handle the event here
	}

	// Handler for Button[fx:id="removePreset"] onAction
	public void removePreset(final ActionEvent event)
	{
		if (presetList.getValue() != null)
		{
			presetList.getValue().delete();
		}
	}

	// Handler for Button[fx:id="resetUpload"] onAction
	public void resetUpload(final ActionEvent event)
	{
		// handle the event here
	}

	// Handler for Button[fx:id="savePreset"] onAction
	public void savePreset(final ActionEvent event)
	{
		// handle the event here
	}

	// Handler for MenuItem[fx:id="menuAddPlaylist"] onAction
	public void menuAddPlaylist(ActionEvent event)
	{
		// PLAYLIST ADD
		final TextField title = new TextField();
		final CheckBox playlistPrivate = new CheckBox();
		final TextArea summary = new TextArea();
		final ChoiceBox<Model> accounts = new ChoiceBox<Model>();
		accounts.setItems(accountItems);
		accounts.getSelectionModel().selectFirst();

		final Object[] message = { I18nHelper.message("playlistDialog.playlistLabel"), title, I18nHelper.message("playlistDialog.descriptionLabel"),
				summary, I18nHelper.message("playlistDialog.playlistPrivate"), playlistPrivate, I18nHelper.message("playlistDialog.playlistAccount"),
				accounts };
		final InputDialog myDialog = new InputDialog(I18nHelper.message("playlistDialog.addPlaylistLabel"), message);

		myDialog.setCallback(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event)
			{
				if (!title.getText().isEmpty() && !accounts.getSelectionModel().isEmpty())
				{
					playlistService.addYoutubePlaylist((Playlist) Playlist.create("title", title.getText(), "summary", summary.getText(), "private",
							playlistPrivate.isSelected(), "account_id", accounts.getValue().getLongId()));
					myDialog.close();
				}
			}
		});
	}

	// Handler for MenuItem[fx:id="menuAddPreset"] onAction
	public void menuAddPreset(ActionEvent event)
	{
		// PRESET ADD
		final TextField textfield = new TextField();
		final Object[] message = { I18nHelper.message("presetDialog.presetLabel"), textfield };

		final InputDialog myDialog = new InputDialog(I18nHelper.message("presetDialog.addPresetLabel"), message);

		myDialog.setCallback(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event)
			{
				if (!textfield.getText().isEmpty())
				{
					Preset.createIt("name", textfield.getText());
					myDialog.close();
				}
			}
		});
	}
}
