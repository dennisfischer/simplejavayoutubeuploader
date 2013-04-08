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

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.StringConverter;
import jfxtras.labs.scene.control.BeanPathAdapter;
import jfxtras.labs.scene.control.CalendarTextField;
import jfxtras.labs.scene.control.ListSpinner;
import jfxtras.labs.scene.control.grid.GridCell;
import jfxtras.labs.scene.control.grid.GridView;
import jfxtras.labs.scene.control.grid.GridViewBuilder;

import org.chaosfisch.google.atom.AtomCategory;
import org.chaosfisch.youtubeuploader.command.RefreshPlaylistsCommand;
import org.chaosfisch.youtubeuploader.command.RemoveTemplateCommand;
import org.chaosfisch.youtubeuploader.command.UpdateTemplateCommand;
import org.chaosfisch.youtubeuploader.controller.renderer.PlaylistGridCell;
import org.chaosfisch.youtubeuploader.db.data.ClaimOption;
import org.chaosfisch.youtubeuploader.db.data.ClaimType;
import org.chaosfisch.youtubeuploader.db.data.Comment;
import org.chaosfisch.youtubeuploader.db.data.License;
import org.chaosfisch.youtubeuploader.db.data.Videoresponse;
import org.chaosfisch.youtubeuploader.db.data.Visibility;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Account;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;
import org.chaosfisch.youtubeuploader.guice.ICommandProvider;
import org.chaosfisch.youtubeuploader.vo.UploadViewVO;

import com.google.inject.Inject;

public class UploadController {

	@FXML
	private ResourceBundle						resources;

	@FXML
	private URL									location;

	@FXML
	private ChoiceBox<Account>					uploadAccount;

	@FXML
	private Button								addUpload;

	@FXML
	private RadioButton							assetMovie;

	@FXML
	private RadioButton							assetTV;

	@FXML
	private ToggleGroup							assetType;

	@FXML
	private RadioButton							assetWeb;

	@FXML
	private ToggleGroup							contentSyndication;

	@FXML
	private GridPane							extendedSettingsGrid;

	@FXML
	private Slider								gridWidthSlider;

	@FXML
	private Label								labelContentInformation;

	@FXML
	private Label								labelCustomId;

	@FXML
	private Label								labelDescription;

	@FXML
	private Label								labelEIDR;

	@FXML
	private Label								labelISAN;

	@FXML
	private Label								labelMonetizeTitle;

	@FXML
	private Label								labelNote;

	@FXML
	private Label								labelNumberEpisode;

	@FXML
	private Label								labelNumberSeason;

	@FXML
	private Label								labelTMSID;

	@FXML
	private Label								labelTitleEpisode;

	@FXML
	private CheckBox							monetizeClaim;

	@FXML
	private ChoiceBox<ClaimOption>				monetizeClaimOption;

	@FXML
	private ChoiceBox<ClaimType>				monetizeClaimType;

	@FXML
	private TextField							monetizeCustomID;

	@FXML
	private TextField							monetizeDescription;

	@FXML
	private TextField							monetizeEIDR;

	@FXML
	private TextField							monetizeISAN;

	@FXML
	private CheckBox							monetizeInStream;

	@FXML
	private CheckBox							monetizeInStreamDefaults;

	@FXML
	private TextField							monetizeNotes;

	@FXML
	private TextField							monetizeNumberEpisode;

	@FXML
	private TextField							monetizeNumberSeason;

	@FXML
	private CheckBox							monetizeOverlay;

	@FXML
	private ToggleButton						monetizePartner;

	@FXML
	private CheckBox							monetizeProductPlacement;

	@FXML
	private TextField							monetizeTMSID;

	@FXML
	private TextField							monetizeTitle;

	@FXML
	private TextField							monetizeTitleEpisode;

	@FXML
	private CheckBox							monetizeTrueView;

	@FXML
	private Button								openDefaultdir;

	@FXML
	private Button								openEnddir;

	@FXML
	private Button								openFiles;

	@FXML
	private Button								openThumbnail;

	@FXML
	private TitledPane							partnerPane;

	@FXML
	private ScrollPane							playlistDropScrollpane;

	@FXML
	private GridPane							playlistGrid;

	@FXML
	private ScrollPane							playlistSourceScrollpane;

	@FXML
	private TextField							previewTitle;

	@FXML
	private Button								refreshPlaylists;

	@FXML
	private Button								removeTemplate;

	@FXML
	private Button								resetUpload;

	@FXML
	private Button								saveTemplate;

	@FXML
	private ChoiceBox<Template>					templates;

	@FXML
	private ChoiceBox<AtomCategory>				uploadCategory;

	@FXML
	private ChoiceBox<Comment>					uploadComment;

	@FXML
	private CheckBox							uploadCommentvote;

	@FXML
	private TextField							uploadDefaultdir;

	@FXML
	private TextArea							uploadDescription;

	@FXML
	private CheckBox							uploadEmbed;

	@FXML
	private TextField							uploadEnddir;

	@FXML
	private CheckBox							uploadFacebook;

	@FXML
	private ChoiceBox<File>						uploadFile;

	@FXML
	private GridPane							uploadGrid;

	@FXML
	private ChoiceBox<License>					uploadLicense;

	@FXML
	private TextArea							uploadMessage;

	@FXML
	private CheckBox							uploadMobile;

	@FXML
	private CheckBox							uploadRate;

	@FXML
	private TextArea							uploadTags;

	@FXML
	private TextField							uploadThumbnail;

	@FXML
	private TextField							uploadTitle;

	@FXML
	private CheckBox							uploadTwitter;

	@FXML
	private ChoiceBox<Videoresponse>			uploadVideoresponse;

