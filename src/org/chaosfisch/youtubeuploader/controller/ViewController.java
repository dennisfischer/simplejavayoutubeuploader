/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.controller;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
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

import org.apache.log4j.Logger;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.google.atom.Category;
import org.chaosfisch.util.logger.InjectLogger;
import org.chaosfisch.youtubeuploader.dao.spi.AccountDao;
import org.chaosfisch.youtubeuploader.dao.spi.PlaylistDao;
import org.chaosfisch.youtubeuploader.dao.spi.PresetDao;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.models.Preset;
import org.chaosfisch.youtubeuploader.services.youtube.spi.CategoryService;

import com.google.inject.Inject;

public class ViewController implements Initializable
{

	@FXML
	// fx:id="abortQueue"
	private Button				abortQueue;

	@FXML
	// fx:id="accountList"
	private ChoiceBox<Account>	accountList;

	@FXML
	// fx:id="actionOnFinish"
	private ChoiceBox<String>	actionOnFinish;

	@FXML
	// fx:id="addUpload"
	private Button				addUpload;

	@FXML
	// fx:id="content_pane"
	private AnchorPane			content_pane;

	@FXML
	// fx:id="editQueue"
	private Button				editQueue;

	@FXML
	// fx:id="grid_pane"
	private GridPane			grid_pane;

	@FXML
	// fx:id="loading_pane"
	private GridPane			loading_pane;

	@FXML
	// fx:id="menuAbout"
	private MenuItem			menuAbout;

	@FXML
	// fx:id="menuAddAccount"
	private MenuItem			menuAddAccount;

	@FXML
	// fx:id="menuAddPlaylist"
	private MenuItem			menuAddPlaylist;

	@FXML
	// fx:id="menuAddPreset"
	private MenuItem			menuAddPreset;

	@FXML
	// fx:id="menuChangelog"
	private MenuItem			menuChangelog;

	@FXML
	// fx:id="menuClose"
	private MenuItem			menuClose;

	@FXML
	// fx:id="menuExportAccount"
	private MenuItem			menuExportAccount;

	@FXML
	// fx:id="menuExportPreset"
	private MenuItem			menuExportPreset;

	@FXML
	// fx:id="menuExportQueue"
	private MenuItem			menuExportQueue;

	@FXML
	// fx:id="menuImportAccount"
	private MenuItem			menuImportAccount;

	@FXML
	// fx:id="menuImportPreset"
	private MenuItem			menuImportPreset;

	@FXML
	// fx:id="menuImportQueue"
	private MenuItem			menuImportQueue;

	@FXML
	// fx:id="menuLogfile"
	private MenuItem			menuLogfile;

	@FXML
	// fx:id="menuOpenFile"
	private MenuItem			menuOpenFile;

	@FXML
	// fx:id="menuPlugins"
	private MenuItem			menuPlugins;

	@FXML
	// fx:id="menuWiki"
	private MenuItem			menuWiki;

	@FXML
	// fx:id="openDefaultdir"
	private Button				openDefaultdir;

	@FXML
	// fx:id="openEnddir"
	private Button				openEnddir;

	@FXML
	// fx:id="openFiles"
	private Button				openFiles;

	@FXML
	// fx:id="playlistList"
	private ChoiceBox<Playlist>	playlistList;

	@FXML
	// fx:id="presetList"
	private ChoiceBox<Preset>	presetList;

	@FXML
	// fx:id="queueBottom"
	private Button				queueBottom;

	@FXML
	// fx:id="queueDown"
	private Button				queueDown;

	@FXML
	// fx:id="queueTop"
	private Button				queueTop;

	@FXML
	// fx:id="queueUp"
	private Button				queueUp;

	@FXML
	// fx:id="queueView"
	private ChoiceBox<String>	queueView;

	@FXML
	// fx:id="refreshPlaylists"
	private Button				refreshPlaylists;

	@FXML
	// fx:id="removeAccount"
	private Button				removeAccount;

	@FXML
	// fx:id="removePreset"
	private Button				removePreset;

	@FXML
	// fx:id="removeQueue"
	private Button				removeQueue;

	@FXML
	// fx:id="resetUpload"
	private Button				resetUpload;

	@FXML
	// fx:id="savePreset"
	private Button				savePreset;

	@FXML
	// fx:id="startQueue"
	private Button				startQueue;

	@FXML
	// fx:id="stopQueue"
	private Button				stopQueue;

	@FXML
	// fx:id="uploadCategory"
	private ChoiceBox<Category>	uploadCategory;

	@FXML
	// fx:id="uploadComment"
	private ChoiceBox<String>	uploadComment;

	@FXML
	// fx:id="uploadCommentvote"
	private CheckBox			uploadCommentvote;

	@FXML
	// fx:id="uploadDefaultdir"
	private TextField			uploadDefaultdir;

	@FXML
	// fx:id="uploadDescription"
	private TextArea			uploadDescription;

	@FXML
	// fx:id="uploadEmbed"
	private CheckBox			uploadEmbed;

	@FXML
	// fx:id="uploadEnddir"
	private TextField			uploadEnddir;

