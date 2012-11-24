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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.converter.DefaultStringConverter;
import jfxtras.labs.scene.control.CalendarTextField;
import jfxtras.labs.scene.control.ListSpinner;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.google.atom.AtomCategory;
import org.chaosfisch.util.ExtendedPlaceholders;
import org.chaosfisch.util.TagParser;
import org.chaosfisch.youtubeuploader.I18nHelper;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.ModelEvents;
import org.chaosfisch.youtubeuploader.models.Placeholder;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.models.Template;
import org.chaosfisch.youtubeuploader.models.UploadBuilder;
import org.chaosfisch.youtubeuploader.services.youtube.spi.CategoryService;
import org.chaosfisch.youtubeuploader.services.youtube.spi.PlaylistService;
import org.javalite.activejdbc.Model;

import com.google.inject.Inject;

public class UploadController implements Initializable
{
	@FXML// fx:id="accountList"
	private ChoiceBox<Model>			accountList;

	@FXML// fx:id="addUpload"
	private Button						addUpload;

	@FXML// fx:id="extendedSettingsGrid"
	private GridPane					extendedSettingsGrid;

	@FXML// fx:id="openDefaultdir"
	private Button						openDefaultdir;

	@FXML// fx:id="openEnddir"
	private Button						openEnddir;

	@FXML// fx:id="openFiles"
	private Button						openFiles;

	@FXML// fx:id="playlistCheckbox"
	private CheckBox					playlistCheckbox;

	@FXML// fx:id="playlistList"
	private ChoiceBox<Model>			playlistList;

	@FXML// fx:id="previewTitle"
	private TextField					previewTitle;

	@FXML// fx:id="refreshPlaylists"
	private Button						refreshPlaylists;

	@FXML// fx:id="removeTemplate"
	private Button						removeTemplate;

	@FXML// fx:id="resetUpload"
	private Button						resetUpload;

	@FXML// fx:id="saveTemplate"
	private Button						saveTemplate;

	@FXML// fx:id="templateList"
	private ChoiceBox<Model>			templateList;

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
	private ChoiceBox<String>			uploadLicense;

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

	@FXML// fx:id="validationText"
	private Label						validationText;

	private final CalendarTextField		starttime		= new CalendarTextField().withValue(Calendar.getInstance())
																.withDateFormat(new SimpleDateFormat("dd.MM.yyyy hh:mm"))
																.withShowTime(Boolean.TRUE);
	private final CalendarTextField		releasetime		= new CalendarTextField().withValue(Calendar.getInstance())
																.withDateFormat(new SimpleDateFormat("dd.MM.yyyy hh:mm"))
																.withShowTime(Boolean.TRUE);
	private final ListSpinner<Integer>	number			= new ListSpinner<Integer>(-1000, 1000).withValue(0).withAlignment(Pos.CENTER_RIGHT);

	@Inject private PlaylistService		playlistService;
	@Inject private CategoryService		categoryService;
	@Inject private FileChooser			fileChooser;
	@Inject private DirectoryChooser	directoryChooser;

	private final ObservableList<Model>	accountItems	= FXCollections.observableArrayList();

	private final ObservableList<Model>	playlistItems	= FXCollections.observableArrayList();

	private final ObservableList<Model>	templateItems	= FXCollections.observableArrayList();													;

