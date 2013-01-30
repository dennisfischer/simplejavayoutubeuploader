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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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

import javax.sql.DataSource;

import jfxtras.labs.dialogs.MonologFX;
import jfxtras.labs.dialogs.MonologFXButton;
import jfxtras.labs.scene.control.CalendarTextField;
import jfxtras.labs.scene.control.ListSpinner;
import jfxtras.labs.scene.control.grid.GridCell;
import jfxtras.labs.scene.control.grid.GridView;
import jfxtras.labs.scene.control.grid.GridViewBuilder;

import org.chaosfisch.google.atom.AtomCategory;
import org.chaosfisch.util.ThreadUtil;
import org.chaosfisch.youtubeuploader.I18nHelper;
import org.chaosfisch.youtubeuploader.grid.cell.PlaylistGridCell;
import org.chaosfisch.youtubeuploader.models.Upload;
import org.chaosfisch.youtubeuploader.services.youtube.spi.CategoryService;
import org.chaosfisch.youtubeuploader.view.models.UploadViewModel;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.Model;

import com.google.inject.Inject;

public class UploadController implements Initializable {
	@FXML// fx:id="accountList"
	private ChoiceBox<Model>			accountList;

	@FXML// fx:id="addUpload"
	private Button						addUpload;

	@FXML// fx:id="extendedSettingsGrid"
	private GridPane					extendedSettingsGrid;

	@FXML// fx:id="gridWidthSlider"
	private Slider						gridWidthSlider;

	@FXML// fx:id="openDefaultdir"
	private Button						openDefaultdir;

	@FXML// fx:id="openEnddir"
	private Button						openEnddir;

	@FXML// fx:id="openFiles"
	private Button						openFiles;

	@FXML// fx:id="openThumbnail"
	private Button						openThumbnail;

	@FXML// fx:id="playlistDropScrollpane"
	private ScrollPane					playlistDropScrollpane;

	@FXML// fx:id="playlistGrid"
	private GridPane					playlistGrid;

	@FXML// fx:id="playlistSourceScrollpane"
	private ScrollPane					playlistSourceScrollpane;

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

	@FXML// fx:id="uploadFacebook"
	private CheckBox					uploadFacebook;

	@FXML// fx:id="uploadFile"
	private ChoiceBox<File>				uploadFile;

	@FXML// fx:id="uploadGrid"
	private GridPane					uploadGrid;

	@FXML// fx:id="uploadLicense"
	private ChoiceBox<String>			uploadLicense;

	@FXML// fx:id="uploadMessage"
	private TextArea					uploadMessage;

	@FXML// fx:id="uploadMobile"
	private CheckBox					uploadMobile;

	@FXML// fx:id="uploadRate"
	private CheckBox					uploadRate;

	@FXML// fx:id="uploadTags"
	private TextArea					uploadTags;

	@FXML// fx:id="uploadThumbnail"
	private TextField					uploadThumbnail;

	@FXML// fx:id="uploadTitle"
	private TextField					uploadTitle;

	@FXML// fx:id="uploadTwitter"
	private CheckBox					uploadTwitter;

	@FXML// fx:id="uploadVideoresponse"
	private ChoiceBox<String>			uploadVideoresponse;

	@FXML// fx:id="uploadVisibility"
	private ChoiceBox<String>			uploadVisibility;

	@FXML// fx:id="validationText"
	private Label						validationText;

	private final CalendarTextField		starttime			= new CalendarTextField().withValue(Calendar.getInstance())
																	.withDateFormat(new SimpleDateFormat("dd.MM.yyyy HH:mm"))
																	.withShowTime(Boolean.TRUE);
	private final CalendarTextField		releasetime			= new CalendarTextField().withValue(Calendar.getInstance())
																	.withDateFormat(new SimpleDateFormat("dd.MM.yyyy HH:mm"))
																	.withShowTime(Boolean.TRUE);
	private final ListSpinner<Integer>	number				= new ListSpinner<Integer>(-1000, 1000).withValue(0).withAlignment(
																	Pos.CENTER_RIGHT);

