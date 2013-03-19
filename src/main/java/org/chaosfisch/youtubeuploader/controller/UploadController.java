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

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
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
import jfxtras.labs.dialogs.MonologFX;
import jfxtras.labs.dialogs.MonologFXButton;
import jfxtras.labs.scene.control.CalendarTextField;
import jfxtras.labs.scene.control.ListSpinner;
import jfxtras.labs.scene.control.grid.GridCell;
import jfxtras.labs.scene.control.grid.GridView;
import jfxtras.labs.scene.control.grid.GridViewBuilder;

import org.chaosfisch.google.atom.AtomCategory;
import org.chaosfisch.youtubeuploader.I18nHelper;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Account;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;
import org.chaosfisch.youtubeuploader.grid.cell.PlaylistGridCell;
import org.chaosfisch.youtubeuploader.services.youtube.CategoryService;
import org.chaosfisch.youtubeuploader.vo.UploadViewModel;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.inject.Inject;

public class UploadController implements Initializable {

	// {{ ViewElements

	@FXML
	private ResourceBundle				resources;
	@FXML
	private URL							location;
	@FXML
	private ChoiceBox<Account>			accountList;
	@FXML
	private Button						addUpload;
	@FXML
	private RadioButton					assetMovie;
	@FXML
	private RadioButton					assetTV;
	@FXML
	private ToggleGroup					assetType;
	@FXML
	private RadioButton					assetWeb;
	@FXML
	private ToggleGroup					contentSyndication;
	@FXML
	private GridPane					extendedSettingsGrid;
	@FXML
	private Slider						gridWidthSlider;
	@FXML
	private Label						labelContentInformation;
	@FXML
	private Label						labelCustomId;
	@FXML
	private Label						labelDescription;
	@FXML
	private Label						labelEIDR;
	@FXML
	private Label						labelISAN;
	@FXML
	private Label						labelMonetizeTitle;
	@FXML
	private Label						labelNote;
	@FXML
	private Label						labelNumberEpisode;
	@FXML
	private Label						labelNumberSeason;
	@FXML
	private Label						labelTMSID;
	@FXML
	private Label						labelTitleEpisode;
	@FXML
	private CheckBox					monetizeClaim;
	@FXML
	private ChoiceBox<String>			monetizeClaimOption;
	@FXML
	private ChoiceBox<String>			monetizeClaimType;
	@FXML
	private TextField					monetizeCustomID;
	@FXML
	private TextField					monetizeDescription;
	@FXML
	private TextField					monetizeEIDR;
	@FXML
	private TextField					monetizeISAN;
	@FXML
	private CheckBox					monetizeInStream;
	@FXML
	private CheckBox					monetizeInStreamDefaults;
	@FXML
	private TextField					monetizeNotes;
	@FXML
	private TextField					monetizeNumberEpisode;
	@FXML
	private TextField					monetizeNumberSeason;
	@FXML
	private CheckBox					monetizeOverlay;
	@FXML
	private ToggleButton				monetizePartner;
	@FXML
	private CheckBox					monetizeProductPlacement;
	@FXML
	private TextField					monetizeTMSID;
	@FXML
	private TextField					monetizeTitle;
	@FXML
	private TextField					monetizeTitleEpisode;
	@FXML
	private CheckBox					monetizeTrueView;
	@FXML
	private Button						openDefaultdir;
	@FXML
	private Button						openEnddir;
	@FXML
	private Button						openFiles;
	@FXML
	private Button						openThumbnail;
	@FXML
	private TitledPane					partnerPane;
	@FXML
	private ScrollPane					playlistDropScrollpane;
	@FXML
	private GridPane					playlistGrid;
	@FXML
	private ScrollPane					playlistSourceScrollpane;
	@FXML
	private TextField					previewTitle;
	@FXML
	private Button						refreshPlaylists;
	@FXML
	private Button						removeTemplate;
	@FXML
	private Button						resetUpload;
	@FXML
	private Button						saveTemplate;
	@FXML
	private ChoiceBox<Template>			templateList;
	@FXML
	private ChoiceBox<AtomCategory>		uploadCategory;
	@FXML
	private ChoiceBox<String>			uploadComment;
	@FXML
	private CheckBox					uploadCommentvote;
	@FXML
	private TextField					uploadDefaultdir;
	@FXML
	private TextArea					uploadDescription;
	@FXML
	private CheckBox					uploadEmbed;
	@FXML
	private TextField					uploadEnddir;
	@FXML
	private CheckBox					uploadFacebook;
	@FXML
	private ChoiceBox<File>				uploadFile;
	@FXML
	private GridPane					uploadGrid;
	@FXML
	private ChoiceBox<String>			uploadLicense;
	@FXML
	private TextArea					uploadMessage;
	@FXML
	private CheckBox					uploadMobile;
	@FXML
	private CheckBox					uploadRate;
	@FXML
	private TextArea					uploadTags;
	@FXML
	private TextField					uploadThumbnail;
	@FXML
	private TextField					uploadTitle;
	@FXML
	private CheckBox					uploadTwitter;
	@FXML
	private ChoiceBox<String>			uploadVideoresponse;
	@FXML
	private ChoiceBox<String>			uploadVisibility;
	@FXML
	private Label						validationText;