	@FXML
	private ChoiceBox<Visibility>				uploadVisibility;

	@FXML
	private TitledPane							x2;

	@FXML
	private TitledPane							x5;

	private final CalendarTextField				started				= new CalendarTextField().withValue(Calendar.getInstance())
																		.withDateFormat(new SimpleDateFormat("dd.MM.yyyy HH:mm"))
																		.withShowTime(true);
	private final CalendarTextField				release				= new CalendarTextField().withValue(Calendar.getInstance())
																		.withDateFormat(new SimpleDateFormat("dd.MM.yyyy HH:mm"))
																		.withShowTime(true);
	private final ListSpinner<Integer>			number				= new ListSpinner<Integer>(-1000,
																		1000).withValue(0)
																		.withAlignment(Pos.CENTER_RIGHT);
	private final GridView<Playlist>			playlistSourcezone	= GridViewBuilder.create(Playlist.class)
																		.build();
	private final GridView<Playlist>			playlistTargetzone	= GridViewBuilder.create(Playlist.class)
																		.build();

	@Inject
	private FileChooser							fileChooser;
	@Inject
	private DirectoryChooser					directoryChooser;
	@Inject
	private ICommandProvider					commandProvider;

	private final ObservableList<File>			filesList			= FXCollections.observableArrayList();
	private final ObservableList<AtomCategory>	categoriesList		= FXCollections.observableArrayList();
	private final ObservableList<Account>		accountsList		= FXCollections.observableArrayList();
	private final ObservableList<Template>		templatesList		= FXCollections.observableArrayList();
	private final ObservableList<Visibility>	visibilityList		= FXCollections.observableArrayList();
	private final ObservableList<Comment>		commentsList		= FXCollections.observableArrayList();
	private final ObservableList<License>		licensesList		= FXCollections.observableArrayList();
	private final ObservableList<Videoresponse>	videoresponsesList	= FXCollections.observableArrayList();
	private final ObservableList<ClaimOption>	claimOptionsList	= FXCollections.observableArrayList();
	private final ObservableList<ClaimType>		claimTypesList		= FXCollections.observableArrayList();
	private final ObservableList<Playlist>		playlistSourceList	= FXCollections.observableArrayList();
	private final ObservableList<Playlist>		playlistTargetList	= FXCollections.observableArrayList();
	private final SimpleIntegerProperty			idProperty			= new SimpleIntegerProperty(-1);
	private final UploadViewVO					uploadViewVO		= new UploadViewVO();
	private final BeanPathAdapter<UploadViewVO>	beanPathAdapter		= new BeanPathAdapter<UploadViewVO>(uploadViewVO);

	@FXML
	void addUpload(final ActionEvent event) {
		System.out.println(beanPathAdapter.getBean()
			.getTemplate()
			.getTitle());
		System.out.println(beanPathAdapter.getBean()
			.getUpload()
			.getVisibility());
		System.out.println(uploadVisibility.valueProperty()
			.get()
			.ordinal());

		/*
		 * final Upload upload = uploadViewModel.toUpload();
		 * TODO
		 * if (upload.isValid()) {
		 * validationText.setId("validation_passed");
		 * validationText.setText(I18nHelper.message("validation.info.added"));
		 * } else {
		 * validationText.setId("validation_error");
		 * final StringBuilder stringBuilder = new StringBuilder("");
		 * for (final String error : upload.errors().values()) {
		 * stringBuilder.append(error);
		 * stringBuilder.append('\n');
		 * }
		 * validationText.setText(stringBuilder.toString());
		 */
	}

	@FXML
	void openDefaultdir(final ActionEvent event) {
		final File directory = directoryChooser.showDialog(null);
		if (directory != null) {
			uploadDefaultdir.setText(directory.getAbsolutePath());
		}
	}

	@FXML
	void openEnddir(final ActionEvent event) {
		final File directory = directoryChooser.showDialog(null);
		if (directory != null) {
			uploadEnddir.setText(directory.getAbsolutePath());
		}
	}

	@FXML
	void openFiles(final ActionEvent event) {
		final List<File> files = fileChooser.showOpenMultipleDialog(null);
		if (files != null && files.size() > 0) {
			filesList.clear();
			filesList.addAll(files);
			uploadFile.getSelectionModel()
				.selectFirst();
			if (uploadTitle.getText() == null || uploadTitle.getText()
				.isEmpty()) {
				final String file = files.get(0)
					.getAbsolutePath();
				int index = file.lastIndexOf(".");
				if (index == -1) {
					index = file.length();
				}
				uploadTitle.setText(file.substring(file.lastIndexOf(File.separator) + 1, index));
			}
		}
	}

	@FXML
	void openThumbnail(final ActionEvent event) {
		final File file = fileChooser.showOpenDialog(null);
		if (file != null) {
			uploadThumbnail.setText(file.getAbsolutePath());
		}
	}

	@FXML
	void refreshPlaylists(final ActionEvent event) {
		final RefreshPlaylistsCommand command = commandProvider.get(RefreshPlaylistsCommand.class);
		accountsList.toArray(command.accounts);
		command.start();
	}

	@FXML
	void removeTemplate(final ActionEvent event) {
		Template template = null;
		if ((template = templates.getSelectionModel()
			.getSelectedItem()) != null) {
			final RemoveTemplateCommand command = commandProvider.get(RemoveTemplateCommand.class);
			command.template = template;
			command.start();
		}
	}

	@FXML
	void resetUpload(final ActionEvent event) {
		uploadViewVO.reset();
		beanPathAdapter.setBean(uploadViewVO);
	}

