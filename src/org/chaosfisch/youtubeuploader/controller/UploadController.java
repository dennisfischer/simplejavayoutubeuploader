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
import java.sql.Date;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.google.atom.AtomCategory;
import org.chaosfisch.util.TagParser;
import org.chaosfisch.youtubeuploader.I18nHelper;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.ModelEvents;
import org.chaosfisch.youtubeuploader.models.Placeholder;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.models.Preset;
import org.chaosfisch.youtubeuploader.models.QueueBuilder;
import org.chaosfisch.youtubeuploader.services.youtube.spi.CategoryService;
import org.chaosfisch.youtubeuploader.services.youtube.spi.PlaylistService;
import org.javalite.activejdbc.Model;

import com.google.inject.Inject;

public class UploadController implements Initializable
{
	@FXML// fx:id="openDefaultdir"
	private Button						openDefaultdir;

	@FXML// fx:id="openEnddir"
	private Button						openEnddir;

	@FXML// fx:id="removePreset"
	private Button						removePreset;

	@FXML// fx:id="savePreset"
	private Button						savePreset;

	@FXML// fx:id="uploadComment"
	private ChoiceBox<String>			uploadComment;

	@FXML// fx:id="uploadCommentvote"
	private CheckBox					uploadCommentvote;

	@FXML// fx:id="uploadDefaultdir"
	private TextField					uploadDefaultdir;

	@FXML// fx:id="uploadEmbed"
	private CheckBox					uploadEmbed;

	@FXML// fx:id="uploadEnddir"
	private TextField					uploadEnddir;

	@FXML// fx:id="uploadLicense"
	private ChoiceBox<String>			uploadLicense;

	@FXML// fx:id="uploadMobile"
	private CheckBox					uploadMobile;

	@FXML// fx:id="uploadRate"
	private CheckBox					uploadRate;

	@FXML// fx:id="uploadVideoresponse"
	private ChoiceBox<String>			uploadVideoresponse;

	@FXML// fx:id="uploadVisibility"
	private ChoiceBox<String>			uploadVisibility;

	@FXML// fx:id="accountList"
	private ChoiceBox<Model>			accountList;

	@FXML// fx:id="addUpload"
	private Button						addUpload;

	@FXML// fx:id="openFiles"
	private Button						openFiles;

	@FXML// fx:id="playlistCheckbox"
	private CheckBox					playlistCheckbox;

	@FXML// fx:id="playlistList"
	private ChoiceBox<Model>			playlistList;

	@FXML// fx:id="presetList"
	private ChoiceBox<Model>			presetList;

	@FXML// fx:id="refreshPlaylists"
	private Button						refreshPlaylists;

	@FXML// fx:id="resetUpload"
	private Button						resetUpload;

	@FXML// fx:id="uploadCategory"
	private ChoiceBox<AtomCategory>		uploadCategory;

	@FXML// fx:id="uploadDescription"
	private TextArea					uploadDescription;

	@FXML// fx:id="uploadFile"
	private ChoiceBox<File>				uploadFile;

	@FXML// fx:id="uploadTags"
	private TextArea					uploadTags;

	@FXML// fx:id="uploadTitle"
	private TextField					uploadTitle;

	@FXML// fx:id="validationText"
	private Label						validationText;

	@Inject private PlaylistService		playlistService;
	@Inject private CategoryService		categoryService;
	@Inject private FileChooser			fileChooser;
	@Inject private DirectoryChooser	directoryChooser;

	private final ObservableList<Model>	accountItems	= FXCollections.observableArrayList();

	private final ObservableList<Model>	playlistItems	= FXCollections.observableArrayList();

	private final ObservableList<Model>	presetItems		= FXCollections.observableArrayList();

