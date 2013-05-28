/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader.controller;

import com.cathive.fx.guice.GuiceFXMLLoader;
import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import jfxtras.labs.scene.control.CalendarTextField;
import jfxtras.labs.scene.control.grid.GridCell;
import jfxtras.labs.scene.control.grid.GridView;
import jfxtras.labs.scene.control.grid.GridViewBuilder;
import org.chaosfisch.util.ExtendedPlaceholders;
import org.chaosfisch.youtubeuploader.command.RefreshPlaylistsCommand;
import org.chaosfisch.youtubeuploader.command.RemoveTemplateCommand;
import org.chaosfisch.youtubeuploader.command.UpdateTemplateCommand;
import org.chaosfisch.youtubeuploader.command.UploadControllerAddCommand;
import org.chaosfisch.youtubeuploader.controller.renderer.AccountStringConverter;
import org.chaosfisch.youtubeuploader.controller.renderer.PlaylistGridCell;
import org.chaosfisch.youtubeuploader.db.dao.AccountDao;
import org.chaosfisch.youtubeuploader.db.dao.PlaylistDao;
import org.chaosfisch.youtubeuploader.db.dao.TemplateDao;
import org.chaosfisch.youtubeuploader.db.dao.UploadDao;
import org.chaosfisch.youtubeuploader.db.data.*;
import org.chaosfisch.youtubeuploader.db.events.ModelAddedEvent;
import org.chaosfisch.youtubeuploader.db.events.ModelRemovedEvent;
import org.chaosfisch.youtubeuploader.db.events.ModelUpdatedEvent;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Account;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;
import org.chaosfisch.youtubeuploader.db.validation.UploadValidationCode;
import org.chaosfisch.youtubeuploader.guice.ICommandProvider;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class UploadController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private ChoiceBox<Account> uploadAccount;

	@FXML
	private Button addUpload;

	@FXML
	private GridPane extendedSettingsGrid;

	@FXML
	private Slider gridWidthSlider;

	@FXML
	private GridPane monetizeGridPane;

	@FXML
	private ToggleButton monetizePartner;

	@FXML
	private Button openDefaultdir;

	@FXML
	private Button openEnddir;

	@FXML
	private Button openFiles;

	@FXML
	private Button openThumbnail;

	@FXML
	private TitledPane partnerPane;

	@FXML
	private ScrollPane playlistDropScrollpane;

	@FXML
	private GridPane playlistGrid;

	@FXML
	private ScrollPane playlistSourceScrollpane;

	@FXML
	private TextField previewTitle;

	@FXML
	private Button refreshPlaylists;

	@FXML
	private Button removeTemplate;

	@FXML
	private Button resetUpload;

	@FXML
	private Button saveTemplate;

	@FXML
	private ChoiceBox<Template> templates;

	@FXML
	private ChoiceBox<Category> uploadCategory;

	@FXML
	private ChoiceBox<Comment> uploadComment;

	@FXML
	private CheckBox uploadCommentvote;

	@FXML
	private TextField uploadDefaultdir;

	@FXML
	private TextArea uploadDescription;

	@FXML
	private CheckBox uploadEmbed;

	@FXML
	private TextField uploadEnddir;

	@FXML
	private CheckBox uploadFacebook;

	@FXML
	private ChoiceBox<File> uploadFile;

	@FXML
	private GridPane uploadGrid;

	@FXML
	private ChoiceBox<License> uploadLicense;

	@FXML
	private TextArea uploadMessage;

	@FXML
	private CheckBox uploadMobile;

	@FXML
	private CheckBox uploadRate;

	@FXML
	private TextArea uploadTags;

	@FXML
	private TextField uploadThumbnail;

	@FXML
	private TextField uploadTitle;

	@FXML
	private CheckBox uploadTwitter;

	@FXML
	private ChoiceBox<Videoresponse> uploadVideoresponse;

	@FXML
	private ChoiceBox<Visibility> uploadVisibility;

	@FXML
	private TitledPane x2;

	@FXML
	private TitledPane x5;

	private final CalendarTextField  started            = new CalendarTextField().withValue(Calendar.getInstance())
			.withDateFormat(new SimpleDateFormat("dd.MM.yyyy HH:mm"))
			.withShowTime(true);
	private final CalendarTextField  release            = new CalendarTextField().withValue(Calendar.getInstance())
			.withDateFormat(new SimpleDateFormat("dd.MM.yyyy HH:mm"))
			.withShowTime(true);
	private final GridView<Playlist> playlistSourcezone = GridViewBuilder.create(Playlist.class).build();
	private final GridView<Playlist> playlistTargetzone = GridViewBuilder.create(Playlist.class).build();

	@Inject
	private FileChooser      fileChooser;
	@Inject
	private DirectoryChooser directoryChooser;
	@Inject
	private ICommandProvider commandProvider;
	@Inject
	private AccountDao       accountDao;
	@Inject
	private PlaylistDao      playlistDao;
	@Inject
	private TemplateDao      templateDao;
	@Inject
	private UploadDao        uploadDao;
	@Inject
	private EventBus         eventBus;

	@Inject
	private GuiceFXMLLoader fxmlLoader;

	private final ObservableList<File>          filesList          = FXCollections.observableArrayList();
	private final ObservableList<Category>      categoriesList     = FXCollections.observableArrayList();
	private final ObservableList<Account>       accountsList       = FXCollections.observableArrayList();
	private final ObservableList<Template>      templatesList      = FXCollections.observableArrayList();
	private final ObservableList<Visibility>    visibilityList     = FXCollections.observableArrayList();
	private final ObservableList<Comment>       commentsList       = FXCollections.observableArrayList();
	private final ObservableList<License>       licensesList       = FXCollections.observableArrayList();
	private final ObservableList<Videoresponse> videoresponsesList = FXCollections.observableArrayList();
	private final ObservableList<Playlist>      playlistSourceList = FXCollections.observableArrayList();
	private final ObservableList<Playlist>      playlistTargetList = FXCollections.observableArrayList();
	private final SimpleIntegerProperty         idProperty         = new SimpleIntegerProperty();
	private final SimpleObjectProperty<File>    defaultDirProperty = new SimpleObjectProperty<>();
	private final SimpleObjectProperty<File>    enddirProperty     = new SimpleObjectProperty<>();
	private UploadMonetizationController uploadMonetizationController;
	private UploadPartnerController      uploadPartnerController;
	private Upload uploadStore = null;

	public UploadController() {
		idProperty.setValue(null);
	}

	@FXML
	void addUpload(final ActionEvent event) {
		final Upload upload = uploadStore == null ? new Upload() : uploadStore;
		toUpload(upload);

		if (uploadEnddir.getText() == null || uploadEnddir.getText().isEmpty()) {
			upload.setEnddir(null);
		}
		final UploadControllerAddCommand command = commandProvider.get(UploadControllerAddCommand.class);
		command.upload = upload;
		command.account = uploadAccount.getValue();
		command.playlists = new Playlist[playlistTargetList.size()];
		playlistTargetList.toArray(command.playlists);
		command.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(final WorkerStateEvent event) {
				// Cleanup (reset form)
				filesList.remove(uploadFile.getValue());
				uploadFile.getSelectionModel().selectNext();
			}
		});
		command.setOnRunning(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(final WorkerStateEvent workerStateEvent) {
				resetControlls();
			}
		});
		command.setOnFailed(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(final WorkerStateEvent event) {
				try {
					//noinspection ThrowableResultOfMethodCallIgnored
					final UploadValidationCode error = UploadValidationCode.valueOf(event.getSource()
							.getException()
							.getMessage());
					switch (error) {
						case ACCOUNT_NULL:
							uploadAccount.getStyleClass().add("input-invalid");
							uploadAccount.setTooltip(TooltipBuilder.create()
									.autoHide(true)
									.text(resources.getString("validation.account"))
									.build());
							uploadAccount.getTooltip()
									.show(uploadAccount, getTooltipX(uploadAccount), getTooltipY(uploadAccount));
							break;
						case CATEGORY_NULL:
							uploadCategory.getStyleClass().add("input-invalid");
							uploadCategory.setTooltip(TooltipBuilder.create()
									.autoHide(true)
									.text(resources.getString("validation.category"))
									.build());
							uploadCategory.getTooltip()
									.show(uploadCategory, getTooltipX(uploadCategory), getTooltipY(uploadCategory));
							break;
						case DESCRIPTION_ILLEGAL:
							uploadDescription.getStyleClass().add("input-invalid");
							uploadDescription.setTooltip(TooltipBuilder.create()
									.autoHide(true)
									.text(resources.getString("validation.description.characters"))
									.build());
							uploadDescription.getTooltip()
									.show(uploadDescription, getTooltipX(uploadDescription), getTooltipY(uploadDescription));
							break;
						case DESCRIPTION_LENGTH:
							uploadDescription.getStyleClass().add("input-invalid");
							uploadDescription.setTooltip(TooltipBuilder.create()
									.autoHide(true)
									.text(resources.getString("validation.description"))
									.build());
							uploadDescription.getTooltip()
									.show(uploadDescription, getTooltipX(uploadDescription), getTooltipY(uploadDescription));
							break;
						case FILE_NULL:
							uploadFile.getStyleClass().add("input-invalid");
							uploadFile.setTooltip(TooltipBuilder.create()
									.autoHide(true)
									.text(resources.getString("validation.filelist"))
									.build());
							uploadFile.getTooltip().show(uploadFile, getTooltipX(uploadFile), getTooltipY(uploadFile));
							break;
						case TAGS_ILLEGAL:
							uploadTags.getStyleClass().add("input-invalid");
							uploadTags.setTooltip(TooltipBuilder.create()
									.autoHide(true)
									.text(resources.getString("validation.tags"))
									.build());
							uploadTags.getTooltip().show(uploadTags, getTooltipX(uploadTags), getTooltipY(uploadTags));
							break;
						case THUMBNAIL_SIZE:
							uploadThumbnail.getStyleClass().add("input-invalid");
							uploadThumbnail.setTooltip(TooltipBuilder.create()
									.autoHide(true)
									.text(resources.getString("validation.thumbnail"))
									.build());
							uploadThumbnail.getTooltip()
									.show(uploadThumbnail, getTooltipX(uploadThumbnail), getTooltipY(uploadThumbnail));
							break;
						case TITLE_ILLEGAL:
						case TITLE_NULL:
							uploadTitle.getStyleClass().add("input-invalid");
							uploadTitle.setTooltip(TooltipBuilder.create()
									.autoHide(true)
									.text(resources.getString("validation.title"))
									.build());
							uploadTitle.getTooltip()
									.show(uploadTitle, getTooltipX(uploadTitle), getTooltipY(uploadTitle));
							break;
					}
				} catch (final Exception e) {
					//noinspection ThrowableResultOfMethodCallIgnored
					event.getSource().getException().printStackTrace();
				}
			}
		});
		command.start();
	}

	private void resetControlls() {
		final Control[] nodes = new Control[] {uploadAccount, uploadCategory, uploadDescription, uploadFile, uploadTags,
											   uploadThumbnail, uploadTitle};
		for (final Control node : nodes) {
			node.getStyleClass().remove("input-invalid");
			node.setTooltip(null);
		}
	}

	private double getTooltipY(final Node node) {
		final Point2D p = node.localToScene(0.0, 0.0);
		return p.getY() + node.getScene().getY() + node.getScene().getWindow().getY() + node.getLayoutBounds()
				.getHeight() - 5;
	}

	private double getTooltipX(final Node node) {
		final Point2D p = node.localToScene(0.0, 0.0);
		return p.getX() + node.getScene().getX() + node.getScene().getWindow().getX() - 5;
	}

	@FXML
	void openDefaultdir(final ActionEvent event) {
		final File directory = directoryChooser.showDialog(null);
		if (directory != null) {
			directoryChooser.setInitialDirectory(directory);
			fileChooser.setInitialDirectory(directory);
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
			addUploadFiles(files);
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
		command.accounts = new Account[accountsList.size()];
		accountsList.toArray(command.accounts);
		command.start();
	}

	@FXML
	void removeTemplate(final ActionEvent event) {
		final Template template;
		if ((template = templates.getSelectionModel().getSelectedItem()) != null) {
			final RemoveTemplateCommand command = commandProvider.get(RemoveTemplateCommand.class);
			command.template = template;
			command.start();
		}
	}

	@FXML
	void resetUpload(final ActionEvent event) {
		_reset();
	}

	private void _reset() {
		fromTemplate(templates.getValue() == null ? ViewController.standardTemplate : templates.getValue());
	}

	@FXML
	void saveTemplate(final ActionEvent event) {
		if (templates.getValue() == null) {
			return;
		}
		final UpdateTemplateCommand command = commandProvider.get(UpdateTemplateCommand.class);
		command.template = toTemplate(templates.getValue());
		command.account = uploadAccount.getValue();
		command.playlists = new Playlist[playlistTargetList.size()];
		playlistTargetList.toArray(command.playlists);
		command.start();
	}

	@FXML
	void togglePartner(final ActionEvent event) {
		if (monetizePartner.isSelected()) {
			monetizeGridPane.getChildren().retainAll(monetizePartner);
			monetizeGridPane.add(uploadPartnerController.getNode(), 0, 0, GridPane.REMAINING, 1);
		} else {
			monetizeGridPane.getChildren().retainAll(monetizePartner);
			monetizeGridPane.add(uploadMonetizationController.getNode(), 0, 0, GridPane.REMAINING, 1);
		}
	}

	@FXML
	void initialize() {
		assert addUpload != null : "fx:id=\"addUpload\" was not injected: check your FXML file 'Upload.fxml'.";
		assert extendedSettingsGrid != null : "fx:id=\"extendedSettingsGrid\" was not injected: check your FXML file 'Upload.fxml'.";
		assert gridWidthSlider != null : "fx:id=\"gridWidthSlider\" was not injected: check your FXML file 'Upload.fxml'.";
		assert monetizePartner != null : "fx:id=\"monetizePartner\" was not injected: check your FXML file 'Upload.fxml'.";
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
		assert templates != null : "fx:id=\"templates\" was not injected: check your FXML file 'Upload.fxml'.";
		assert uploadAccount != null : "fx:id=\"uploadAccount\" was not injected: check your FXML file 'Upload.fxml'.";
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
		initData();
		initSelection();

		eventBus.register(this);

		refreshPlaylists(null);
	}

	@Subscribe
	public void onModelAdded(final ModelAddedEvent modelAddedEvent) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				if (modelAddedEvent.getModel() instanceof Account) {
					accountsList.add((Account) modelAddedEvent.getModel());
					if (uploadAccount.getValue() == null && accountsList.size() > 0) {
						uploadAccount.getSelectionModel().selectFirst();
					}
					refreshPlaylists(null);
				} else if (modelAddedEvent.getModel() instanceof Template) {

					templatesList.add((Template) modelAddedEvent.getModel());
					if (templates.getValue() == null && templatesList.size() > 0) {
						templates.getSelectionModel().selectFirst();
					}

				} else if (modelAddedEvent.getModel() instanceof Playlist && playlistDao.fetchOneAccountByPlaylist((Playlist) modelAddedEvent
						.getModel()).equals(uploadAccount.getValue())) {
					playlistSourceList.add((Playlist) modelAddedEvent.getModel());
				}
			}
		});
	}

	@Subscribe
	public void onModelUpdated(final ModelUpdatedEvent modelUpdatedEvent) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (modelUpdatedEvent.getModel() instanceof Account) {
					accountsList.set(accountsList.indexOf(modelUpdatedEvent.getModel()), (Account) modelUpdatedEvent.getModel());
				} else if (modelUpdatedEvent.getModel() instanceof Template) {
					templatesList.set(templatesList.indexOf(modelUpdatedEvent.getModel()), (Template) modelUpdatedEvent.getModel());
					templates.getSelectionModel().select((Template) modelUpdatedEvent.getModel());
				} else if (modelUpdatedEvent.getModel() instanceof Playlist && playlistDao.fetchOneAccountByPlaylist((Playlist) modelUpdatedEvent
						.getModel()).equals(uploadAccount.getValue())) {
					if (((Playlist) modelUpdatedEvent.getModel()).getHidden()) {
						playlistSourceList.remove(modelUpdatedEvent.getModel());
						playlistTargetList.remove(modelUpdatedEvent.getModel());
					} else if (playlistSourceList.contains(modelUpdatedEvent.getModel())) {
						playlistSourceList.set(playlistSourceList.indexOf(modelUpdatedEvent.getModel()), (Playlist) modelUpdatedEvent
								.getModel());
					} else if (playlistTargetList.contains(modelUpdatedEvent.getModel())) {
						playlistTargetList.set(playlistTargetList.indexOf(modelUpdatedEvent.getModel()), (Playlist) modelUpdatedEvent
								.getModel());
					} else {
						playlistSourceList.add((Playlist) modelUpdatedEvent.getModel());
					}
				}
			}
		});
	}

	@Subscribe
	public void onModelRemoved(final ModelRemovedEvent modelPostRemovedEvent) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (modelPostRemovedEvent.getModel() instanceof Account) {
					accountsList.remove(modelPostRemovedEvent.getModel());
					if (uploadAccount.getValue() == null && accountsList.size() > 0) {
						uploadAccount.getSelectionModel().selectFirst();
					}
				} else if (modelPostRemovedEvent.getModel() instanceof Template) {
					templatesList.remove(modelPostRemovedEvent.getModel());
					if (templates.getValue() == null && templatesList.size() > 0) {
						templates.getSelectionModel().selectFirst();
					}
				} else if (modelPostRemovedEvent.getModel() instanceof Playlist) {
					playlistSourceList.remove(modelPostRemovedEvent.getModel());
					playlistTargetList.remove(modelPostRemovedEvent.getModel());
				}
			}
		});

	}

	private void initData() {
		visibilityList.addAll(Visibility.values());
		videoresponsesList.addAll(Videoresponse.values());
		commentsList.addAll(Comment.values());
		licensesList.addAll(License.values());
		categoriesList.addAll(Category.values());

		accountsList.addAll(accountDao.findAll());
		templatesList.addAll(templateDao.findAll());
	}

	private void initControls() {
		extendedSettingsGrid.add(started, 1, 11, GridPane.REMAINING, 1);
		extendedSettingsGrid.add(release, 1, 12, GridPane.REMAINING, 1);
		playlistSourceScrollpane.setContent(playlistSourcezone);
		playlistDropScrollpane.setContent(playlistTargetzone);
	}

	private void initCustomFactories() {
		playlistSourcezone.setCellFactory(new PlaylistSourceCellFactory());
		playlistTargetzone.setCellFactory(new PlaylistTargetCellFactory());
		uploadAccount.setConverter(new AccountStringConverter());
		uploadFile.setConverter(new UploadFileListViewConverter());
		idProperty.addListener(new UploadIdInvalidationListener());
		try {
			uploadPartnerController = fxmlLoader.load(getClass().getResource("/org/chaosfisch/youtubeuploader/view/UploadPartner.fxml"), resources)
					.getController();
			uploadMonetizationController = fxmlLoader.load(getClass().getResource("/org/chaosfisch/youtubeuploader/view/UploadMonetization.fxml"), resources)
					.getController();
		} catch (IOException e) {
			e.printStackTrace();
		}

		monetizeGridPane.add(uploadMonetizationController.getNode(), 0, 0, GridPane.REMAINING, 1);

		monetizePartner.selectedProperty().addListener(new MonetizePartnerInvalidationListener());
		uploadLicense.getSelectionModel().selectedItemProperty().addListener(new LicenseChangeListener());
		uploadAccount.getSelectionModel().selectedItemProperty().addListener(new AccountChangeListener());

		templates.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Template>() {

			@Override
			public void changed(final ObservableValue<? extends Template> observable, final Template oldValue, final Template newValue) {
				_reset();
			}
		});

		final PreviewTitleChangeListener previewTitleChangeListener = new PreviewTitleChangeListener();
		uploadFile.getSelectionModel().selectedItemProperty().addListener(previewTitleChangeListener);
		playlistTargetList.addListener(previewTitleChangeListener);

		uploadFile.setItems(filesList);
		uploadCategory.setItems(categoriesList);
		uploadAccount.setItems(accountsList);
		templates.setItems(templatesList);
		uploadVisibility.setItems(visibilityList);
		uploadComment.setItems(commentsList);
		uploadLicense.setItems(licensesList);
		uploadVideoresponse.setItems(videoresponsesList);
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
		release.disableProperty().bind(uploadVisibility.getSelectionModel().selectedIndexProperty().isNotEqualTo(3));
		uploadMessage.disableProperty()
				.bind(uploadVisibility.getSelectionModel()
						.selectedIndexProperty()
						.isNotEqualTo(3)
						.and(uploadVisibility.getSelectionModel().selectedIndexProperty().isNotEqualTo(0)));
		uploadFacebook.disableProperty()
				.bind(uploadVisibility.getSelectionModel()
						.selectedIndexProperty()
						.isNotEqualTo(3)
						.and(uploadVisibility.getSelectionModel().selectedIndexProperty().isNotEqualTo(0)));
		uploadTwitter.disableProperty()
				.bind(uploadVisibility.getSelectionModel()
						.selectedIndexProperty()
						.isNotEqualTo(3)
						.and(uploadVisibility.getSelectionModel().selectedIndexProperty().isNotEqualTo(0)));
		gridWidthSlider.minProperty().set(1280);
		gridWidthSlider.maxProperty().set(2000);
		playlistSourcezone.cellWidthProperty().bind(gridWidthSlider.valueProperty().divide(9));
		playlistSourcezone.cellHeightProperty().bind(gridWidthSlider.valueProperty().divide(16));
		playlistTargetzone.minWidthProperty().bind(playlistDropScrollpane.widthProperty().subtract(5));
		playlistTargetzone.prefHeightProperty().bind(playlistDropScrollpane.heightProperty());
		playlistTargetzone.cellHeightProperty().set(68);
		playlistTargetzone.cellWidthProperty().set(120);
		playlistSourcezone.minHeightProperty().bind(playlistSourceScrollpane.heightProperty().subtract(5));
		playlistSourcezone.prefWidthProperty().bind(playlistSourceScrollpane.widthProperty().subtract(5));

		previewTitle.textProperty().bindBidirectional(uploadTitle.textProperty(), new PreviewTitleStringConverter());

		uploadDefaultdir.textProperty().bindBidirectional(defaultDirProperty, new DefaultDirStringConverter());
		uploadEnddir.textProperty().bindBidirectional(enddirProperty, new DefaultDirStringConverter());
	}

	private Upload toUpload(final Upload upload) {
		upload.setId(idProperty.getValue());
		upload.setCategory(uploadCategory.getValue());
		upload.setCommentvote(uploadCommentvote.isSelected());
		upload.setComment(uploadComment.getValue());

		if (started.getValue() != null) {
			final GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis(started.getValue().getTimeInMillis());
			upload.setDateOfStart(cal);
		}
		if (release.getValue() != null) {
			final GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis(release.getValue().getTimeInMillis());
			upload.setDateOfRelease(cal);
		}
		upload.setDescription(uploadDescription.getText());
		upload.setEmbed(uploadEmbed.isSelected());
		upload.setEnddir(enddirProperty.getValue());
		upload.setFile(uploadFile.getValue());
		upload.setKeywords(uploadTags.getText());
		upload.setLicense(uploadLicense.getValue());

		upload.setMobile(uploadMobile.isSelected());

		upload.setRate(uploadRate.isSelected());

		upload.setThumbnail(uploadThumbnail.getText());
		upload.setTitle(uploadTitle.getText());

		upload.setVisibility(uploadVisibility.getValue());
		upload.setVideoresponse(uploadVideoresponse.getValue());

		upload.setFacebook(uploadFacebook.isSelected());
		upload.setTwitter(uploadTwitter.isSelected());
		upload.setMessage(uploadMessage.getText());
		upload.setMonetizePartner(monetizePartner.isSelected());

		if (monetizePartner.isSelected()) {
			uploadPartnerController.toUpload(upload);
		} else {
			uploadMonetizationController.toUpload(upload);
		}

		return upload;
	}

	private Template toTemplate(final Template template) {
		template.setCategory(uploadCategory.getValue());
		template.setCommentvote(uploadCommentvote.isSelected());
		template.setComment(uploadComment.getValue());
		template.setDescription(uploadDescription.getText());
		template.setDefaultdir(Strings.isNullOrEmpty(uploadDefaultdir.getText())
							   ? null
							   : defaultDirProperty.getValue());
		template.setEmbed(uploadEmbed.isSelected());

		template.setEnddir(Strings.isNullOrEmpty(uploadEnddir.getText()) ? null : enddirProperty.getValue());
		template.setKeywords(uploadTags.getText());
		template.setLicense(uploadLicense.getValue());

		template.setMobile(uploadMobile.isSelected());

		template.setRate(uploadRate.isSelected());

		template.setThumbnail(uploadThumbnail.getText());
		template.setTitle(uploadTitle.getText());

		template.setVisibility(uploadVisibility.getValue());
		template.setVideoresponse(uploadVideoresponse.getValue());

		template.setFacebook(uploadFacebook.isSelected());
		template.setTwitter(uploadTwitter.isSelected());
		template.setMessage(uploadMessage.getText());
		template.setMonetizePartner(monetizePartner.isSelected());

		if (monetizePartner.isSelected()) {
			uploadPartnerController.toTemplate(template);
		} else {
			uploadMonetizationController.toTemplate(template);
		}

		return template;
	}

	public void fromUpload(final Upload upload) {
		_reset();

		uploadStore = upload;
		idProperty.setValue(upload.getId());
		uploadCategory.setValue(upload.getCategory());
		uploadCommentvote.setSelected(upload.getCommentvote());
		uploadComment.setValue(upload.getComment());

		started.setValue(upload.getDateOfStart());
		release.setValue(upload.getDateOfRelease());

		uploadDescription.setText(upload.getDescription());
		uploadEmbed.setSelected(upload.getEmbed());
		enddirProperty.setValue(upload.getEnddir());
		uploadFile.setValue(upload.getFile());
		uploadTags.setText(upload.getKeywords());
		uploadLicense.setValue(upload.getLicense());
		uploadMobile.setSelected(upload.getMobile());
		uploadRate.setSelected(upload.getRate());

		uploadThumbnail.setText(upload.getThumbnail());
		uploadTitle.setText(upload.getTitle());
		uploadVisibility.setValue(upload.getVisibility());
		uploadVideoresponse.setValue(upload.getVideoresponse());
		uploadFacebook.setSelected(upload.getFacebook());
		uploadTwitter.setSelected(upload.getTwitter());
		uploadMessage.setText(upload.getMessage());

		monetizePartner.setSelected(upload.getMonetizePartner());

		if (monetizePartner.isSelected()) {
			uploadPartnerController.fromUpload(upload);
		} else {
			uploadMonetizationController.fromUpload(upload);
		}

		uploadAccount.getSelectionModel().select(accountDao.findById(upload.getAccountId()));

		if (uploadAccount.getValue() == null) {
			uploadAccount.getSelectionModel().selectFirst();
		}
		if (uploadCategory.getValue() == null) {
			uploadCategory.getSelectionModel().selectFirst();
		}

		final Iterator<Playlist> playlistIterator = playlistTargetList.iterator();
		while (playlistIterator.hasNext()) {
			final Playlist playlist = playlistIterator.next();
			playlistSourceList.add(playlist);
			playlistIterator.remove();
		}

		for (final Playlist playlist : playlistDao.fetchByUpload(upload)) {
			playlistTargetList.add(playlist);
			playlistSourceList.remove(playlist);
		}
	}

	private void fromTemplate(final Template template) {
		resetControlls();
		if (template.getDefaultdir() != null && template.getDefaultdir().isDirectory()) {
			fileChooser.setInitialDirectory(template.getDefaultdir());
			directoryChooser.setInitialDirectory(template.getDefaultdir());
		}

		idProperty.setValue(null);
		uploadStore = null;
		uploadCategory.setValue(template.getCategory());
		uploadCommentvote.setSelected(template.getCommentvote());
		uploadComment.setValue(template.getComment());
		uploadDescription.setText(template.getDescription());
		uploadEmbed.setSelected(template.getEmbed());
		enddirProperty.setValue(template.getEnddir());
		uploadTags.setText(template.getKeywords());
		uploadLicense.setValue(template.getLicense());
		uploadMobile.setSelected(template.getMobile());
		uploadRate.setSelected(template.getRate());
		uploadThumbnail.setText(template.getThumbnail());
		uploadTitle.setText(template.getTitle());
		uploadVisibility.setValue(template.getVisibility());
		uploadVideoresponse.setValue(template.getVideoresponse());
		uploadFacebook.setSelected(template.getFacebook());
		uploadTwitter.setSelected(template.getTwitter());
		uploadMessage.setText(template.getMessage());
		defaultDirProperty.setValue(template.getDefaultdir());
		enddirProperty.setValue(template.getEnddir());

		monetizePartner.setSelected(template.getMonetizePartner());

		if (monetizePartner.isSelected()) {
			uploadPartnerController.fromTemplate(template);
		} else {
			uploadMonetizationController.fromTemplate(template);
		}

		uploadAccount.getSelectionModel().select(accountDao.findById(template.getAccountId()));
		if (uploadAccount.getValue() == null) {
			uploadAccount.getSelectionModel().selectFirst();
		}
		if (uploadCategory.getValue() == null) {
			uploadCategory.getSelectionModel().selectFirst();
		}

		final Iterator<Playlist> playlistIterator = playlistTargetList.iterator();
		while (playlistIterator.hasNext()) {
			final Playlist playlist = playlistIterator.next();
			playlistSourceList.add(playlist);
			playlistIterator.remove();
		}

		if (templates.getValue() != null) {
			for (final Playlist playlist : playlistDao.fetchByTemplate(templates.getValue())) {
				playlistTargetList.add(playlist);
				playlistSourceList.remove(playlist);
			}
		}

		playlistSourcezone.setItems(null);
		playlistSourcezone.setItems(playlistSourceList);

		playlistTargetzone.setItems(null);
		playlistTargetzone.setItems(playlistTargetList);
	}

	private void initSelection() {

		final ChoiceBox<?>[] controls = new ChoiceBox[] {uploadVisibility, uploadComment, uploadLicense,
														 uploadVideoresponse, uploadAccount, uploadCategory, templates};

		for (final ChoiceBox<?> comboBox : controls) {
			comboBox.getSelectionModel().selectFirst();
		}
	}

	void movePlaylistToDropzone(final int model) {
		movePlaylist(model, playlistSourceList, playlistTargetList);
	}

	void removePlaylistFromDropzone(final int model) {
		movePlaylist(model, playlistTargetList, playlistSourceList);
	}

	private void movePlaylist(final int model, final List<Playlist> from, final List<Playlist> to) {
		if (model >= 0) {
			to.add(from.get(model));
			from.remove(model);
		}
	}

	public void addUploadFiles(final List<File> files) {
		filesList.clear();
		filesList.addAll(files);
		uploadFile.getSelectionModel().selectFirst();
		if (uploadTitle.getText() == null || uploadTitle.getText().isEmpty()) {
			final String file = files.get(0).getAbsolutePath();
			int index = file.lastIndexOf('.');
			if (index == -1) {
				index = file.length();
			}
			uploadTitle.setText(file.substring(file.lastIndexOf(File.separator) + 1, index));
		}
	}

	private final class AccountChangeListener implements ChangeListener<Account> {
		@Override
		public void changed(final ObservableValue<? extends Account> observable, final Account oldValue, final Account newValue) {
			playlistSourceList.clear();
			playlistTargetList.clear();
			if (newValue != null) {
				playlistSourceList.addAll(playlistDao.fetchUnhidden(newValue.getId()));
			}
		}
	}

	private final static class DefaultDirStringConverter extends StringConverter<File> {

		@Override
		public String toString(final File file) {
			return file == null ? null : file.getAbsolutePath();
		}

		@Override
		public File fromString(final String path) {
			return path == null ? null : new File(path);
		}
	}

	private final class PreviewTitleStringConverter extends DefaultStringConverter {
		final ExtendedPlaceholders extendedPlaceholders = new ExtendedPlaceholders();

		@Override
		public String toString(final String value) {
			extendedPlaceholders.setFile(uploadFile.getValue() == null ? null : uploadFile.getValue());
			extendedPlaceholders.setPlaylists(playlistTargetList);
			return extendedPlaceholders.replace(value);
		}
	}

	private final class PreviewTitleChangeListener implements ListChangeListener<Playlist>, ChangeListener<File> {

		@Override
		public void onChanged(final Change<? extends Playlist> change) {
			_change();
		}

		@Override
		public void changed(final ObservableValue<? extends File> observableValue, final File file, final File file2) {
			_change();
		}

		private void _change() {
			final String value = uploadTitle.getText();
			uploadTitle.setText("");
			uploadTitle.setText(value);
		}
	}

	private final class LicenseChangeListener implements ChangeListener<License> {
		@Override
		public void changed(final ObservableValue<? extends License> observable, final License oldValue, final License newValue) {
			if (newValue == null) {
				return;
			}
			switch (newValue) {
				case CREATIVE_COMMONS:
					partnerPane.setDisable(true);
					partnerPane.getStyleClass().add("partnerPaneDisabled");
					break;
				case YOUTUBE:
					partnerPane.setDisable(false);
					partnerPane.getStyleClass().remove("partnerPaneDisabled");
					break;
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
			if (idProperty.getValue() == null || idProperty.getValue() == 0) {
				addUpload.setText(resources.getString("button.addUpload"));
				addUpload.setId("addUpload");
			} else {
				addUpload.setText(resources.getString("button.saveUpload"));
				addUpload.setId("saveUpload");
			}
		}
	}

	private final static class UploadFileListViewConverter extends StringConverter<File> {
		@Override
		public String toString(final File object) {
			if (object.getPath().length() > 50) {
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

	private final class PlaylistTargetCellFactory implements Callback<GridView<Playlist>, GridCell<Playlist>> {
		@Override
		public PlaylistGridCell call(final GridView<Playlist> arg0) {
			final PlaylistGridCell cell = new PlaylistGridCell();
			cell.setOnDragDetected(new PlaylistDragDetected(playlistTargetzone, playlistTargetList, cell));
			cell.setOnMouseClicked(new PlaylistMouseClicked(playlistTargetList, cell));
			return cell;
		}
	}

	private final class PlaylistSourceCellFactory implements Callback<GridView<Playlist>, GridCell<Playlist>> {
		@Override
		public PlaylistGridCell call(final GridView<Playlist> arg0) {
			final PlaylistGridCell cell = new PlaylistGridCell();
			cell.setOnDragDetected(new PlaylistDragDetected(playlistSourcezone, playlistSourceList, cell));
			cell.setOnMouseClicked(new PlaylistMouseClicked(playlistSourceList, cell));
			return cell;
		}
	}

	private static final class PlaylistDragDetected implements EventHandler<Event> {
		private final GridView<Playlist>       gridView;
		private final ObservableList<Playlist> playlists;
		private final PlaylistGridCell         cell;

		public PlaylistDragDetected(final GridView<Playlist> gridView, final ObservableList<Playlist> playlists, final PlaylistGridCell cell) {
			this.gridView = gridView;
			this.playlists = playlists;
			this.cell = cell;
		}

		@Override
		public void handle(final Event event) {
			final Dragboard db = gridView.startDragAndDrop(TransferMode.ANY);
			final ClipboardContent content = new ClipboardContent();
			content.putString(String.valueOf(playlists.indexOf(cell.itemProperty().get())));
			db.setContent(content);
			event.consume();
		}
	}

	private final class PlaylistMouseClicked implements EventHandler<MouseEvent> {
		private final ObservableList<Playlist> playlists;
		private final PlaylistGridCell         cell;

		public PlaylistMouseClicked(final ObservableList<Playlist> playlists, final PlaylistGridCell cell) {
			this.playlists = playlists;
			this.cell = cell;
		}

		@Override
		public void handle(final MouseEvent event) {
			if (event.getClickCount() == 2) {

				if (playlists.equals(playlistTargetList)) {
					removePlaylistFromDropzone(playlists.indexOf(cell.itemProperty().get()));
				} else if (playlists.equals(playlistSourceList)) {
					movePlaylistToDropzone(playlists.indexOf(cell.itemProperty().get()));
				}
			}
		}
	}

	private final static class DragExitedCallback implements EventHandler<DragEvent> {
		@Override
		public void handle(final DragEvent event) {
			((Node) event.getTarget()).getParent().getParent().getParent().getStyleClass().clear();
			((Node) event.getTarget()).getParent().getParent().getParent().getStyleClass().add("dropzone");
			event.consume();
		}
	}

	private final class DragEnteredCallback implements EventHandler<DragEvent> {
		@Override
		public void handle(final DragEvent event) {
			if ((event.getGestureSource() != event.getTarget() && event.getTarget() == playlistTargetzone || event.getTarget() == playlistSourcezone) && event
					.getDragboard()
					.hasString()) {
				((Node) event.getTarget()).getParent().getParent().getParent().getStyleClass().clear();
				((Node) event.getTarget()).getParent().getParent().getParent().getStyleClass().add("dragentered");
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

	private final static class DragOverCallback implements EventHandler<DragEvent> {
		@Override
		public void handle(final DragEvent event) {
			if (event.getGestureSource() != event.getTarget() && event.getDragboard().hasString()) {
				event.acceptTransferModes(TransferMode.ANY);
			}
			event.consume();
		}
	}
}