	@FXML
	void saveTemplate(final ActionEvent event) {
		uploadViewVO.setTemplate(uploadViewVO.getTemplate());
		final UpdateTemplateCommand command = commandProvider.get(UpdateTemplateCommand.class);
		command.template = uploadViewVO.getTemplate();
		command.start();
	}

	@FXML
	void togglePartner(final ActionEvent event) {
		final Control[] controls = new Control[] { monetizeClaim, monetizeClaimType, monetizeClaimOption, monetizeEIDR, monetizeISAN,
				monetizeNumberEpisode, monetizeNumberSeason, monetizeTitleEpisode, monetizeTMSID, monetizeCustomID, monetizeNotes,
				monetizeDescription, monetizeTitle, monetizeInStreamDefaults, monetizeInStream, assetMovie, assetTV, assetWeb,
				labelContentInformation, labelEIDR, labelISAN, labelNumberEpisode, labelNumberSeason, labelTitleEpisode, labelTMSID,
				labelCustomId, labelMonetizeTitle, labelNote, labelDescription };
		if (monetizePartner.isSelected()) {
			for (final Control control : controls) {
				control.setVisible(true);
			}
			assetType.selectToggle(assetType.getSelectedToggle());
		} else {
			for (final Control control : controls) {
				control.setVisible(false);
			}
		}
	}