	// Handler for Button[fx:id="addUpload"] onAction
	public void addUpload(final ActionEvent event)
	{
		final String validation = validate();
		validationText.setText(validation);
		if (!validation.equals(I18nHelper.message("validation.info.added")))
		{
			validationText.setId("validation_error");
			return;
		}
		validationText.setId("validation_passed");

		final QueueBuilder queueBuilder = new QueueBuilder(uploadFile.getValue(), uploadTitle.getText().trim(), uploadCategory.getValue().term,
				(Account) accountList.getValue())
				.setComment(uploadComment.getSelectionModel().getSelectedIndex())
				.setCommentvote(uploadCommentvote.isSelected())
				.setDescription(uploadDescription.getText().trim())
				.setEmbed(uploadEmbed.isSelected())
				.setEnddir(uploadEnddir.getText().trim())
				.setLicense(uploadLicense.getSelectionModel().getSelectedIndex())
				.setMobile(uploadMobile.isSelected())
				.setNumber(0)
				.setRate(uploadRate.isSelected())
				.setTags(uploadTags.getText().trim())
				.setVideoresponse(uploadVideoresponse.getSelectionModel().getSelectedIndex())
				.setVisibility(uploadVisibility.getSelectionModel().getSelectedIndex());

		if (playlistCheckbox.isSelected())
		{
			queueBuilder.setPlaylist((Playlist) playlistList.getValue());
		}
		final Date starttime = new Date(0);
		final Date releasetime = new Date(0);

		if (starttime.getTime() > System.currentTimeMillis())
		{
			queueBuilder.setStarted(starttime);
		}

		if (releasetime.getTime() > System.currentTimeMillis())
		{
			final Calendar calendar = Calendar.getInstance();
			calendar.setTime(releasetime);
			final int unroundedMinutes = calendar.get(Calendar.MINUTE);
			final int mod = unroundedMinutes % 30;
			calendar.add(Calendar.MINUTE, (mod < 16) ? -mod : (30 - mod));
			queueBuilder.setRelease(new Date(calendar.getTimeInMillis()));
		}

		queueBuilder.build();
	}

	// Handler for Button[fx:id="resetUpload"] onAction
	public void resetUpload(final ActionEvent event)
	{
		// handle the event here
	}

	// Handler for Button[id="savePreset"] onAction
	public void savePreset(final ActionEvent event)
	{
		// handle the event here
	}

	// validate each of the three input fields
	private String validate()
	{
		if (uploadFile.getItems().isEmpty())
		{
			return I18nHelper.message("validation.filelist");
		} else if ((uploadTitle.getText().getBytes().length < 5) || (uploadTitle.getText().getBytes().length > 100))
		{
			return I18nHelper.message("validation.title");
		} else if (uploadCategory.getValue() == null)
		{
			return I18nHelper.message("validation.category");
		} else if (uploadDescription.getText().getBytes().length > 5000)
		{
			return I18nHelper.message("validation.description");
		} else if (uploadDescription.getText().contains(">") || uploadDescription.getText().contains("<"))
		{
			return I18nHelper.message("validation.description.characters");
		} else if ((uploadTags.getText().getBytes().length > 500) || !TagParser.isValid(uploadTags.getText()))
		{
			return I18nHelper.message("validation.tags");
		} else if (accountList.getValue() == null) { return I18nHelper.message("validation.account"); }
		return I18nHelper.message("validation.info.added");
	}