	private void _resetUpload(final Model template)
	{
		if (!(template instanceof Template)) { return; }
		uploadCategory.getSelectionModel().select(template.getInteger("category") != null ? template.getInteger("category") : 0);
		uploadComment.getSelectionModel().select(template.getInteger("comment") != null ? template.getInteger("comment") : 0);
		uploadCommentvote.setSelected(template.getBoolean("commentvote"));
		uploadDefaultdir.setText(template.getString("defaultdir"));
		uploadDescription.setText(template.getString("description"));
		uploadEmbed.setSelected(template.getBoolean("embed"));
		uploadEnddir.setText(template.getString("enddir"));
		uploadLicense.getSelectionModel().select(template.getInteger("license") != null ? template.getInteger("license") : 0);
		uploadMobile.setSelected(template.getBoolean("mobile"));
		uploadRate.setSelected(template.getBoolean("rate"));
		uploadTags.setText(template.getString("keywords"));
		uploadTitle.setText(template.getString("title"));
		uploadVideoresponse.getSelectionModel().select(template.getInteger("videoresponse") != null ? template.getInteger("videoresponse") : 1);
		uploadVisibility.getSelectionModel().select(template.getInteger("visibility") != null ? template.getInteger("visibility") : 0);
		number.setValue(template.getInteger("number") != null ? template.getInteger("number") : 0);
		if (template.parent(Account.class) != null)
		{
			accountList.getSelectionModel().select(template.parent(Account.class));
		}
		if (template.getAll(Playlist.class).size() > 0)
		{
			playlistList.getSelectionModel().select(template.getAll(Playlist.class).get(0));
			playlistCheckbox.setSelected(true);
		} else
		{
			playlistCheckbox.setSelected(false);
		}

	}

	public void addPlaceholder(final String placeholder, final String replacement)
	{
		Placeholder.createIt("placeholder", placeholder, "replacement", replacement);
	}

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