	@FXML
	void initialize() {
		assert uploadAccount != null : "fx:id=\"accountList\" was not injected: check your FXML file 'Upload.fxml'.";
		assert addUpload != null : "fx:id=\"addUpload\" was not injected: check your FXML file 'Upload.fxml'.";
		assert assetMovie != null : "fx:id=\"assetMovie\" was not injected: check your FXML file 'Upload.fxml'.";
		assert assetTV != null : "fx:id=\"assetTV\" was not injected: check your FXML file 'Upload.fxml'.";
		assert assetType != null : "fx:id=\"assetType\" was not injected: check your FXML file 'Upload.fxml'.";
		assert assetWeb != null : "fx:id=\"assetWeb\" was not injected: check your FXML file 'Upload.fxml'.";
		assert contentSyndication != null : "fx:id=\"contentSyndication\" was not injected: check your FXML file 'Upload.fxml'.";
		assert extendedSettingsGrid != null : "fx:id=\"extendedSettingsGrid\" was not injected: check your FXML file 'Upload.fxml'.";
		assert gridWidthSlider != null : "fx:id=\"gridWidthSlider\" was not injected: check your FXML file 'Upload.fxml'.";
		assert labelContentInformation != null : "fx:id=\"labelContentInformation\" was not injected: check your FXML file 'Upload.fxml'.";
		assert labelCustomId != null : "fx:id=\"labelCustomId\" was not injected: check your FXML file 'Upload.fxml'.";
		assert labelDescription != null : "fx:id=\"labelDescription\" was not injected: check your FXML file 'Upload.fxml'.";
		assert labelEIDR != null : "fx:id=\"labelEIDR\" was not injected: check your FXML file 'Upload.fxml'.";
		assert labelISAN != null : "fx:id=\"labelISAN\" was not injected: check your FXML file 'Upload.fxml'.";
		assert labelMonetizeTitle != null : "fx:id=\"labelMonetizeTitle\" was not injected: check your FXML file 'Upload.fxml'.";
		assert labelNote != null : "fx:id=\"labelNote\" was not injected: check your FXML file 'Upload.fxml'.";
		assert labelNumberEpisode != null : "fx:id=\"labelNumberEpisode\" was not injected: check your FXML file 'Upload.fxml'.";
		assert labelNumberSeason != null : "fx:id=\"labelNumberSeason\" was not injected: check your FXML file 'Upload.fxml'.";
		assert labelTMSID != null : "fx:id=\"labelTMSID\" was not injected: check your FXML file 'Upload.fxml'.";
		assert labelTitleEpisode != null : "fx:id=\"labelTitleEpisode\" was not injected: check your FXML file 'Upload.fxml'.";
		assert monetizeClaim != null : "fx:id=\"monetizeClaim\" was not injected: check your FXML file 'Upload.fxml'.";
		assert monetizeClaimOption != null : "fx:id=\"monetizeClaimOption\" was not injected: check your FXML file 'Upload.fxml'.";
		assert monetizeClaimType != null : "fx:id=\"monetizeClaimType\" was not injected: check your FXML file 'Upload.fxml'.";
		assert monetizeCustomID != null : "fx:id=\"monetizeCustomID\" was not injected: check your FXML file 'Upload.fxml'.";
		assert monetizeDescription != null : "fx:id=\"monetizeDescription\" was not injected: check your FXML file 'Upload.fxml'.";
		assert monetizeEIDR != null : "fx:id=\"monetizeEIDR\" was not injected: check your FXML file 'Upload.fxml'.";
		assert monetizeISAN != null : "fx:id=\"monetizeISAN\" was not injected: check your FXML file 'Upload.fxml'.";
		assert monetizeInStream != null : "fx:id=\"monetizeInStream\" was not injected: check your FXML file 'Upload.fxml'.";
		assert monetizeInStreamDefaults != null : "fx:id=\"monetizeInStreamDefaults\" was not injected: check your FXML file 'Upload.fxml'.";
		assert monetizeNotes != null : "fx:id=\"monetizeNotes\" was not injected: check your FXML file 'Upload.fxml'.";
		assert monetizeNumberEpisode != null : "fx:id=\"monetizeNumberEpisode\" was not injected: check your FXML file 'Upload.fxml'.";
		assert monetizeNumberSeason != null : "fx:id=\"monetizeNumberSeason\" was not injected: check your FXML file 'Upload.fxml'.";
		assert monetizeOverlay != null : "fx:id=\"monetizeOverlay\" was not injected: check your FXML file 'Upload.fxml'.";
		assert monetizePartner != null : "fx:id=\"monetizePartner\" was not injected: check your FXML file 'Upload.fxml'.";
		assert monetizeProductPlacement != null : "fx:id=\"monetizeProductPlacement\" was not injected: check your FXML file 'Upload.fxml'.";
		assert monetizeTMSID != null : "fx:id=\"monetizeTMSID\" was not injected: check your FXML file 'Upload.fxml'.";
		assert monetizeTitle != null : "fx:id=\"monetizeTitle\" was not injected: check your FXML file 'Upload.fxml'.";
		assert monetizeTitleEpisode != null : "fx:id=\"monetizeTitleEpisode\" was not injected: check your FXML file 'Upload.fxml'.";
		assert monetizeTrueView != null : "fx:id=\"monetizeTrueView\" was not injected: check your FXML file 'Upload.fxml'.";
		assert openDefaultdir != null : "fx:id=\"openDefaultdir\" was not injected: check your FXML file 'Upload.fxml'.";
		assert openEnddir != null : "fx:id=\"openEnddir\" was not injected: check your FXML file 'Upload.fxml'.";
		assert openFiles != null : "fx:id=\"openFiles\" was not injected: check your FXML file 'Upload.fxml'.";
		assert openThumbnail != null : "fx:id=\"openThumbnail\" was not injected: check your FXML file 'Upload.fxml'.";
		assert partnerPane != null : "fx:id=\"partnerPane\" was not injected: check your FXML file 'Upload.fxml'.";
		assert playlistDropScrollpane != null : "fx:id=\"playlistDropScrollpane\" was not injected: check your FXML file 'Upload.fxml'.";
		assert playlistGrid != null : "fx:id=\"playlistGrid\" was not injected: check your FXML file 'Upload.fxml'.";
		assert playlistSourceScrollpane != null : "fx:id=\"playlistSourceScrollpane\" was not injected: check your FXML file 'Upload.fxml'.";
		assert previewTitle != null : "fx:id=\"previewTitle\" was not injected: check your FXML file 'Upload.fxml'.";
		assert refreshPlaylists != null : "fx:id=\"refreshPlaylists\" was not injected: check your FXML file 'Upload.fxml'.";
		assert removeTemplate != null : "fx:id=\"removeTemplate\" was not injected: check your FXML file 'Upload.fxml'.";
		assert resetUpload != null : "fx:id=\"resetUpload\" was not injected: check your FXML file 'Upload.fxml'.";
		assert saveTemplate != null : "fx:id=\"saveTemplate\" was not injected: check your FXML file 'Upload.fxml'.";
		assert templates != null : "fx:id=\"templateList\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadCategory != null : "fx:id=\"uploadCategory\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadComment != null : "fx:id=\"uploadComment\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadCommentvote != null : "fx:id=\"uploadCommentvote\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadDefaultdir != null : "fx:id=\"uploadDefaultdir\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadDescription != null : "fx:id=\"uploadDescription\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadEmbed != null : "fx:id=\"uploadEmbed\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadEnddir != null : "fx:id=\"uploadEnddir\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadFacebook != null : "fx:id=\"uploadFacebook\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadFile != null : "fx:id=\"uploadFile\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadGrid != null : "fx:id=\"uploadGrid\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadLicense != null : "fx:id=\"uploadLicense\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadMessage != null : "fx:id=\"uploadMessage\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadMobile != null : "fx:id=\"uploadMobile\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadRate != null : "fx:id=\"uploadRate\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadTags != null : "fx:id=\"uploadTags\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadThumbnail != null : "fx:id=\"uploadThumbnail\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadTitle != null : "fx:id=\"uploadTitle\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadTwitter != null : "fx:id=\"uploadTwitter\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadVideoresponse != null : "fx:id=\"uploadVideoresponse\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadVisibility != null : "fx:id=\"uploadVisibility\" was not injected: check your FXML file 'Upload.fxml'.";
		assert x2 != null : "fx:id=\"x2\" was not injected: check your FXML file 'Upload.fxml'.";
		assert x5 != null : "fx:id=\"x5\" was not injected: check your FXML file 'Upload.fxml'.";

		initControls();
		initCustomFactories();
		initDragEventHandlers();
		initBindings();
		initSelection();
		initData();

		uploadViewVO.setTemplate(templatesList.size() > 0 ? templates.getValue() : ViewController.standardTemplate);
		uploadViewVO.setUpload(new Upload());
		beanPathAdapter.setBean(uploadViewVO);
	}

	private void initData() {

		visibilityList.addAll(Visibility.values());
		videoresponsesList.addAll(Videoresponse.values());
		commentsList.addAll(Comment.values());
		licensesList.addAll(License.values());
		claimTypesList.addAll(ClaimType.values());
		claimOptionsList.addAll(ClaimOption.values());

		// accountsList.addAll(accountDao.findAll());
		// templatesList.addAll(templateDao.findAll());
		//
		// try {
		// categoriesList.addAll(categoryService.load());
		// } catch (final SystemException e) {
		// final MonologFX dialog = new MonologFX(MonologFX.Type.ERROR);
		// dialog.setTitleText(resources.getString("categoryload.failed.title"));
		// dialog.setMessage(resources.getString("categoryload.failed.message"));
		// final MonologFXButton okButton = new MonologFXButton();
		// okButton.setType(MonologFXButton.Type.OK);
		// okButton.setLabel("OK");
		// dialog.addButton(okButton);
		// dialog.showDialog();
		// }
	}