	@SuppressWarnings("unchecked")
	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(final URL fxmlFileLocation, final ResourceBundle resources)
	{
		assert accountList != null : "fx:id=\"accountList\" was not injected: check your FXML file 'Upload.fxml'.";
		assert addUpload != null : "fx:id=\"addUpload\" was not injected: check your FXML file 'Upload.fxml'.";
		assert openDefaultdir != null : "fx:id=\"openDefaultdir\" was not injected: check your FXML file 'Upload.fxml'.";
		assert openEnddir != null : "fx:id=\"openEnddir\" was not injected: check your FXML file 'Upload.fxml'.";
		assert openFiles != null : "fx:id=\"openFiles\" was not injected: check your FXML file 'Upload.fxml'.";
		assert playlistCheckbox != null : "fx:id=\"playlistCheckbox\" was not injected: check your FXML file 'Upload.fxml'.";
		assert playlistList != null : "fx:id=\"playlistList\" was not injected: check your FXML file 'Upload.fxml'.";
		assert presetList != null : "fx:id=\"presetList\" was not injected: check your FXML file 'Upload.fxml'.";
		assert refreshPlaylists != null : "fx:id=\"refreshPlaylists\" was not injected: check your FXML file 'Upload.fxml'.";
		assert removePreset != null : "fx:id=\"removePreset\" was not injected: check your FXML file 'Upload.fxml'.";
		assert resetUpload != null : "fx:id=\"resetUpload\" was not injected: check your FXML file 'Upload.fxml'.";
		assert savePreset != null : "fx:id=\"savePreset\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadCategory != null : "fx:id=\"uploadCategory\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadComment != null : "fx:id=\"uploadComment\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadCommentvote != null : "fx:id=\"uploadCommentvote\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadDefaultdir != null : "fx:id=\"uploadDefaultdir\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadDescription != null : "fx:id=\"uploadDescription\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadEmbed != null : "fx:id=\"uploadEmbed\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadEnddir != null : "fx:id=\"uploadEnddir\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadFile != null : "fx:id=\"uploadFile\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadLicense != null : "fx:id=\"uploadLicense\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadMobile != null : "fx:id=\"uploadMobile\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadRate != null : "fx:id=\"uploadRate\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadTags != null : "fx:id=\"uploadTags\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadTitle != null : "fx:id=\"uploadTitle\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadVideoresponse != null : "fx:id=\"uploadVideoresponse\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadVisibility != null : "fx:id=\"uploadVisibility\" was not injected: check your FXML file 'Upload.fxml'.";
		assert validationText != null : "fx:id=\"validationText\" was not injected: check your FXML file 'Upload.fxml'.";
		// initialize your logic here: all @FXML variables will have been
		// injected

		AnnotationProcessor.process(this);

		// Initial fill of lists
		uploadVisibility.setItems(FXCollections.observableArrayList(I18nHelper.message("visibilitylist.public"),
				I18nHelper.message("visibilitylist.unlisted"), I18nHelper.message("visibilitylist.private"),
				I18nHelper.message("visibilitylist.scheduled")));
		uploadVisibility.getSelectionModel().selectFirst();
		uploadComment
				.setItems(FXCollections.observableArrayList(I18nHelper.message("commentlist.allowed"), I18nHelper.message("commentlist.moderated"),
						I18nHelper.message("commentlist.denied"), I18nHelper.message("commentlist.friendsonly")));
		uploadComment.getSelectionModel().selectFirst();
		uploadLicense.setItems(FXCollections.observableArrayList(I18nHelper.message("licenselist.youtube"), I18nHelper.message("licenselist.cc")));
		uploadLicense.getSelectionModel().selectFirst();
		uploadVideoresponse.setItems(FXCollections.observableArrayList(I18nHelper.message("videoresponselist.allowed"),
				I18nHelper.message("videoresponselist.moderated"), I18nHelper.message("videoresponselist.denied")));
		uploadVideoresponse.getSelectionModel().selectFirst();

		uploadCategory.setItems(FXCollections.observableList(categoryService.load()));
		uploadCategory.getSelectionModel().selectFirst();

		// Load dynamic list data
		accountItems.addAll(Account.where("type = ?", Account.Type.YOUTUBE.name()).include(Playlist.class));
		accountList.setItems(accountItems);
		playlistList.setItems(playlistItems);
		accountList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Model>() {

			@Override
			public void changed(final ObservableValue<? extends Model> observable, final Model oldValue, final Model newValue)
			{
				playlistList.getItems().clear();

				if ((newValue != null) && (newValue.get("playlists") != null))
				{
					playlistItems.clear();
					playlistItems.addAll((Collection<Model>) newValue.get("playlists"));
					playlistList.getSelectionModel().selectFirst();
				}
			}
		});
		accountList.getSelectionModel().selectFirst();

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
		final Account[] accountArray = new Account[accountItems.size()];
		accountItems.toArray(accountArray);
		playlistService.synchronizePlaylists(Arrays.asList(accountArray));
	}

	// Handler for Button[fx:id="removePreset"] onAction
	public void removePreset(final ActionEvent event)
	{
		if (presetList.getValue() != null)
		{
			presetList.getValue().delete();
		}
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
				if ((model instanceof Account) && model.get("type").equals(Account.Type.YOUTUBE.name()))
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
				} else if ((model instanceof Playlist) && model.parent(Account.class).equals(accountList.getValue()))
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

	public void addPlaceholder(final String placeholder, final String replacement)
	{
		Placeholder.createIt("placeholder", placeholder, "replacement", replacement);
	}
}