	private final CalendarTextField		starttime			= new CalendarTextField().withValue(Calendar.getInstance())
																.withDateFormat(new SimpleDateFormat("dd.MM.yyyy HH:mm"))
																.withShowTime(Boolean.TRUE);
	private final CalendarTextField		releasetime			= new CalendarTextField().withValue(Calendar.getInstance())
																.withDateFormat(new SimpleDateFormat("dd.MM.yyyy HH:mm"))
																.withShowTime(Boolean.TRUE);
	private final ListSpinner<Integer>	number				= new ListSpinner<Integer>(-1000,
																1000).withValue(0)
																.withAlignment(Pos.CENTER_RIGHT);

	private final GridView<Playlist>	playlistSourcezone	= GridViewBuilder.create(Playlist.class)
																.build();
	private final GridView<Playlist>	playlistDropzone	= GridViewBuilder.create(Playlist.class)
																.build();

	// }} ViewElements

	@Inject
	private CategoryService				categoryService;
	@Inject
	private FileChooser					fileChooser;
	@Inject
	private DirectoryChooser			directoryChooser;
	@Inject
	private UploadViewModel				uploadViewModel;

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(final URL fxmlFileLocation, final ResourceBundle resources) {
		// {{ ViewElementsExistanceCheck
		assert accountList != null : "fx:id=\"accountList\" was not injected: check your FXML file 'Upload.fxml'.";
		assert addUpload != null : "fx:id=\"addUpload\" was not injected: check your FXML file 'Upload.fxml'.";
		assert assetMovie != null : "fx:id=\"assetMovie\" was not injected: check your FXML file 'Upload.fxml'.";
		assert assetTV != null : "fx:id=\"assetTV\" was not injected: check your FXML file 'Upload.fxml'.";
		assert assetType != null : "fx:id=\"assetType\" was not injected: check your FXML file 'Upload.fxml'.";
		assert assetWeb != null : "fx:id=\"assetWeb\" was not injected: check your FXML file 'Upload.fxml'.";
		assert contentSyndication != null : "fx:id=\"contentSyndication\" was not injected: check your FXML file 'Upload.fxml'.";
		assert extendedSettingsGrid != null : "fx:id=\"extendedSettingsGrid\" was not injected: check your FXML file 'Upload.fxml'.";
		assert gridWidthSlider != null : "fx:id=\"gridWidthSlider\" was not injected: check your FXML file 'Upload.fxml'.";
		assert labelContentInformation != null : "fx:id=\"labelContentInformation\" was not injected: check your FXML file 'Upload.fxml'.";
		assert labelEIDR != null : "fx:id=\"labelEIDR\" was not injected: check your FXML file 'Upload.fxml'.";
		assert labelISAN != null : "fx:id=\"labelISAN\" was not injected: check your FXML file 'Upload.fxml'.";
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
		assert templateList != null : "fx:id=\"templateList\" was not injected: check your FXML file 'Upload.fxml'.";
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
		assert validationText != null : "fx:id=\"validationText\" was not injected: check your FXML file 'Upload.fxml'.";
		// }} ViewElementsExistanceCheck
		initControls();
		initBindings();
		initCustomFactories();
		initDragEventHandlers();
		initSelection();
	}