	private void initControls() {
		extendedSettingsGrid.add(number, 1, 1, GridPane.REMAINING, 1);
		extendedSettingsGrid.add(started, 1, 12, GridPane.REMAINING, 1);
		extendedSettingsGrid.add(release, 1, 13, GridPane.REMAINING, 1);
		playlistSourceScrollpane.setContent(playlistSourcezone);
		playlistDropScrollpane.setContent(playlistTargetzone);
	}

	private void initCustomFactories() {
		playlistSourcezone.setCellFactory(new PlaylistSourceCellFactory());
		playlistTargetzone.setCellFactory(new PlaylistTargetCellFactory());
		uploadAccount.setConverter(new AccountListViewConverter());
		uploadFile.setConverter(new UploadFileListViewConverter());
		idProperty.addListener(new UploadIdInvalidationListener());

		monetizeClaimOption.selectionModelProperty()
			.addListener(new MonetizeClaimOptionInvalidationListener());
		assetType.selectedToggleProperty()
			.addListener(new AssetTypeInvalidaitonListener());
		monetizePartner.selectedProperty()
			.addListener(new MonetizePartnerInvalidationListener());
		uploadLicense.selectionModelProperty()
			.addListener(new LicenseInvalidationListener());

		uploadFile.setItems(filesList);
		uploadCategory.setItems(categoriesList);
		uploadAccount.setItems(accountsList);
		templates.setItems(templatesList);
		uploadVisibility.setItems(visibilityList);
		uploadComment.setItems(commentsList);
		uploadLicense.setItems(licensesList);
		uploadVideoresponse.setItems(videoresponsesList);
		monetizeClaimOption.setItems(claimOptionsList);
		monetizeClaimType.setItems(claimTypesList);
		playlistTargetzone.setItems(playlistTargetList);
		playlistSourcezone.setItems(playlistSourceList);

	}

	private void initDragEventHandlers() {
		final EventHandler<DragEvent> onDragOver = new DragOverCallback();
		final EventHandler<DragEvent> onDragDropped = new DragDroppedCallback();
		final EventHandler<DragEvent> onDragEntered = new DragEnteredCallback();
		final EventHandler<DragEvent> onDragExited = new DragExitedCallback();
		playlistTargetzone.setOnDragDropped(onDragDropped);
		playlistTargetzone.setOnDragEntered(onDragEntered);
		playlistTargetzone.setOnDragExited(onDragExited);
		playlistTargetzone.setOnDragOver(onDragOver);
		playlistSourcezone.setOnDragDropped(onDragDropped);
		playlistSourcezone.setOnDragEntered(onDragEntered);
		playlistSourcezone.setOnDragExited(onDragExited);
		playlistSourcezone.setOnDragOver(onDragOver);
	}