		final UploadBuilder uploadBuilder = new UploadBuilder(uploadFile.getValue(), uploadTitle.getText().trim(), uploadCategory.getValue().term,
				(Account) accountList.getValue()).setComment(uploadComment.getSelectionModel().getSelectedIndex())
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
			uploadBuilder.addPlaylist((Playlist) playlistList.getValue());
		}

		if (starttime.getValue().getTimeInMillis() > System.currentTimeMillis())
		{
			uploadBuilder.setStarted(new Date(starttime.getValue().getTimeInMillis()));
		}

		if (releasetime.getValue().getTimeInMillis() > System.currentTimeMillis())
		{
			final Calendar calendar = releasetime.getValue();
			final int unroundedMinutes = calendar.get(Calendar.MINUTE);
			final int mod = unroundedMinutes % 30;
			calendar.add(Calendar.MINUTE, (mod < 16) ? -mod : (30 - mod));
			uploadBuilder.setRelease(new Date(calendar.getTimeInMillis()));
		}

		uploadBuilder.build();
	}

	@SuppressWarnings("unchecked")
	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(final URL fxmlFileLocation, final ResourceBundle resources)
	{
		assert accountList != null : "fx:id=\"accountList\" was not injected: check your FXML file 'Upload.fxml'.";
		assert addUpload != null : "fx:id=\"addUpload\" was not injected: check your FXML file 'Upload.fxml'.";
		assert extendedSettingsGrid != null : "fx:id=\"extendedSettingsGrid\" was not injected: check your FXML file 'Upload.fxml'.";
		assert openDefaultdir != null : "fx:id=\"openDefaultdir\" was not injected: check your FXML file 'Upload.fxml'.";
		assert openEnddir != null : "fx:id=\"openEnddir\" was not injected: check your FXML file 'Upload.fxml'.";
		assert openFiles != null : "fx:id=\"openFiles\" was not injected: check your FXML file 'Upload.fxml'.";
		assert playlistCheckbox != null : "fx:id=\"playlistCheckbox\" was not injected: check your FXML file 'Upload.fxml'.";
		assert playlistList != null : "fx:id=\"playlistList\" was not injected: check your FXML file 'Upload.fxml'.";
		assert previewTitle != null : "fx:id=\"previewTitle\" was not injected: check your FXML file 'Upload.fxml'.";
		assert refreshPlaylists != null : "fx:id=\"refreshPlaylists\" was not injected: check your FXML file 'Upload.fxml'.";
		assert removeTemplate != null : "fx:id=\"removeTemplate\" was not injected: check your FXML file 'Upload.fxml'.";
		assert resetUpload != null : "fx:id=\"resetUpload\" was not injected: check your FXML file 'Upload.fxml'.";
		assert saveTemplate != null : "fx:id=\"saveTemplate\" was not injected: check your FXML file 'Upload.fxml'.";
		assert templateList != null : "fx:id=\"templateList\" was not injected: check your FXML file 'Upload.fxml'.";
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

		extendedSettingsGrid.add(number, 1, 1, GridPane.REMAINING, 1);
		extendedSettingsGrid.add(starttime, 1, 11, GridPane.REMAINING, 1);
		extendedSettingsGrid.add(releasetime, 1, 12, GridPane.REMAINING, 1);

		// Initial fill of lists
		uploadVisibility.setItems(FXCollections.observableArrayList(I18nHelper.message("visibilitylist.public"),
																	I18nHelper.message("visibilitylist.unlisted"),
																	I18nHelper.message("visibilitylist.private"),
																	I18nHelper.message("visibilitylist.scheduled")));
		uploadVisibility.getSelectionModel().selectFirst();
		uploadComment.setItems(FXCollections.observableArrayList(	I18nHelper.message("commentlist.allowed"),
																	I18nHelper.message("commentlist.moderated"),
																	I18nHelper.message("commentlist.denied"),
																	I18nHelper.message("commentlist.friendsonly")));
		uploadComment.getSelectionModel().selectFirst();
		uploadLicense.setItems(FXCollections.observableArrayList(I18nHelper.message("licenselist.youtube"), I18nHelper.message("licenselist.cc")));
		uploadLicense.getSelectionModel().selectFirst();
		uploadVideoresponse.setItems(FXCollections.observableArrayList(	I18nHelper.message("videoresponselist.allowed"),
																		I18nHelper.message("videoresponselist.moderated"),
																		I18nHelper.message("videoresponselist.denied")));
		uploadVideoresponse.getSelectionModel().selectFirst();

		uploadCategory.setItems(FXCollections.observableList(categoryService.load()));
		uploadCategory.getSelectionModel().selectFirst();

		releasetime.disableProperty().bind(uploadVisibility.getSelectionModel().selectedIndexProperty().lessThan(2));

		uploadFile.setItems(FXCollections.observableArrayList(new File("")));
		uploadFile.getItems().clear();

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

		templateItems.addAll(Template.findAll().include(Account.class, Playlist.class));
		templateList.setItems(templateItems);
		templateList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Model>() {

			@Override
			public void changed(final ObservableValue<? extends Model> observable, final Model oldValue, final Model newValue)
			{
				resetUpload(null);
			}
		});
		templateList.getSelectionModel().selectFirst();

		previewTitle.textProperty().bindBidirectional(uploadTitle.textProperty(), new DefaultStringConverter() {

			final ExtendedPlaceholders	extendedPlaceholders	= new ExtendedPlaceholders();

			@Override
			public String toString(final String value)
			{
				extendedPlaceholders.setFile(uploadFile.getValue() != null ? uploadFile.getValue().getAbsolutePath() : "{file-missing}");
				extendedPlaceholders.setPlaylist((Playlist) playlistList.getValue());
				extendedPlaceholders.setNumber(number.getValue());

				return extendedPlaceholders.replace(value);
			}
		});

		final InvalidationListener previewTitleChangeListener = new InvalidationListener() {

			@Override
			public void invalidated(final Observable observable)
			{
				_refreshPreviewTitle();
			}
		};
		playlistList.valueProperty().addListener(previewTitleChangeListener);
		number.valueProperty().addListener(previewTitleChangeListener);
		uploadFile.valueProperty().addListener(previewTitleChangeListener);
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
				} else if (model instanceof Template)
				{
					templateItems.add(model);
					if (templateList.getValue() == null)
					{
						templateList.getSelectionModel().selectFirst();
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
				} else if (model instanceof Template)
				{
					templateItems.remove(model);
					if (templateList.getSelectionModel().isEmpty())
					{
						templateList.getSelectionModel().selectFirst();
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
			uploadFile.getItems().clear();
			uploadFile.getItems().addAll(files);
			uploadFile.getSelectionModel().selectFirst();
			if ((uploadTitle.getText() == null) || uploadTitle.getText().isEmpty())
			{
				final String file = files.get(0).getAbsolutePath();
				int index = file.lastIndexOf(".");
				if (index == -1)
				{
					index = file.length();
				}
				uploadTitle.setText(file.substring(file.lastIndexOf(File.separator) + 1, index));
			}
		}
	}

	// Handler for Button[fx:id="refreshPlaylists"] onAction
	public void refreshPlaylists(final ActionEvent event)
	{
		final Account[] accountArray = new Account[accountItems.size()];
		accountItems.toArray(accountArray);
		playlistService.synchronizePlaylists(Arrays.asList(accountArray));
	}

	// Handler for Button[fx:id="removeTemplate"] onAction
	public void removeTemplate(final ActionEvent event)
	{
		if (templateList.getValue() != null)
		{
			templateList.getValue().delete();
		}
	}

	// Handler for Button[fx:id="resetUpload"] onAction
	public void resetUpload(final ActionEvent event)
	{
		if (templateList.getItems().isEmpty())
		{
			_resetUpload(ViewController.standardTemplate);
		} else
		{
			_resetUpload(templateList.getValue());
		}
	}

	// Handler for Button[id="saveTemplate"] onAction
	public void saveTemplate(final ActionEvent event)
	{
		final Template template = (Template) templateList.getValue();
		if (template == null) { return; }
		template.setInteger("category", uploadCategory.getSelectionModel().getSelectedIndex());
		template.setInteger("comment", uploadComment.getSelectionModel().getSelectedIndex());
		template.setBoolean("commentvote", uploadCommentvote.isSelected());
		template.setString("defaultdir", uploadDefaultdir.getText() == null ? "" : uploadDefaultdir.getText());
		template.setString("description", uploadDescription.getText() == null ? "" : uploadDescription.getText());
		template.setBoolean("embed", uploadEmbed.isSelected());
		template.setString("enddir", uploadEnddir.getText() == null ? "" : uploadEnddir.getText());
		template.setInteger("license", uploadLicense.getSelectionModel().getSelectedIndex());
		template.setBoolean("mobile", uploadMobile.isSelected());
		template.setBoolean("rate", uploadRate.isSelected());
		template.setString("keywords", uploadTags.getText() == null ? "" : uploadTags.getText());
		template.setString("title", uploadTitle.getText() == null ? "" : uploadTitle.getText());
		template.setInteger("videoresponse", uploadVideoresponse.getSelectionModel().getSelectedIndex());
		template.setInteger("visibility", uploadVisibility.getSelectionModel().getSelectedIndex());
		template.setInteger("number", number.getValue());
		if (accountList.getValue() != null)
		{
			template.setParent(accountList.getValue());
		}
		if (playlistCheckbox.isSelected() && (playlistList.getValue() != null))
		{
			template.add(playlistList.getValue());
		} else
		{
			for (final Playlist playlist : template.getAll(Playlist.class))
			{
				template.remove(playlist);
			}
		}
		template.saveIt();

	}

	private void _refreshPreviewTitle()
	{
		final String value = uploadTitle.textProperty().get();
		uploadTitle.textProperty().set(null);
		uploadTitle.textProperty().set(value);
	}

	// validate each of the three input fields
	private String validate()
	{
		if (uploadFile.getItems().isEmpty())
		{
			return I18nHelper.message("validation.filelist");
		} else if ((uploadTitle.getText() == null) || (uploadTitle.getText().getBytes().length < 5)
				|| (uploadTitle.getText().getBytes().length > 100))
		{
			return I18nHelper.message("validation.title");
		} else if (uploadCategory.getValue() == null)
		{
			return I18nHelper.message("validation.category");
		} else if ((uploadDescription.getText() != null) && (uploadDescription.getText().getBytes().length > 5000))
		{
			return I18nHelper.message("validation.description");
		} else if ((uploadDescription.getText() != null) && (uploadDescription.getText().contains(">") || uploadDescription.getText().contains("<")))
		{
			return I18nHelper.message("validation.description.characters");
		} else if ((uploadTags.getText() != null) && ((uploadTags.getText().getBytes().length > 500) || !TagParser.isValid(uploadTags.getText())))
		{
			return I18nHelper.message("validation.tags");
		} else if (accountList.getValue() == null) { return I18nHelper.message("validation.account"); }
		return I18nHelper.message("validation.info.added");
	}
}