	private void initControls() {
		extendedSettingsGrid.add(number, 1, 1, GridPane.REMAINING, 1);
		extendedSettingsGrid.add(starttime, 1, 12, GridPane.REMAINING, 1);
		extendedSettingsGrid.add(releasetime, 1, 13, GridPane.REMAINING, 1);

		playlistSourceScrollpane.setContent(playlistSourcezone);
		playlistDropScrollpane.setContent(playlistDropzone);
	}

	private void initDragEventHandlers() {
		final EventHandler<DragEvent> onDragOver = new DragOverCallback();
		final EventHandler<DragEvent> onDragDropped = new DragDroppedCallback();
		final EventHandler<DragEvent> onDragEntered = new DragEnteredCallback();
		final EventHandler<DragEvent> onDragExited = new DragExitedCallback();

		playlistDropzone.setOnDragDropped(onDragDropped);
		playlistDropzone.setOnDragEntered(onDragEntered);
		playlistDropzone.setOnDragExited(onDragExited);
		playlistDropzone.setOnDragOver(onDragOver);

		playlistSourcezone.setOnDragDropped(onDragDropped);
		playlistSourcezone.setOnDragEntered(onDragEntered);
		playlistSourcezone.setOnDragExited(onDragExited);
		playlistSourcezone.setOnDragOver(onDragOver);
	}

	private void initCustomFactories() {
		playlistSourcezone.setCellFactory(new PlaylistSourceCellFactory());
		playlistDropzone.setCellFactory(new PlaylistDropCellFactory());
		accountList.setConverter(new AccountListViewConverter());
		uploadFile.converterProperty()
			.set(new UploadFileListViewConverter());
		uploadViewModel.idProperty.addListener(new UploadIdInvalidationListener());
	}

	private void initSelection() {
		uploadVisibility.getSelectionModel()
			.selectFirst();
		uploadComment.getSelectionModel()
			.selectFirst();
		uploadLicense.getSelectionModel()
			.selectFirst();
		uploadVideoresponse.getSelectionModel()
			.selectFirst();
		accountList.getSelectionModel()
			.selectFirst();
		monetizeClaimOption.getSelectionModel()
			.selectFirst();
		monetizeClaimType.getSelectionModel()
			.selectFirst();
		Futures.addCallback(categoryService.load(), new FutureCallback<List<AtomCategory>>() {

			@Override
			public void onSuccess(final List<AtomCategory> result) {
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						uploadViewModel.categoryProperty.set(FXCollections.observableList(result));
						uploadCategory.getSelectionModel()
							.selectFirst();
						templateList.getSelectionModel()
							.selectFirst();
					}
				});
			}