	private void initBindings() {
		release.disableProperty()
			.bind(uploadVisibility.getSelectionModel()
				.selectedIndexProperty()
				.isNotEqualTo(2)
				.and(uploadVisibility.getSelectionModel()
					.selectedIndexProperty()
					.isNotEqualTo(0)));
		uploadMessage.disableProperty()
			.bind(release.disableProperty());
		uploadFacebook.disableProperty()
			.bind(release.disableProperty());
		uploadTwitter.disableProperty()
			.bind(release.disableProperty());
		gridWidthSlider.minProperty()
			.set(1280);
		gridWidthSlider.maxProperty()
			.set(2000);
		playlistSourcezone.cellWidthProperty()
			.bind(gridWidthSlider.valueProperty()
				.divide(9));
		playlistSourcezone.cellHeightProperty()
			.bind(gridWidthSlider.valueProperty()
				.divide(16));
		playlistTargetzone.minWidthProperty()
			.bind(playlistDropScrollpane.widthProperty()
				.subtract(5));
		playlistTargetzone.prefHeightProperty()
			.bind(playlistDropScrollpane.heightProperty());
		playlistTargetzone.cellHeightProperty()
			.set(68);
		playlistTargetzone.cellWidthProperty()
			.set(120);
		playlistSourcezone.minHeightProperty()
			.bind(playlistSourceScrollpane.heightProperty()
				.subtract(5));
		playlistSourcezone.prefWidthProperty()
			.bind(playlistSourceScrollpane.widthProperty()
				.subtract(5));

		beanPathAdapter.bindBidirectional("template.commentvote", uploadCommentvote.selectedProperty());
		beanPathAdapter.bindBidirectional("template.defaultdir", uploadDefaultdir.textProperty());
		beanPathAdapter.bindBidirectional("template.description", uploadDescription.textProperty());
		beanPathAdapter.bindBidirectional("template.embed", uploadEmbed.selectedProperty());
		beanPathAdapter.bindBidirectional("template.enddir", uploadEnddir.textProperty());
		beanPathAdapter.bindBidirectional("template.mobile", uploadMobile.selectedProperty());
		beanPathAdapter.bindBidirectional("template.rate", uploadRate.selectedProperty());
		beanPathAdapter.bindBidirectional("template.keywords", uploadTags.textProperty());
		beanPathAdapter.bindBidirectional("template.title", uploadTitle.textProperty());
		beanPathAdapter.bindBidirectional("template.thumbnail", uploadThumbnail.textProperty());
		// *****************************************************************************************
		beanPathAdapter.bindBidirectional("template.category", uploadCategory.valueProperty(), AtomCategory.class);
		beanPathAdapter.bindBidirectional("template.comment", uploadComment.valueProperty(), Comment.class);
		beanPathAdapter.bindBidirectional("template.visibility", uploadVisibility.valueProperty(), Visibility.class);
		beanPathAdapter.bindBidirectional("template.videoresponse", uploadVideoresponse.valueProperty(), Videoresponse.class);
		beanPathAdapter.bindBidirectional("template.license", uploadLicense.valueProperty(), License.class);
		beanPathAdapter.bindBidirectional("template.monetizeclaimpolicy", monetizeClaimOption.valueProperty(), ClaimOption.class);
		beanPathAdapter.bindBidirectional("template.monetizeclaimtype", monetizeClaimType.valueProperty(), ClaimType.class);
		// *****************************************************************************************
		beanPathAdapter.bindBidirectional("template.claim", monetizeClaim.selectedProperty());
		beanPathAdapter.bindBidirectional("template.monetizeid", monetizeCustomID.textProperty());
		beanPathAdapter.bindBidirectional("template.monetizedescription", monetizeDescription.textProperty());
		beanPathAdapter.bindBidirectional("template.monetizeeidr", monetizeEIDR.textProperty());
		beanPathAdapter.bindBidirectional("template.instream", monetizeInStream.selectedProperty());
		beanPathAdapter.bindBidirectional("template.instreamdefaults", monetizeInStreamDefaults.selectedProperty());
		beanPathAdapter.bindBidirectional("template.monetizeisan", monetizeISAN.textProperty());
		beanPathAdapter.bindBidirectional("template.monetizenotes", monetizeNotes.textProperty());
		beanPathAdapter.bindBidirectional("template.monetizeepisodenb", monetizeNumberEpisode.textProperty());
		beanPathAdapter.bindBidirectional("template.monetizeseasonnb", monetizeNumberSeason.textProperty());
		beanPathAdapter.bindBidirectional("template.overlay", monetizeOverlay.selectedProperty());
		beanPathAdapter.bindBidirectional("template.product", monetizeProductPlacement.selectedProperty());
		beanPathAdapter.bindBidirectional("template.monetizetitle", monetizeTitle.textProperty());
		beanPathAdapter.bindBidirectional("template.monetizetitleepisode", monetizeTitleEpisode.textProperty());
		beanPathAdapter.bindBidirectional("template.monetizetmsid", monetizeTMSID.textProperty());
		beanPathAdapter.bindBidirectional("template.trueview", monetizeTrueView.selectedProperty());
		beanPathAdapter.bindBidirectional("template.monetizepartner", monetizePartner.selectedProperty());
		// *****************************************************************************************
		// *****************************************************************************************
		// *****************************************************************************************
		beanPathAdapter.bindBidirectional("upload.commentvote", uploadCommentvote.selectedProperty());
		beanPathAdapter.bindBidirectional("upload.description", uploadDescription.textProperty());
		beanPathAdapter.bindBidirectional("upload.embed", uploadEmbed.selectedProperty());
		beanPathAdapter.bindBidirectional("upload.enddir", uploadEnddir.textProperty());
		beanPathAdapter.bindBidirectional("upload.mobile", uploadMobile.selectedProperty());
		beanPathAdapter.bindBidirectional("upload.rate", uploadRate.selectedProperty());
		beanPathAdapter.bindBidirectional("upload.keywords", uploadTags.textProperty());
		beanPathAdapter.bindBidirectional("upload.title", uploadTitle.textProperty());
		beanPathAdapter.bindBidirectional("upload.thumbnail", uploadThumbnail.textProperty());
		beanPathAdapter.bindBidirectional("upload.dateOfStart", started.valueProperty(), Calendar.class);
		beanPathAdapter.bindBidirectional("upload.dateOfRelease", release.valueProperty(), Calendar.class);
		// *****************************************************************************************
		beanPathAdapter.bindBidirectional("upload.category", uploadCategory.valueProperty(), AtomCategory.class);
		beanPathAdapter.bindBidirectional("upload.comment", uploadComment.valueProperty(), Comment.class);
		beanPathAdapter.bindBidirectional("upload.visibility", uploadVisibility.valueProperty(), Visibility.class);
		beanPathAdapter.bindBidirectional("upload.videoresponse", uploadVideoresponse.valueProperty(), Videoresponse.class);
		beanPathAdapter.bindBidirectional("upload.license", uploadLicense.valueProperty(), License.class);
		beanPathAdapter.bindBidirectional("upload.monetizeclaimpolicy", monetizeClaimOption.valueProperty(), ClaimOption.class);
		beanPathAdapter.bindBidirectional("upload.monetizeclaimtype", monetizeClaimType.valueProperty(), ClaimType.class);
		// *****************************************************************************************
		beanPathAdapter.bindBidirectional("upload.facebook", uploadFacebook.selectedProperty());
		beanPathAdapter.bindBidirectional("upload.twitter", uploadTwitter.selectedProperty());
		beanPathAdapter.bindBidirectional("upload.message", uploadMessage.textProperty());
		// *****************************************************************************************
		beanPathAdapter.bindBidirectional("upload.claim", monetizeClaim.selectedProperty());
		beanPathAdapter.bindBidirectional("upload.monetizeid", monetizeCustomID.textProperty());
		beanPathAdapter.bindBidirectional("upload.monetizedescription", monetizeDescription.textProperty());
		beanPathAdapter.bindBidirectional("upload.monetizeeidr", monetizeEIDR.textProperty());
		beanPathAdapter.bindBidirectional("upload.instream", monetizeInStream.selectedProperty());
		beanPathAdapter.bindBidirectional("upload.instreamdefaults", monetizeInStreamDefaults.selectedProperty());
		beanPathAdapter.bindBidirectional("upload.monetizeisan", monetizeISAN.textProperty());
		beanPathAdapter.bindBidirectional("upload.monetizenotes", monetizeNotes.textProperty());
		beanPathAdapter.bindBidirectional("upload.monetizeepisodenb", monetizeNumberEpisode.textProperty());
		beanPathAdapter.bindBidirectional("upload.monetizeseasonnb", monetizeNumberSeason.textProperty());
		beanPathAdapter.bindBidirectional("upload.overlay", monetizeOverlay.selectedProperty());
		beanPathAdapter.bindBidirectional("upload.product", monetizeProductPlacement.selectedProperty());
		beanPathAdapter.bindBidirectional("upload.monetizetitle", monetizeTitle.textProperty());
		beanPathAdapter.bindBidirectional("upload.monetizetitleepisode", monetizeTitleEpisode.textProperty());
		beanPathAdapter.bindBidirectional("upload.monetizetmsid", monetizeTMSID.textProperty());
		beanPathAdapter.bindBidirectional("upload.trueview", monetizeTrueView.selectedProperty());
		beanPathAdapter.bindBidirectional("upload.monetizepartner", monetizePartner.selectedProperty());

	}