	@FXML
	// fx:id="uploadFile"
	private ChoiceBox<File>		uploadFile;

	@FXML
	// fx:id="uploadLicense"
	private ChoiceBox<?>		uploadLicense;

	@FXML
	// fx:id="uploadMobile"
	private CheckBox			uploadMobile;

	@FXML
	// fx:id="uploadRate"
	private CheckBox			uploadRate;

	@FXML
	// fx:id="uploadTags"
	private TextArea			uploadTags;

	@FXML
	// fx:id="uploadTitle"
	private TextField			uploadTitle;

	@FXML
	// fx:id="uploadVideoresponse"
	private ChoiceBox<String>	uploadVideoresponse;

	@FXML
	// fx:id="uploadVisibility"
	private ChoiceBox<String>	uploadVisibility;

	@FXML
	// fx:id="x1"
	private TitledPane			x1;

	@Inject
	private AccountDao			accountDao;

	@Inject
	private PresetDao			presetDao;

	@Inject
	private PlaylistDao			playlistDao;

	@Inject
	private FileChooser			fileChooser;

	@Inject
	private DirectoryChooser	directoryChooser;

	@InjectLogger
	private Logger				logger;

	@Inject
	private CategoryService		categoryService;

	// Handler for Button[fx:id="addUpload"] onAction
	public void addUpload(ActionEvent event)
	{
		// handle the event here
	}

	// Handler for Button[fx:id="openDefaultdir"] onAction
	public void openDefaultdir(ActionEvent event)
	{
		File directory = directoryChooser.showDialog(null);
		if (directory != null)
		{
			uploadDefaultdir.setText(directory.getAbsolutePath());
		}
	}

	// Handler for Button[fx:id="openEnddir"] onAction
	public void openEnddir(ActionEvent event)
	{
		File directory = directoryChooser.showDialog(null);
		if (directory != null)
		{
			uploadEnddir.setText(directory.getAbsolutePath());
		}
	}

	// Handler for Button[fx:id="openFiles"] onAction
	public void openFiles(ActionEvent event)
	{
		List<File> files = fileChooser.showOpenMultipleDialog(null);
		if (files != null)
		{
			uploadFile.setItems(FXCollections.observableList(files));
			uploadFile.getSelectionModel().selectFirst();
		}
	}

	// Handler for Button[fx:id="refreshPlaylists"] onAction
	public void refreshPlaylists(ActionEvent event)
	{
		// handle the event here
	}

	// Handler for Button[fx:id="removeAccount"] onAction
	public void removeAccount(ActionEvent event)
	{
		if (accountList.getValue() != null)
		{
			accountDao.delete(accountList.getValue());
		}
	}

	// Handler for Button[fx:id="removePreset"] onAction
	public void removePreset(ActionEvent event)
	{
		if (presetList.getValue() != null)
		{
			presetDao.delete(presetList.getValue());
		}
	}

	// Handler for Button[fx:id="resetUpload"] onAction
	public void resetUpload(ActionEvent event)
	{
		// handle the event here
	}

	// Handler for Button[fx:id="savePreset"] onAction
	public void savePreset(ActionEvent event)
	{
		// handle the event here
	}

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources)
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
		assert removeAccount != null : "fx:id=\"removeAccount\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
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

		accountList.setItems(FXCollections.observableList(accountDao.getAll()));
		accountList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Account>() {

			@Override
			public void changed(ObservableValue<? extends Account> observable, Account oldValue, Account newValue)
			{
				playlistList.getItems().clear();

				if (newValue != null)
				{
					playlistList.getItems().addAll(playlistDao.getByAccount(newValue));
					playlistList.getSelectionModel().selectFirst();
				}
			}
		});
		accountList.getSelectionModel().selectFirst();

		presetList.setItems(FXCollections.observableArrayList(presetDao.getAll()));
		presetList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Preset>() {

			@Override
			public void changed(ObservableValue<? extends Preset> observable, Preset oldValue, Preset newValue)
			{
				if (newValue != null && newValue.account != null)
				{
					accountList.getSelectionModel().select(accountList.getItems().indexOf(newValue.account));
					if (newValue.playlist != null)
					{
						playlistList.getSelectionModel().select(newValue.playlist);
					}
				}
			}
		});
		presetList.getSelectionModel().selectFirst();

		uploadCategory.setItems(FXCollections.observableList(categoryService.load()));
	}

	@EventTopicSubscriber(topic = AccountDao.ACCOUNT_POST_ADDED)
	public void onAccountAdded(final String topic, final Account account)
	{
		Platform.runLater(new Runnable() {

			@Override
			public void run()
			{
				accountList.getItems().add(account);
				if (accountList.getValue() == null)
				{
					accountList.getSelectionModel().select(account);
				}
			}
		});
	}

	@EventTopicSubscriber(topic = AccountDao.ACCOUNT_POST_REMOVED)
	public void onAccountRemoved(final String topic, final Account account)
	{
		Platform.runLater(new Runnable() {

			@Override
			public void run()
			{
				accountList.getItems().remove(account);
				if (accountList.getValue() == null)
				{
					accountList.getSelectionModel().selectFirst();
				}
			}
		});
	}
}