			@Override
			public void onFailure(final Throwable t) {
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						final MonologFX dialog = new MonologFX(MonologFX.Type.ERROR);
						dialog.setTitleText(I18nHelper.message("categoryload.failed.title"));
						dialog.setMessage(I18nHelper.message("categoryload.failed.message"));
						final MonologFXButton okButton = new MonologFXButton();
						okButton.setType(MonologFXButton.Type.OK);
						okButton.setLabel("OK");
						dialog.addButton(okButton);
						dialog.showDialog();
					}
				});
			}
		});
	}

	private void initBindings() {

		releasetime.disableProperty()
			.bind(uploadVisibility.getSelectionModel()
				.selectedIndexProperty()
				.lessThan(2));
		uploadMessage.disableProperty()
			.bind(releasetime.disableProperty());
		uploadFacebook.disableProperty()
			.bind(releasetime.disableProperty());
		uploadTwitter.disableProperty()
			.bind(releasetime.disableProperty());

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
		playlistDropzone.minWidthProperty()
			.bind(playlistDropScrollpane.widthProperty()
				.subtract(5));
		playlistDropzone.prefHeightProperty()
			.bind(playlistDropScrollpane.heightProperty());
		playlistDropzone.cellHeightProperty()
			.set(68);
		playlistDropzone.cellWidthProperty()
			.set(120);

		playlistSourcezone.minHeightProperty()
			.bind(playlistSourceScrollpane.heightProperty()
				.subtract(5));
		playlistSourcezone.prefWidthProperty()
			.bind(playlistSourceScrollpane.widthProperty()
				.subtract(5));

		// VIEW MODEL BINDINGS

		uploadViewModel.init(uploadCategory.getSelectionModel(), uploadFile.getSelectionModel(), accountList.getSelectionModel(),
			uploadComment.getSelectionModel(), uploadLicense.getSelectionModel(), uploadVideoresponse.getSelectionModel(),
			uploadVisibility.getSelectionModel(), templateList.getSelectionModel(), monetizeClaimType.getSelectionModel(),
			monetizeClaimOption.getSelectionModel(), assetType.selectedToggleProperty(), contentSyndication.selectedToggleProperty());

		fileChooser.initialDirectoryProperty()
			.bindBidirectional(uploadViewModel.initialDirectoryProperty);
		directoryChooser.initialDirectoryProperty()
			.bindBidirectional(uploadViewModel.initialDirectoryProperty);
		templateList.itemsProperty()
			.bindBidirectional(uploadViewModel.templateProperty);
		accountList.itemsProperty()
			.bindBidirectional(uploadViewModel.accountProperty);
		playlistDropzone.itemsProperty()
			.bindBidirectional(uploadViewModel.playlistDropListProperty);
		playlistSourcezone.itemsProperty()
			.bindBidirectional(uploadViewModel.playlistSourceListProperty);
		previewTitle.textProperty()
			.bindBidirectional(uploadViewModel.previewTitleProperty);
		uploadCategory.itemsProperty()
			.bindBidirectional(uploadViewModel.categoryProperty);
		uploadComment.itemsProperty()
			.bindBidirectional(uploadViewModel.commentProperty);
		uploadCommentvote.selectedProperty()
			.bindBidirectional(uploadViewModel.commentVoteProperty);
		uploadDefaultdir.textProperty()
			.bindBidirectional(uploadViewModel.defaultdirProperty);
		uploadDescription.textProperty()
			.bindBidirectional(uploadViewModel.descriptionProperty);
		uploadEmbed.selectedProperty()
			.bindBidirectional(uploadViewModel.embedProperty);
		uploadEnddir.textProperty()
			.bindBidirectional(uploadViewModel.enddirProperty);
		uploadFile.itemsProperty()
			.bindBidirectional(uploadViewModel.fileProperty);
		uploadLicense.itemsProperty()
			.bindBidirectional(uploadViewModel.licenseProperty);
		uploadMobile.selectedProperty()
			.bindBidirectional(uploadViewModel.mobileProperty);
		uploadRate.selectedProperty()
			.bindBidirectional(uploadViewModel.rateProperty);
		uploadTags.textProperty()
			.bindBidirectional(uploadViewModel.tagsProperty);
		uploadTitle.textProperty()
			.bindBidirectional(uploadViewModel.titleProperty);
		uploadThumbnail.textProperty()
			.bindBidirectional(uploadViewModel.thumbnailProperty);
		uploadVideoresponse.itemsProperty()
			.bindBidirectional(uploadViewModel.videoresponseProperty);
		uploadVisibility.itemsProperty()
			.bindBidirectional(uploadViewModel.visibilityProperty);
		starttime.valueProperty()
			.bindBidirectional(uploadViewModel.starttimeProperty);
		releasetime.valueProperty()
			.bindBidirectional(uploadViewModel.releasetimeProperty);
		uploadFacebook.selectedProperty()
			.bindBidirectional(uploadViewModel.facebookProperty);
		uploadTwitter.selectedProperty()
			.bindBidirectional(uploadViewModel.twitterProperty);
		uploadMessage.textProperty()
			.bindBidirectional(uploadViewModel.messageProperty);

		// VIEW MODEL MONETIZE BINDINGS
		uploadViewModel.selectedLicenseProperty.getValue()
			.selectedIndexProperty()
			.addListener(new LicenseInvalidationListener());
		monetizeClaimOption.selectionModelProperty()
			.get()
			.selectedIndexProperty()
			.addListener(new MonetizeClaimOptionInvalidationListener());
		assetType.selectedToggleProperty()
			.addListener(new AssetTypeInvalidaitonListener());

		monetizeClaim.selectedProperty()
			.bindBidirectional(uploadViewModel.claimProperty);
		monetizeClaimType.itemsProperty()
			.bindBidirectional(uploadViewModel.claimTypeProperty);
		monetizeClaimOption.itemsProperty()
			.bindBidirectional(uploadViewModel.claimOptionsProperty);
		monetizeCustomID.textProperty()
			.bindBidirectional(uploadViewModel.customidProperty);
		monetizeDescription.textProperty()
			.bindBidirectional(uploadViewModel.monetizeDescriptionProperty);
		monetizeEIDR.textProperty()
			.bindBidirectional(uploadViewModel.eidrProperty);
		monetizeInStream.selectedProperty()
			.bindBidirectional(uploadViewModel.inStreamProperty);
		monetizeInStreamDefaults.selectedProperty()
			.bindBidirectional(uploadViewModel.inStreamDefaultsProperty);
		monetizeISAN.textProperty()
			.bindBidirectional(uploadViewModel.isanProperty);
		monetizeNotes.textProperty()
			.bindBidirectional(uploadViewModel.monetizeNotesProperty);
		monetizeNumberEpisode.textProperty()
			.bindBidirectional(uploadViewModel.numberEpisodeProperty);
		monetizeNumberSeason.textProperty()
			.bindBidirectional(uploadViewModel.numberSeasonProperty);
		monetizeOverlay.selectedProperty()
			.bindBidirectional(uploadViewModel.overlayProperty);
		monetizeProductPlacement.selectedProperty()
			.bindBidirectional(uploadViewModel.productPlacementProperty);
		monetizeTitle.textProperty()
			.bindBidirectional(uploadViewModel.monetizeTitleProperty);
		monetizeTitleEpisode.textProperty()
			.bindBidirectional(uploadViewModel.monetizeTitleEpisodeProperty);
		monetizeTMSID.textProperty()
			.bindBidirectional(uploadViewModel.tmsidProperty);
		monetizeTrueView.selectedProperty()
			.bindBidirectional(uploadViewModel.trueViewProperty);
		monetizePartner.selectedProperty()
			.bindBidirectional(uploadViewModel.partnerProperty);

		monetizePartner.selectedProperty()
			.addListener(new MonetizePartnerInvalidationListener());
	}

	// Handler for ToggleButton[fx:id="monetizePartner"] onAction
	@FXML
	void togglePartner(final ActionEvent event) {
		if (monetizePartner.isSelected()) {
			monetizeClaim.visibleProperty()
				.set(true);
			monetizeClaimType.visibleProperty()
				.set(true);
			monetizeClaimOption.visibleProperty()
				.set(true);
			monetizeEIDR.visibleProperty()
				.set(true);
			monetizeISAN.visibleProperty()
				.set(true);
			monetizeNumberEpisode.visibleProperty()
				.set(true);
			monetizeNumberSeason.visibleProperty()
				.set(true);
			monetizeTitleEpisode.visibleProperty()
				.set(true);
			monetizeTMSID.visibleProperty()
				.set(true);
			monetizeCustomID.visibleProperty()
				.set(true);
			monetizeNotes.visibleProperty()
				.set(true);
			monetizeDescription.visibleProperty()
				.set(true);
			monetizeTitle.visibleProperty()
				.set(true);
			monetizeInStreamDefaults.visibleProperty()
				.set(true);
			monetizeInStream.visibleProperty()
				.set(true);
			assetMovie.visibleProperty()
				.set(true);
			assetTV.visibleProperty()
				.set(true);
			assetWeb.visibleProperty()
				.set(true);
			labelContentInformation.visibleProperty()
				.set(true);
			labelEIDR.visibleProperty()
				.set(true);
			labelISAN.visibleProperty()
				.set(true);
			labelNumberEpisode.visibleProperty()
				.set(true);
			labelNumberSeason.visibleProperty()
				.set(true);
			labelTitleEpisode.visibleProperty()
				.set(true);
			labelTMSID.visibleProperty()
				.set(true);
			labelCustomId.visibleProperty()
				.set(true);
			labelMonetizeTitle.visibleProperty()
				.set(true);
			labelNote.visibleProperty()
				.set(true);
			labelDescription.visibleProperty()
				.set(true);
			assetType.selectToggle(assetType.getSelectedToggle());
		} else {
			monetizeClaim.visibleProperty()
				.set(false);
			monetizeClaimType.visibleProperty()
				.set(false);
			monetizeClaimOption.visibleProperty()
				.set(false);
			monetizeEIDR.visibleProperty()
				.set(false);
			monetizeISAN.visibleProperty()
				.set(false);
			monetizeNumberEpisode.visibleProperty()
				.set(false);
			monetizeNumberSeason.visibleProperty()
				.set(false);
			monetizeTitleEpisode.visibleProperty()
				.set(false);
			monetizeTMSID.visibleProperty()
				.set(false);
			monetizeCustomID.visibleProperty()
				.set(false);
			monetizeNotes.visibleProperty()
				.set(false);
			monetizeDescription.visibleProperty()
				.set(false);
			monetizeTitle.visibleProperty()
				.set(false);
			monetizeInStreamDefaults.visibleProperty()
				.set(false);
			monetizeInStream.visibleProperty()
				.set(false);
			assetMovie.visibleProperty()
				.set(false);
			assetTV.visibleProperty()
				.set(false);
			assetWeb.visibleProperty()
				.set(false);
			labelContentInformation.visibleProperty()
				.set(false);
			labelEIDR.visibleProperty()
				.set(false);
			labelISAN.visibleProperty()
				.set(false);
			labelNumberEpisode.visibleProperty()
				.set(false);
			labelNumberSeason.visibleProperty()
				.set(false);
			labelTitleEpisode.visibleProperty()
				.set(false);
			labelTMSID.visibleProperty()
				.set(false);
			labelCustomId.visibleProperty()
				.set(false);
			labelMonetizeTitle.visibleProperty()
				.set(false);
			labelNote.visibleProperty()
				.set(false);
			labelDescription.visibleProperty()
				.set(false);

		}
	}

	// Handler for Button[fx:id="addUpload"] onAction
	@FXML
	public void addUpload(final ActionEvent event) {
		final Upload upload = uploadViewModel.toUpload();

		/*
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
		 * }
		 */
	}

	// Handler for Button[fx:id="openDefaultdir"] onAction
	@FXML
	public void openDefaultdir(final ActionEvent event) {
		final File directory = directoryChooser.showDialog(null);
		if (directory != null) {
			uploadDefaultdir.setText(directory.getAbsolutePath());
		}
	}

	// Handler for Button[fx:id="openEnddir"] onAction
	@FXML
	public void openEnddir(final ActionEvent event) {
		final File directory = directoryChooser.showDialog(null);
		if (directory != null) {
			uploadEnddir.setText(directory.getAbsolutePath());
		}
	}

	// Handler for Button[fx:id="openFiles"] onAction
	@FXML
	public void openFiles(final ActionEvent event) {
		final List<File> files = fileChooser.showOpenMultipleDialog(null);
		if (files != null) {
			addUploadFiles(files);
		}
	}

	// Handler for Button[fx:id="openThumbnail"] onAction
	@FXML
	void openThumbnail(final ActionEvent event) {
		final File file = fileChooser.showOpenDialog(null);
		if (file != null) {
			uploadThumbnail.setText(file.getAbsolutePath());
		}
	}

	public void addUploadFiles(final List<File> files) {
		uploadFile.getItems()
			.clear();
		uploadFile.getItems()
			.addAll(files);
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

	// Handler for Button[fx:id="refreshPlaylists"] onAction
	@FXML
	public void refreshPlaylists(final ActionEvent event) {
		uploadViewModel.refreshPlaylists();
	}

	// Handler for Button[fx:id="removeTemplate"] onAction
	@FXML
	public void removeTemplate(final ActionEvent event) {
		uploadViewModel.removeTemplate();
	}

	// Handler for Button[fx:id="resetUpload"] onAction
	@FXML
	public void resetUpload(final ActionEvent event) {
		uploadViewModel.resetTemplate();
	}

	// Handler for Button[id="saveTemplate"] onAction
	@FXML
	public void saveTemplate(final ActionEvent event) {
		uploadViewModel.saveTemplate();
	}

	// {{ INNER CLASSES
	private final class LicenseInvalidationListener implements InvalidationListener {
		@Override
		public void invalidated(final Observable observable) {
			if (uploadViewModel.selectedLicenseProperty.getValue()
				.selectedIndexProperty()
				.get() == 1) {
				monetizeClaim.selectedProperty()
					.set(false);
				monetizeOverlay.selectedProperty()
					.set(false);
				monetizeTrueView.selectedProperty()
					.set(false);
				monetizeInStream.selectedProperty()
					.set(false);
				monetizeInStreamDefaults.selectedProperty()
					.set(false);
				monetizeProductPlacement.selectedProperty()
					.set(false);
				partnerPane.disableProperty()
					.set(true);
			} else {
				partnerPane.disableProperty()
					.set(false);
			}
		}
	}

	private final class MonetizeClaimOptionInvalidationListener implements InvalidationListener {
		@Override
		public void invalidated(final Observable observable) {
			if (!monetizePartner.isSelected()) {
				return;
			}
			if (monetizeClaimOption.selectionModelProperty()
				.get()
				.selectedIndexProperty()
				.get() == 0) {
				monetizeOverlay.visibleProperty()
					.set(true);
				monetizeTrueView.visibleProperty()
					.set(true);
				monetizeInStream.visibleProperty()
					.set(true);
				monetizeInStreamDefaults.visibleProperty()
					.set(true);
				monetizeProductPlacement.visibleProperty()
					.set(true);
			} else {
				monetizeOverlay.selectedProperty()
					.set(false);
				monetizeTrueView.selectedProperty()
					.set(false);
				monetizeInStream.selectedProperty()
					.set(false);
				monetizeInStreamDefaults.selectedProperty()
					.set(false);
				monetizeProductPlacement.selectedProperty()
					.set(false);
				monetizeOverlay.visibleProperty()
					.set(false);
				monetizeTrueView.visibleProperty()
					.set(false);
				monetizeInStream.visibleProperty()
					.set(false);
				monetizeInStreamDefaults.visibleProperty()
					.set(false);
				monetizeProductPlacement.visibleProperty()
					.set(false);
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
				monetizeEIDR.visibleProperty()
					.set(false);
				monetizeISAN.visibleProperty()
					.set(false);
				monetizeNumberEpisode.visibleProperty()
					.set(false);
				monetizeNumberSeason.visibleProperty()
					.set(false);
				monetizeTitleEpisode.visibleProperty()
					.set(false);
				monetizeTMSID.visibleProperty()
					.set(false);
				labelEIDR.visibleProperty()
					.set(false);
				labelISAN.visibleProperty()
					.set(false);
				labelNumberEpisode.visibleProperty()
					.set(false);
				labelNumberSeason.visibleProperty()
					.set(false);
				labelTitleEpisode.visibleProperty()
					.set(false);
				labelTMSID.visibleProperty()
					.set(false);
				monetizeDescription.disableProperty()
					.set(false);
			} else if (assetType.getSelectedToggle()
				.equals(assetTV)) {
				monetizeEIDR.visibleProperty()
					.set(true);
				monetizeISAN.visibleProperty()
					.set(true);
				monetizeNumberEpisode.visibleProperty()
					.set(true);
				monetizeNumberSeason.visibleProperty()
					.set(true);
				monetizeTitleEpisode.visibleProperty()
					.set(true);
				monetizeTMSID.visibleProperty()
					.set(true);
				labelEIDR.visibleProperty()
					.set(true);
				labelISAN.visibleProperty()
					.set(true);
				labelNumberEpisode.visibleProperty()
					.set(true);
				labelNumberSeason.visibleProperty()
					.set(true);
				labelTitleEpisode.visibleProperty()
					.set(true);
				labelTMSID.visibleProperty()
					.set(true);
				monetizeDescription.disableProperty()
					.set(true);
			} else if (assetType.getSelectedToggle()
				.equals(assetMovie)) {
				monetizeDescription.disableProperty()
					.set(false);
				monetizeEIDR.visibleProperty()
					.set(true);
				monetizeISAN.visibleProperty()
					.set(true);
				monetizeNumberEpisode.visibleProperty()
					.set(false);
				monetizeNumberSeason.visibleProperty()
					.set(false);
				monetizeTitleEpisode.visibleProperty()
					.set(false);
				monetizeTMSID.visibleProperty()
					.set(true);
				labelEIDR.visibleProperty()
					.set(true);
				labelISAN.visibleProperty()
					.set(true);
				labelTMSID.visibleProperty()
					.set(true);
				labelNumberEpisode.visibleProperty()
					.set(false);
				labelNumberSeason.visibleProperty()
					.set(false);
				labelTitleEpisode.visibleProperty()
					.set(false);
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
			if (uploadViewModel.idProperty.get() == -1) {
				addUpload.setText(I18nHelper.message("button.addUpload"));
				addUpload.setId("addUpload");
			} else {
				addUpload.setText(I18nHelper.message("button.saveUpload"));
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

	private final class PlaylistDropCellFactory implements Callback<GridView<Playlist>, GridCell<Playlist>> {
		@Override
		public PlaylistGridCell call(final GridView<Playlist> arg0) {
			final PlaylistGridCell cell = new PlaylistGridCell();

			cell.setOnDragDetected(new EventHandler<Event>() {

				@Override
				public void handle(final Event event) {
					final Dragboard db = playlistDropzone.startDragAndDrop(TransferMode.ANY);
					final ClipboardContent content = new ClipboardContent();
					content.putString(uploadViewModel.playlistDropListProperty.indexOf(cell.itemProperty()
						.get()) + "");
					db.setContent(content);
					event.consume();
				}
			});

			cell.setOnMouseClicked(new EventHandler<MouseEvent>() {

				@Override
				public void handle(final MouseEvent event) {
					if (event.getClickCount() == 2) {
						uploadViewModel.removePlaylistFromDropzone(uploadViewModel.playlistDropListProperty.indexOf(cell.itemProperty()
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
					content.putString(uploadViewModel.playlistSourceListProperty.indexOf(cell.itemProperty()
						.get()) + "");
					db.setContent(content);
					event.consume();
				}
			});

			cell.setOnMouseClicked(new EventHandler<MouseEvent>() {

				@Override
				public void handle(final MouseEvent event) {
					if (event.getClickCount() == 2) {
						uploadViewModel.movePlaylistToDropzone(uploadViewModel.playlistSourceListProperty.indexOf(cell.itemProperty()
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
			if ((event.getGestureSource() != event.getTarget() && event.getTarget() == playlistDropzone || event.getTarget() == playlistSourcezone)
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
				if (((Node) event.getTarget()).getParent() == playlistDropzone && event.getGestureSource() != playlistDropzone) {
					uploadViewModel.movePlaylistToDropzone(Integer.parseInt(db.getString()));
					success = true;
				} else if (((Node) event.getTarget()).getParent() == playlistSourcezone && event.getGestureSource() != playlistSourcezone) {
					uploadViewModel.removePlaylistFromDropzone(Integer.parseInt(db.getString()));
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