	private void initSelection() {

		final ChoiceBox<?>[] controls = new ChoiceBox[] { uploadVisibility, uploadComment, uploadLicense, uploadVideoresponse,
				uploadAccount, monetizeClaimOption, monetizeClaimType, uploadCategory, templates };

		for (final ChoiceBox<?> choiceBox : controls) {
			choiceBox.getSelectionModel()
				.selectFirst();
		}
	}

	/*
	 * private void initBindings() {
	 * // VIEW MODEL BINDINGS
	 * fileChooser.initialDirectoryProperty()
	 * .bindBidirectional(uploadViewModel.initialDirectoryProperty);
	 * directoryChooser.initialDirectoryProperty()
	 * .bindBidirectional(uploadViewModel.initialDirectoryProperty);
	 * previewTitle.textProperty()
	 * .bindBidirectional(uploadViewModel.previewTitleProperty);
	 * ***************************************************************
	 */

	public void movePlaylistToDropzone(final int model) {
		if (model >= 0) {
			playlistTargetList.add(playlistSourceList.get(model));
			playlistSourceList.remove(model);
		}
	}

	public void removePlaylistFromDropzone(final int model) {
		if (model >= 0) {
			playlistSourceList.add(playlistTargetList.get(model));
			playlistTargetList.remove(model);
		}
	}

	// {{ INNER CLASSES
	private final class LicenseInvalidationListener implements InvalidationListener {
		final CheckBox[]	controls	= new CheckBox[] { monetizeClaim, monetizeOverlay, monetizeTrueView, monetizeInStream,
												monetizeInStreamDefaults, monetizeProductPlacement };

		@Override
		public void invalidated(final Observable observable) {
			if (uploadLicense.getSelectionModel()
				.getSelectedIndex() == 1) {

				for (final CheckBox checkBox : controls) {
					checkBox.setSelected(false);
				}
				partnerPane.setDisable(true);
			} else {
				partnerPane.setDisable(false);
			}
		}
	}

	private final class MonetizeClaimOptionInvalidationListener implements InvalidationListener {
		final CheckBox[]	controls	= new CheckBox[] { monetizeClaim, monetizeOverlay, monetizeTrueView, monetizeInStream,
												monetizeInStreamDefaults, monetizeProductPlacement };

		@Override
		public void invalidated(final Observable observable) {
			if (!monetizePartner.isSelected()) {
				return;
			}
			if (monetizeClaimOption.getSelectionModel()
				.getSelectedIndex() == 0) {
				for (final CheckBox checkBox : controls) {
					checkBox.setVisible(true);
				}
			} else {
				for (final CheckBox checkBox : controls) {
					checkBox.setSelected(false);
					checkBox.setVisible(false);
				}
			}
		}
	}

	private final class AssetTypeInvalidaitonListener implements InvalidationListener {
		@Override
		public void invalidated(final Observable observable) {
			if (assetType.getSelectedToggle() == null) {
				return;
			}
			if (assetType.getSelectedToggle()
				.equals(assetWeb)) {
				monetizeDescription.setDisable(false);

				monetizeEIDR.setVisible(false);
				monetizeISAN.setVisible(false);
				monetizeNumberEpisode.setVisible(false);
				monetizeNumberSeason.setVisible(false);
				monetizeTitleEpisode.setVisible(false);
				monetizeTMSID.setVisible(false);
				labelEIDR.setVisible(false);
				labelISAN.setVisible(false);
				labelNumberEpisode.setVisible(false);
				labelNumberSeason.setVisible(false);
				labelTitleEpisode.setVisible(false);
				labelTMSID.setVisible(false);

			} else if (assetType.getSelectedToggle()
				.equals(assetTV)) {
				monetizeDescription.setDisable(true);

				monetizeEIDR.setVisible(true);
				monetizeISAN.setVisible(true);
				monetizeNumberEpisode.setVisible(true);
				monetizeNumberSeason.setVisible(true);
				monetizeTitleEpisode.setVisible(true);
				monetizeTMSID.setVisible(true);
				labelEIDR.setVisible(true);
				labelISAN.setVisible(true);
				labelNumberEpisode.setVisible(true);
				labelNumberSeason.setVisible(true);
				labelTitleEpisode.setVisible(true);
				labelTMSID.setVisible(true);
			} else if (assetType.getSelectedToggle()
				.equals(assetMovie)) {
				monetizeDescription.setDisable(false);

				monetizeEIDR.setVisible(true);
				monetizeISAN.setVisible(true);
				monetizeNumberEpisode.setVisible(true);
				monetizeNumberSeason.setVisible(true);
				monetizeTitleEpisode.setVisible(true);
				monetizeTMSID.setVisible(true);
				labelEIDR.setVisible(true);
				labelISAN.setVisible(true);
				labelTMSID.setVisible(true);
				labelNumberEpisode.setVisible(false);
				labelNumberSeason.setVisible(false);
				labelTitleEpisode.setVisible(false);
			}
		}
	}