	private final GridView<Model>		playlistSourcezone	= GridViewBuilder.create(Model.class).build();
	private final GridView<Model>		playlistDropzone	= GridViewBuilder.create(Model.class).build();

	@Inject private CategoryService		categoryService;
	@Inject private FileChooser			fileChooser;
	@Inject private DirectoryChooser	directoryChooser;
	@Inject private DataSource			dataSource;
	@Inject private UploadViewModel		uploadViewModel;

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(final URL fxmlFileLocation, final ResourceBundle resources) {
		assert accountList != null : "fx:id=\"accountList\" was not injected: check your FXML file 'Upload.fxml'.";
		assert addUpload != null : "fx:id=\"addUpload\" was not injected: check your FXML file 'Upload.fxml'.";
		assert extendedSettingsGrid != null : "fx:id=\"extendedSettingsGrid\" was not injected: check your FXML file 'Upload.fxml'.";
		assert gridWidthSlider != null : "fx:id=\"gridWidthSlider\" was not injected: check your FXML file 'Upload.fxml'.";
		assert openDefaultdir != null : "fx:id=\"openDefaultdir\" was not injected: check your FXML file 'Upload.fxml'.";
		assert openEnddir != null : "fx:id=\"openEnddir\" was not injected: check your FXML file 'Upload.fxml'.";
		assert openFiles != null : "fx:id=\"openFiles\" was not injected: check your FXML file 'Upload.fxml'.";
		assert openThumbnail != null : "fx:id=\"openThumbnail\" was not injected: check your FXML file 'Upload.fxml'.";
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
		// initialize your logic here: all @FXML variables will have been
		// injected
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
		final EventHandler<DragEvent> onDragOver = new EventHandler<DragEvent>() {

			@Override
			public void handle(final DragEvent event) {
				if (event.getGestureSource() != event.getTarget() && event.getDragboard().hasString()) {
					event.acceptTransferModes(TransferMode.ANY);
				}
				event.consume();
			}
		};

		final EventHandler<DragEvent> onDragDropped = new EventHandler<DragEvent>() {

			@Override
			public void handle(final DragEvent event) {
				final Dragboard db = event.getDragboard();
				boolean success = false;
				if (db.hasString()) {
					if (((Node) event.getTarget()).getParent() == playlistDropzone && event.getGestureSource() != playlistDropzone) {
						uploadViewModel.movePlaylistToDropzone(Integer.parseInt(db.getString()));
						success = true;
					} else if (((Node) event.getTarget()).getParent() == playlistSourcezone
							&& event.getGestureSource() != playlistSourcezone) {
						uploadViewModel.removePlaylistFromDropzone(Integer.parseInt(db.getString()));
						success = true;
					}

				}
				event.setDropCompleted(success);
				event.consume();
			}
		};

		final EventHandler<DragEvent> onDragEntered = new EventHandler<DragEvent>() {

			@Override
			public void handle(final DragEvent event) {
				if ((event.getGestureSource() != event.getTarget() && event.getTarget() == playlistDropzone || event.getTarget() == playlistSourcezone)
						&& event.getDragboard().hasString()) {

					((Node) event.getTarget()).getParent().getParent().getParent().getStyleClass().clear();
					((Node) event.getTarget()).getParent().getParent().getParent().getStyleClass().add("dragentered");
				}

				event.consume();
			}
		};

		final EventHandler<DragEvent> onDragExited = new EventHandler<DragEvent>() {

			@Override
			public void handle(final DragEvent event) {
				((Node) event.getTarget()).getParent().getParent().getParent().getStyleClass().clear();
				((Node) event.getTarget()).getParent().getParent().getParent().getStyleClass().add("dropzone");
				event.consume();
			}
		};

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
		final Callback<GridView<Model>, GridCell<Model>> playlistSourceCellFactory = new Callback<GridView<Model>, GridCell<Model>>() {

			@Override
			public PlaylistGridCell call(final GridView<Model> arg0) {
				final PlaylistGridCell cell = new PlaylistGridCell();

				cell.setOnDragDetected(new EventHandler<Event>() {

					@Override
					public void handle(final Event event) {
						final Dragboard db = playlistSourcezone.startDragAndDrop(TransferMode.ANY);
						final ClipboardContent content = new ClipboardContent();
						content.putString(uploadViewModel.playlistSourceListProperty.indexOf(cell.itemProperty().get()) + "");
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
		};

		final Callback<GridView<Model>, GridCell<Model>> playlistDropCellFactory = new Callback<GridView<Model>, GridCell<Model>>() {

			@Override
			public PlaylistGridCell call(final GridView<Model> arg0) {
				final PlaylistGridCell cell = new PlaylistGridCell();

				cell.setOnDragDetected(new EventHandler<Event>() {

					@Override
					public void handle(final Event event) {
						final Dragboard db = playlistDropzone.startDragAndDrop(TransferMode.ANY);
						final ClipboardContent content = new ClipboardContent();
						content.putString(uploadViewModel.playlistDropListProperty.indexOf(cell.itemProperty().get()) + "");
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
		};
		playlistSourcezone.setCellFactory(playlistSourceCellFactory);
		playlistDropzone.setCellFactory(playlistDropCellFactory);

		uploadFile.converterProperty().set(new StringConverter<File>() {

			@Override
			public String toString(final File object) {

				if (object.getPath().length() > 50) {
					final String fileName = object.getPath();
					return fileName.substring(0, fileName.indexOf(File.separatorChar, fileName.indexOf(File.separatorChar)))
							.concat(File.separator).concat("...")
							.concat(fileName.substring(fileName.lastIndexOf(File.separatorChar, fileName.length())));
				}

				return object.getPath();
			}

			@Override
			public File fromString(final String string) {
				throw new RuntimeException("This method is not implemented: uploadFile is readonly!");
			}
		});

		uploadViewModel.idProperty.addListener(new InvalidationListener() {

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
		});
	}

	private void initSelection() {
		uploadVisibility.getSelectionModel().selectFirst();
		uploadComment.getSelectionModel().selectFirst();
		uploadLicense.getSelectionModel().selectFirst();
		uploadVideoresponse.getSelectionModel().selectFirst();
		accountList.getSelectionModel().selectFirst();
		ThreadUtil.doInBackground(new Runnable() {

			@Override
			public void run() {
				if (!Base.hasConnection()) {
					Base.open(dataSource);
				}
				uploadViewModel.categoryProperty.set(FXCollections.observableList(categoryService.load()));
				if (uploadViewModel.categoryProperty.isEmpty()) {
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
					return;
				}
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						uploadCategory.getSelectionModel().selectFirst();
						templateList.getSelectionModel().selectFirst();
					}
				});
			}
		});
	}

	private void initBindings() {

		releasetime.disableProperty().bind(uploadVisibility.getSelectionModel().selectedIndexProperty().lessThan(2));
		uploadMessage.disableProperty().bind(releasetime.disableProperty());
		uploadFacebook.disableProperty().bind(releasetime.disableProperty());
		uploadTwitter.disableProperty().bind(releasetime.disableProperty());

		gridWidthSlider.minProperty().set(1280);
		gridWidthSlider.maxProperty().set(2000);
		playlistSourcezone.cellWidthProperty().bind(gridWidthSlider.valueProperty().divide(9));
		playlistSourcezone.cellHeightProperty().bind(gridWidthSlider.valueProperty().divide(16));
		playlistDropzone.minWidthProperty().bind(playlistDropScrollpane.widthProperty().subtract(5));
		playlistDropzone.prefHeightProperty().bind(playlistDropScrollpane.heightProperty());
		playlistDropzone.cellHeightProperty().set(68);
		playlistDropzone.cellWidthProperty().set(120);

		playlistSourcezone.minHeightProperty().bind(playlistSourceScrollpane.heightProperty().subtract(5));
		playlistSourcezone.prefWidthProperty().bind(playlistSourceScrollpane.widthProperty().subtract(5));

		// VIEW MODEL BINDINGS

		uploadViewModel.init(uploadCategory.getSelectionModel(), uploadFile.getSelectionModel(), accountList.getSelectionModel(),
				uploadComment.getSelectionModel(), uploadLicense.getSelectionModel(), uploadVideoresponse.getSelectionModel(),
				uploadVisibility.getSelectionModel(), templateList.getSelectionModel());

		fileChooser.initialDirectoryProperty().bindBidirectional(uploadViewModel.initialDirectoryProperty);
		directoryChooser.initialDirectoryProperty().bindBidirectional(uploadViewModel.initialDirectoryProperty);
		templateList.itemsProperty().bindBidirectional(uploadViewModel.templateProperty);
		accountList.itemsProperty().bindBidirectional(uploadViewModel.accountProperty);
		playlistDropzone.itemsProperty().bindBidirectional(uploadViewModel.playlistDropListProperty);
		playlistSourcezone.itemsProperty().bindBidirectional(uploadViewModel.playlistSourceListProperty);
		previewTitle.textProperty().bindBidirectional(uploadViewModel.previewTitleProperty);
		uploadCategory.itemsProperty().bindBidirectional(uploadViewModel.categoryProperty);
		uploadComment.itemsProperty().bindBidirectional(uploadViewModel.commentProperty);
		uploadCommentvote.selectedProperty().bindBidirectional(uploadViewModel.commentVoteProperty);
		uploadDefaultdir.textProperty().bindBidirectional(uploadViewModel.defaultdirProperty);
		uploadDescription.textProperty().bindBidirectional(uploadViewModel.descriptionProperty);
		uploadEmbed.selectedProperty().bindBidirectional(uploadViewModel.embedProperty);
		uploadEnddir.textProperty().bindBidirectional(uploadViewModel.enddirProperty);
		uploadFile.itemsProperty().bindBidirectional(uploadViewModel.fileProperty);
		uploadLicense.itemsProperty().bindBidirectional(uploadViewModel.licenseProperty);
		uploadMobile.selectedProperty().bindBidirectional(uploadViewModel.mobileProperty);
		uploadRate.selectedProperty().bindBidirectional(uploadViewModel.rateProperty);
		uploadTags.textProperty().bindBidirectional(uploadViewModel.tagsProperty);
		uploadTitle.textProperty().bindBidirectional(uploadViewModel.titleProperty);
		uploadThumbnail.textProperty().bindBidirectional(uploadViewModel.thumbnailProperty);
		uploadVideoresponse.itemsProperty().bindBidirectional(uploadViewModel.videoresponseProperty);
		uploadVisibility.itemsProperty().bindBidirectional(uploadViewModel.visibilityProperty);
		starttime.valueProperty().bindBidirectional(uploadViewModel.starttimeProperty);
		releasetime.valueProperty().bindBidirectional(uploadViewModel.releasetimeProperty);
		uploadFacebook.selectedProperty().bindBidirectional(uploadViewModel.facebookProperty);
		uploadTwitter.selectedProperty().bindBidirectional(uploadViewModel.twitterProperty);
		uploadMessage.textProperty().bindBidirectional(uploadViewModel.messageProperty);
	}

	// Handler for Button[fx:id="addUpload"] onAction
	@FXML
	public void addUpload(final ActionEvent event) {
		final Upload upload = uploadViewModel.toUpload();

		if (upload.isValid()) {
			validationText.setId("validation_passed");
			validationText.setText(I18nHelper.message("validation.info.added"));
		} else {
			validationText.setId("validation_error");
			final StringBuilder stringBuilder = new StringBuilder("");
			for (final String error : upload.errors().values()) {
				stringBuilder.append(error);
				stringBuilder.append('\n');
			}
			validationText.setText(stringBuilder.toString());
		}

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
		uploadFile.getItems().clear();
		uploadFile.getItems().addAll(files);
		uploadFile.getSelectionModel().selectFirst();
		if (uploadTitle.getText() == null || uploadTitle.getText().isEmpty()) {
			final String file = files.get(0).getAbsolutePath();
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
}