	private final class MonetizePartnerInvalidationListener implements InvalidationListener {
		@Override
		public void invalidated(final Observable arg0) {
			togglePartner(null);
		}
	}

	private final class UploadIdInvalidationListener implements InvalidationListener {
		@Override
		public void invalidated(final Observable arg0) {
			if (idProperty.get() == -1) {
				addUpload.setText(resources.getString("button.addUpload"));
				addUpload.setId("addUpload");
			} else {
				addUpload.setText(resources.getString("button.saveUpload"));
				addUpload.setId("saveUpload");
			}
		}
	}

	private final class UploadFileListViewConverter extends StringConverter<File> {
		@Override
		public String toString(final File object) {
			if (object.getPath()
				.length() > 50) {
				final String fileName = object.getPath();
				return fileName.substring(0, fileName.indexOf(File.separatorChar, fileName.indexOf(File.separatorChar)))
					.concat(File.separator)
					.concat("...")
					.concat(fileName.substring(fileName.lastIndexOf(File.separatorChar, fileName.length())));
			}
			return object.getPath();
		}

		@Override
		public File fromString(final String string) {
			throw new RuntimeException("This method is not implemented: uploadFile is readonly!");
		}
	}

	private final class AccountListViewConverter extends StringConverter<Account> {
		@Override
		public String toString(final Account arg0) {
			return arg0.getName();
		}

		@Override
		public Account fromString(final String arg0) {
			throw new UnsupportedClassVersionError();
		}
	}

	private final class PlaylistTargetCellFactory implements Callback<GridView<Playlist>, GridCell<Playlist>> {
		@Override
		public PlaylistGridCell call(final GridView<Playlist> arg0) {
			final PlaylistGridCell cell = new PlaylistGridCell();
			cell.setOnDragDetected(new EventHandler<Event>() {
				@Override
				public void handle(final Event event) {
					final Dragboard db = playlistTargetzone.startDragAndDrop(TransferMode.ANY);
					final ClipboardContent content = new ClipboardContent();
					content.putString(playlistTargetList.indexOf(cell.itemProperty()
						.get()) + "");
					db.setContent(content);
					event.consume();
				}
			});
			cell.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(final MouseEvent event) {
					if (event.getClickCount() == 2) {
						removePlaylistFromDropzone(playlistTargetList.indexOf(cell.itemProperty()
							.get()));
					}
				}
			});
			return cell;
		}
	}

	private final class PlaylistSourceCellFactory implements Callback<GridView<Playlist>, GridCell<Playlist>> {
		@Override
		public PlaylistGridCell call(final GridView<Playlist> arg0) {
			final PlaylistGridCell cell = new PlaylistGridCell();
			cell.setOnDragDetected(new EventHandler<Event>() {
				@Override
				public void handle(final Event event) {
					final Dragboard db = playlistSourcezone.startDragAndDrop(TransferMode.ANY);
					final ClipboardContent content = new ClipboardContent();
					content.putString(playlistSourceList.indexOf(cell.itemProperty()
						.get()) + "");
					db.setContent(content);
					event.consume();
				}
			});
			cell.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(final MouseEvent event) {
					if (event.getClickCount() == 2) {
						movePlaylistToDropzone(playlistSourceList.indexOf(cell.itemProperty()
							.get()));
					}
				}
			});
			return cell;
		}
	}

	private final class DragExitedCallback implements EventHandler<DragEvent> {
		@Override
		public void handle(final DragEvent event) {
			((Node) event.getTarget()).getParent()
				.getParent()
				.getParent()
				.getStyleClass()
				.clear();
			((Node) event.getTarget()).getParent()
				.getParent()
				.getParent()
				.getStyleClass()
				.add("dropzone");
			event.consume();
		}
	}

	private final class DragEnteredCallback implements EventHandler<DragEvent> {
		@Override
		public void handle(final DragEvent event) {
			if ((event.getGestureSource() != event.getTarget() && event.getTarget() == playlistTargetzone || event.getTarget() == playlistSourcezone)
					&& event.getDragboard()
						.hasString()) {
				((Node) event.getTarget()).getParent()
					.getParent()
					.getParent()
					.getStyleClass()
					.clear();
				((Node) event.getTarget()).getParent()
					.getParent()
					.getParent()
					.getStyleClass()
					.add("dragentered");
			}
			event.consume();
		}
	}

	private final class DragDroppedCallback implements EventHandler<DragEvent> {
		@Override
		public void handle(final DragEvent event) {
			final Dragboard db = event.getDragboard();
			boolean success = false;
			if (db.hasString()) {
				if (((Node) event.getTarget()).getParent() == playlistTargetzone && event.getGestureSource() != playlistTargetzone) {
					movePlaylistToDropzone(Integer.parseInt(db.getString()));
					success = true;
				} else if (((Node) event.getTarget()).getParent() == playlistSourcezone && event.getGestureSource() != playlistSourcezone) {
					removePlaylistFromDropzone(Integer.parseInt(db.getString()));
					success = true;
				}
			}
			event.setDropCompleted(success);
			event.consume();
		}
	}

	private final class DragOverCallback implements EventHandler<DragEvent> {
		@Override
		public void handle(final DragEvent event) {
			if (event.getGestureSource() != event.getTarget() && event.getDragboard()
				.hasString()) {
				event.acceptTransferModes(TransferMode.ANY);
			}
			event.consume();
		}
	}
	// }} INNER CLASSES

}
