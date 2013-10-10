/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.controller;

import com.cathive.fx.guice.FXMLController;
import com.cathive.fx.guice.FxApplicationThread;
import com.cathive.fx.guice.GuiceFXMLLoader;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Files;
import com.google.inject.Inject;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.account.IAccountService;
import de.chaosfisch.google.account.events.AccountAdded;
import de.chaosfisch.google.account.events.AccountRemoved;
import de.chaosfisch.google.account.events.AccountUpdated;
import de.chaosfisch.google.youtube.playlist.IPlaylistService;
import de.chaosfisch.google.youtube.playlist.Playlist;
import de.chaosfisch.google.youtube.upload.IUploadService;
import de.chaosfisch.google.youtube.upload.Status;
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.google.youtube.upload.metadata.*;
import de.chaosfisch.google.youtube.upload.metadata.permissions.Comment;
import de.chaosfisch.google.youtube.upload.metadata.permissions.Permissions;
import de.chaosfisch.google.youtube.upload.metadata.permissions.Visibility;
import de.chaosfisch.services.ExtendedPlaceholders;
import de.chaosfisch.uploader.renderer.AccountStringConverter;
import de.chaosfisch.uploader.renderer.DialogHelper;
import de.chaosfisch.uploader.renderer.PlaylistGridCell;
import de.chaosfisch.uploader.renderer.TagTextArea;
import de.chaosfisch.uploader.template.ITemplateService;
import de.chaosfisch.uploader.template.Template;
import de.chaosfisch.uploader.template.events.TemplateAdded;
import de.chaosfisch.uploader.template.events.TemplateRemoved;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import jfxtras.labs.scene.control.CalendarTextField;
import jfxtras.labs.scene.control.grid.GridCell;
import jfxtras.labs.scene.control.grid.GridView;
import jfxtras.labs.scene.control.grid.GridViewBuilder;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

@FXMLController
public class UploadController {

	@FXML
	public Label descriptionLabel;

	@FXML
	public Label tagLabel;

	@FXML
	public Label titleLabel;

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
	private CheckBox uploadRate;

	@FXML
	public ScrollPane uploadTagsScrollpane;

	@FXML
	private TextField uploadThumbnail;

	@FXML
	private TextField uploadTitle;

	@FXML
	private CheckBox uploadTwitter;

	@FXML
	private ChoiceBox<Visibility> uploadVisibility;

	@FXML
	private TitledPane x2;

	@FXML
	private TitledPane x5;

	private final CalendarTextField  started            = new CalendarTextField().withValue(Calendar.getInstance())
			.withDateFormat(new SimpleDateFormat("dd.MM.yyyy HH:mm"));
	private final CalendarTextField  release            = new CalendarTextField().withValue(Calendar.getInstance())
			.withDateFormat(new SimpleDateFormat("dd.MM.yyyy HH:mm"));
	private final GridView<Playlist> playlistSourcezone = GridViewBuilder.create(Playlist.class).build();
	private final GridView<Playlist> playlistTargetzone = GridViewBuilder.create(Playlist.class).build();

	@Inject
	private FileChooser      fileChooser;
	@Inject
	private DirectoryChooser directoryChooser;
	@Inject
	private IAccountService  accountService;
	@Inject
	private IPlaylistService playlistService;
	@Inject
	private ITemplateService templateService;
	@Inject
	private IUploadService   uploadService;
	@Inject
	private DialogHelper     dialogHelper;
	@Inject
	private TagTextArea      uploadTags;
	@Inject
	private GuiceFXMLLoader  fxmlLoader;

	private final ObservableList<File>       filesList          = FXCollections.observableArrayList();
	private final ObservableList<Category>   categoriesList     = FXCollections.observableArrayList();
	private final ObservableList<Account>    accountsList       = FXCollections.observableArrayList();
	private final ObservableList<Template>   templatesList      = FXCollections.observableArrayList();
	private final ObservableList<Visibility> visibilityList     = FXCollections.observableArrayList();
	private final ObservableList<Comment>    commentsList       = FXCollections.observableArrayList();
	private final ObservableList<License>    licensesList       = FXCollections.observableArrayList();
	private final ObservableList<Playlist>   playlistSourceList = FXCollections.observableArrayList();
	private final ObservableList<Playlist>   playlistTargetList = FXCollections.observableArrayList();
	private final SimpleStringProperty       idProperty         = new SimpleStringProperty();
	private final SimpleObjectProperty<File> defaultDirProperty = new SimpleObjectProperty<>();
	private final SimpleObjectProperty<File> enddirProperty     = new SimpleObjectProperty<>();
	private UploadMonetizationController uploadMonetizationController;
	private UploadPartnerController      uploadPartnerController;
	private Upload                       uploadStore;

	public UploadController() {
		idProperty.setValue(null);
	}

	@FXML
	void addUpload(final ActionEvent event) {

		try {
			dialogHelper.resetControlls(new Control[] {uploadAccount, uploadCategory, uploadDescription, uploadFile,
													   uploadThumbnail, uploadTitle});
			uploadTags.getStyleClass().remove("input-invalid");
			buildUpload();
			// Cleanup (reset form)
			filesList.remove(uploadFile.getValue());
			uploadFile.getSelectionModel().selectNext();
			idProperty.setValue(null);
		} catch (IllegalArgumentException e) {
			handleUploadBuildException(e);
		}
	}

	private void handleUploadBuildException(final IllegalArgumentException e) {
		switch (e.getMessage()) {
			case Upload.Validation.ACCOUNT:
				uploadAccount.getStyleClass().add("input-invalid");
				uploadAccount.setTooltip(TooltipBuilder.create()
						.autoHide(true)
						.text(resources.getString("validation.account"))
						.build());
				uploadAccount.getTooltip()
						.show(uploadAccount, dialogHelper.getTooltipX(uploadAccount), dialogHelper.getTooltipY(uploadAccount));
				break;
			case Upload.Validation.CATEGORY:
				uploadCategory.getStyleClass().add("input-invalid");
				uploadCategory.setTooltip(TooltipBuilder.create()
						.autoHide(true)
						.text(resources.getString("validation.category"))
						.build());
				uploadCategory.getTooltip()
						.show(uploadCategory, dialogHelper.getTooltipX(uploadCategory), dialogHelper.getTooltipY(uploadCategory));
				break;
			case Upload.Validation.DESCRIPTION_CHARACTERS:
				uploadDescription.getStyleClass().add("input-invalid");
				uploadDescription.setTooltip(TooltipBuilder.create()
						.autoHide(true)
						.text(resources.getString("validation.description.characters"))
						.build());
				uploadDescription.getTooltip()
						.show(uploadDescription, dialogHelper.getTooltipX(uploadDescription), dialogHelper.getTooltipY(uploadDescription));
				break;
			case Upload.Validation.DESCRIPTION_SIZE:
				uploadDescription.getStyleClass().add("input-invalid");
				uploadDescription.setTooltip(TooltipBuilder.create()
						.autoHide(true)
						.text(resources.getString("validation.description"))
						.build());
				uploadDescription.getTooltip()
						.show(uploadDescription, dialogHelper.getTooltipX(uploadDescription), dialogHelper.getTooltipY(uploadDescription));
				break;
			case Upload.Validation.FILE:
				uploadFile.getStyleClass().add("input-invalid");
				uploadFile.setTooltip(TooltipBuilder.create()
						.autoHide(true)
						.text(resources.getString("validation.filelist"))
						.build());
				uploadFile.getTooltip()
						.show(uploadFile, dialogHelper.getTooltipX(uploadFile), dialogHelper.getTooltipY(uploadFile));
				break;
			case Upload.Validation.KEYWORD:
				uploadTags.getStyleClass().add("input-invalid");
				final Tooltip tooltip = TooltipBuilder.create()
						.autoHide(true)
						.text(resources.getString("validation.tags"))
						.build();
				tooltip.show(uploadTags, dialogHelper.getTooltipX(uploadTags), dialogHelper.getTooltipY(uploadTags));

				break;
			case Upload.Validation.THUMBNAIL:
			case Upload.Validation.THUMBNAIL_SIZE:
				uploadThumbnail.getStyleClass().add("input-invalid");
				uploadThumbnail.setTooltip(TooltipBuilder.create()
						.autoHide(true)
						.text(resources.getString("validation.thumbnail"))
						.build());
				uploadThumbnail.getTooltip()
						.show(uploadThumbnail, dialogHelper.getTooltipX(uploadThumbnail), dialogHelper.getTooltipY(uploadThumbnail));
				break;
			case Upload.Validation.TITLE:
			case Upload.Validation.TITLE_SIZE:
			case Upload.Validation.TITLE_CHARACTERS:
				uploadTitle.getStyleClass().add("input-invalid");
				uploadTitle.setTooltip(TooltipBuilder.create()
						.autoHide(true)
						.text(resources.getString("validation.title"))
						.build());
				uploadTitle.getTooltip()
						.show(uploadTitle, dialogHelper.getTooltipX(uploadTitle), dialogHelper.getTooltipY(uploadTitle));
				break;
			case Upload.Validation.ENDDIR:
				uploadEnddir.getStyleClass().add("input-invalid");
				uploadEnddir.setTooltip(TooltipBuilder.create()
						.autoHide(true)
						.text(resources.getString("validation.enddir"))
						.build());
				uploadEnddir.getTooltip()
						.show(uploadEnddir, dialogHelper.getTooltipX(uploadEnddir), dialogHelper.getTooltipY(uploadEnddir));
				break;
		}
	}

	private void buildUpload() {
		final Upload upload = null == uploadStore ?
							  new Upload(uploadAccount.getValue(), uploadFile.getValue()) :
							  uploadStore;
		toUpload(upload);

		final Monetization monetization = upload.getMonetization();
		if (!monetization.isPartner() && (monetization.isOverlay() || monetization.isTrueview() || monetization.isProduct())) {
			monetization.setClaim(true);
		}

		if (null == upload.getId()) {
			final Status status = new Status();
			status.setArchived(false);
			status.setFailed(false);
			status.setRunning(false);
			status.setLocked(false);
			upload.setStatus(status);
			uploadService.insert(upload);
		} else {
			final Status status = upload.getStatus();
			status.setArchived(false);
			status.setFailed(false);
			status.setLocked(false);

			upload.setStatus(status);
			uploadService.update(upload);
		}
	}

	@FXML
	void openDefaultdir(final ActionEvent event) {
		final File directory = directoryChooser.showDialog(null);
		if (null != directory) {
			directoryChooser.setInitialDirectory(directory);
			fileChooser.setInitialDirectory(directory);
			uploadDefaultdir.setText(directory.getAbsolutePath());
		}
	}

	@FXML
	void openEnddir(final ActionEvent event) {
		final File directory = directoryChooser.showDialog(null);
		if (null != directory) {
			uploadEnddir.setText(directory.getAbsolutePath());
		}
	}

	@FXML
	void openFiles(final ActionEvent event) {
		final List<File> files = fileChooser.showOpenMultipleDialog(null);
		if (null != files && !files.isEmpty()) {
			addUploadFiles(files);
		}
	}

	@FXML
	void openThumbnail(final ActionEvent event) {
		final File file = fileChooser.showOpenDialog(null);
		if (null != file) {
			uploadThumbnail.setText(file.getAbsolutePath());
		}
	}

	@FXML
	void refreshPlaylists(final ActionEvent event) {

		final Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				updateMessage("Loading playlists");
				updateProgress(-1, -1);
				playlistService.synchronizePlaylists(accountsList);
				return null;
			}

			@Override
			protected void failed() {
				updateMessage("Failed loading playlists");
				updateProgress(0, 0);
			}

			@Override
			protected void succeeded() {
				updateMessage("Playlists loaded.");
				updateProgress(1, 1);
			}
		};
		dialogHelper.registerBusyTask(task);
		final Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();
	}

	@FXML
	void removeTemplate(final ActionEvent event) {
		final Template template;
		if (null != (template = templates.getSelectionModel().getSelectedItem())) {
			templateService.delete(template);
		}
	}

	@FXML
	void resetUpload(final ActionEvent event) {
		_reset();
	}

	private void _reset() {
		fromTemplate(null == templates.getValue() ? ViewController.standardTemplate : templates.getValue());
	}

	@FXML
	void saveTemplate(final ActionEvent event) {
		if (null == templates.getValue()) {
			return;
		}
		try {
			final Template template = toTemplate(templates.getValue());
			template.setAccount(uploadAccount.getValue());
			template.setPlaylists(playlistTargetList);
			templateService.update(template);
		} catch (IllegalArgumentException e) {
			handleUploadBuildException(e);
		}
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
		assert null != addUpload : "fx:id=\"addUpload\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != extendedSettingsGrid : "fx:id=\"extendedSettingsGrid\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != gridWidthSlider : "fx:id=\"gridWidthSlider\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != monetizePartner : "fx:id=\"monetizePartner\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != openDefaultdir : "fx:id=\"openDefaultdir\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != openEnddir : "fx:id=\"openEnddir\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != openFiles : "fx:id=\"openFiles\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != openThumbnail : "fx:id=\"openThumbnail\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != partnerPane : "fx:id=\"partnerPane\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != playlistDropScrollpane : "fx:id=\"playlistDropScrollpane\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != playlistGrid : "fx:id=\"playlistGrid\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != playlistSourceScrollpane : "fx:id=\"playlistSourceScrollpane\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != previewTitle : "fx:id=\"previewTitle\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != refreshPlaylists : "fx:id=\"refreshPlaylists\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != removeTemplate : "fx:id=\"removeTemplate\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != resetUpload : "fx:id=\"resetUpload\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != saveTemplate : "fx:id=\"saveTemplate\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != templates : "fx:id=\"templates\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != uploadAccount : "fx:id=\"uploadAccount\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != uploadCategory : "fx:id=\"uploadCategory\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != uploadComment : "fx:id=\"uploadComment\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != uploadCommentvote : "fx:id=\"uploadCommentvote\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != uploadDefaultdir : "fx:id=\"uploadDefaultdir\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != uploadDescription : "fx:id=\"uploadDescription\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != uploadEmbed : "fx:id=\"uploadEmbed\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != uploadEnddir : "fx:id=\"uploadEnddir\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != uploadFacebook : "fx:id=\"uploadFacebook\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != uploadFile : "fx:id=\"uploadFile\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != uploadGrid : "fx:id=\"uploadGrid\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != uploadLicense : "fx:id=\"uploadLicense\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != uploadMessage : "fx:id=\"uploadMessage\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != uploadRate : "fx:id=\"uploadRate\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != uploadTags : "fx:id=\"uploadTags\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != uploadThumbnail : "fx:id=\"uploadThumbnail\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != uploadTitle : "fx:id=\"uploadTitle\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != uploadTwitter : "fx:id=\"uploadTwitter\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != uploadVisibility : "fx:id=\"uploadVisibility\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != x2 : "fx:id=\"x2\" was not injected: check your FXML file 'Upload.fxml'.";
		assert null != x5 : "fx:id=\"x5\" was not injected: check your FXML file 'Upload.fxml'.";
		initControls();
		initCustomFactories();
		initDragEventHandlers();
		initBindings();
		initData();
		initSelection();
	}

	@Subscribe
	public void onTemplateAdded(final TemplateAdded event) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				templatesList.add(event.getTemplate());
				if (null == templates.getValue()) {
					templates.getSelectionModel().selectFirst();
				}
			}
		});
	}

	@Subscribe
	public void onTemplateRemoved(final TemplateRemoved event) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				templatesList.remove(event.getTemplate());
				if (event.getTemplate().equals(templates.getValue())) {
					templates.setValue(null);
					templates.getSelectionModel().selectFirst();
				}
			}
		});
	}

	@Subscribe
	public void onAccountAdded(final AccountAdded event) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				accountsList.add(event.getAccount());
				if (null == uploadAccount.getValue()) {
					uploadAccount.getSelectionModel().selectFirst();
				}
				refreshPlaylists(null);
			}
		});
	}

	@Subscribe
	public void onAccoutUpdated(final AccountUpdated event) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (accountsList.contains(event.getAccount())) {
					accountsList.set(accountsList.indexOf(event.getAccount()), event.getAccount());
				} else {
					accountsList.add(event.getAccount());
				}

				if (null == uploadAccount.getValue()) {
					uploadAccount.getSelectionModel().select(event.getAccount());
				}
			}
		});
	}

	@Subscribe
	public void onAccountRemoved(final AccountRemoved event) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				accountsList.remove(event.getAccount());
				if (event.getAccount().equals(uploadAccount.getValue())) {
					playlistSourceList.clear();
					playlistTargetList.clear();
					uploadAccount.setValue(null);
					uploadAccount.getSelectionModel().selectFirst();
				}
			}
		});
	}

	@Subscribe
	public void onAccountUpdated(final AccountUpdated event) {
		if (!event.getAccount().equals(uploadAccount.getValue())) {
			return;
		}
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				playlistSourceList.retainAll(event.getAccount().getPlaylists());
				playlistTargetList.retainAll(event.getAccount().getPlaylists());

				for (final Playlist playlist : event.getAccount().getPlaylists()) {
					if (playlist.isHidden()) {
						playlistSourceList.remove(playlist);
						playlistTargetList.remove(playlist);
					} else if (!playlistSourceList.contains(playlist) && !playlistTargetList.contains(playlist)) {
						playlistSourceList.add(playlist);
					}
				}
			}
		});
	}

	private void initData() {
		visibilityList.addAll(Visibility.values());
		commentsList.addAll(Comment.values());
		licensesList.addAll(License.values());
		categoriesList.addAll(Category.values());

		accountsList.addAll(accountService.getAll());
		templatesList.addAll(templateService.getAll());
	}

	private void initControls() {
		extendedSettingsGrid.add(started, 1, 10, GridPane.REMAINING, 1);
		extendedSettingsGrid.add(release, 1, 11, GridPane.REMAINING, 1);
		uploadTagsScrollpane.setContent(uploadTags);
		playlistSourceScrollpane.setContent(playlistSourcezone);
		playlistDropScrollpane.setContent(playlistTargetzone);

		final String formatTitle = resources.getString("label.title") + "\n%d/100";
		previewTitle.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(final ObservableValue<? extends String> observableValue, final String oldTitle, final String newTitle) {
				final int length = newTitle.getBytes(Charsets.UTF_8).length;
				if (Upload.Validation.MAX_TITLE_SIZE < length) {
					titleLabel.setTextFill(Color.RED);
				} else {
					titleLabel.setTextFill(Color.BLACK);
				}
				titleLabel.setText(String.format(formatTitle, length));
			}
		});

		final String formatDescription = resources.getString("label.description") + "\n%d/5000";
		uploadDescription.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(final ObservableValue<? extends String> observableValue, final String oldDescription, final String newDescription) {
				final int length = newDescription.getBytes(Charsets.UTF_8).length;
				if (Upload.Validation.MAX_DESCRIPTION_SIZE < length) {
					descriptionLabel.setTextFill(Color.RED);
				} else {
					descriptionLabel.setTextFill(Color.BLACK);
				}
				descriptionLabel.setText(String.format(formatDescription, length));
			}
		});

		final String formatTags = resources.getString("label.tags") + "\n%d/500";
		uploadTags.tagsProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(final ObservableValue<? extends String> observableValue, final String oldTags, final String newTags) {
				final List<String> tags = TagParser.parse(newTags);
				if (!TagParser.areTagsValid(tags)) {
					tagLabel.setTextFill(Color.RED);
				} else {
					tagLabel.setTextFill(Color.BLACK);
				}
				tagLabel.setText(String.format(formatTags, TagParser.getLength(tags)));
			}
		});
	}

	private void initCustomFactories() {
		playlistSourcezone.setCellFactory(new PlaylistCellFactory(playlistSourceList));
		playlistTargetzone.setCellFactory(new PlaylistCellFactory(playlistTargetList));
		uploadAccount.setConverter(new AccountStringConverter());
		uploadFile.setConverter(new UploadFileListViewConverter());
		idProperty.addListener(new UploadIdInvalidationListener());
		try {
			uploadPartnerController = fxmlLoader.load(getClass().getResource("/de/chaosfisch/uploader/view/UploadPartner.fxml"), resources)
					.getController();
			uploadMonetizationController = fxmlLoader.load(getClass().getResource("/de/chaosfisch/uploader/view/UploadMonetization.fxml"), resources)
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

		final PreviewTitleChangeListener titleChangeListener = new PreviewTitleChangeListener();
		uploadFile.getSelectionModel().selectedItemProperty().addListener(titleChangeListener);
		playlistTargetList.addListener(titleChangeListener);

		uploadFile.setItems(filesList);
		uploadCategory.setItems(categoriesList);
		uploadAccount.setItems(accountsList);
		templates.setItems(templatesList);
		uploadVisibility.setItems(visibilityList);
		uploadComment.setItems(commentsList);
		uploadLicense.setItems(licensesList);
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

	private void initTwitterFacebookBinding(final BooleanProperty booleanProperty) {
		booleanProperty.bind(uploadVisibility.getSelectionModel()
				.selectedIndexProperty()
				.isNotEqualTo(3)
				.and(uploadVisibility.getSelectionModel().selectedIndexProperty().isNotEqualTo(0)));
	}

	private void initBindings() {
		release.disableProperty().bind(uploadVisibility.getSelectionModel().selectedIndexProperty().isNotEqualTo(3));
		uploadMessage.disableProperty()
				.bind(uploadVisibility.getSelectionModel()
						.selectedIndexProperty()
						.isNotEqualTo(3)
						.and(uploadVisibility.getSelectionModel().selectedIndexProperty().isNotEqualTo(0)));
		initTwitterFacebookBinding(uploadFacebook.disableProperty());
		initTwitterFacebookBinding(uploadTwitter.disableProperty());
		gridWidthSlider.minProperty().set(1280);
		gridWidthSlider.maxProperty().set(2000);

		playlistSourcezone.minHeightProperty().bind(playlistSourceScrollpane.heightProperty());
		playlistSourcezone.prefWidthProperty().bind(playlistSourceScrollpane.widthProperty().subtract(10));
		playlistSourcezone.cellWidthProperty().bind(gridWidthSlider.valueProperty().divide(9));
		playlistSourcezone.cellHeightProperty().bind(gridWidthSlider.valueProperty().divide(16));
		playlistTargetzone.minWidthProperty().bind(playlistDropScrollpane.widthProperty().subtract(5));
		playlistTargetzone.prefHeightProperty().bind(playlistDropScrollpane.heightProperty());
		playlistTargetzone.cellHeightProperty().set(68);
		playlistTargetzone.cellWidthProperty().set(120);

		previewTitle.textProperty().bindBidirectional(uploadTitle.textProperty(), new PreviewTitleStringConverter());

		uploadDefaultdir.textProperty().bindBidirectional(defaultDirProperty, new DefaultDirStringConverter());
		uploadEnddir.textProperty().bindBidirectional(enddirProperty, new DefaultDirStringConverter());
	}

	private Upload toUpload(final Upload upload) {
		upload.setId(idProperty.getValue());
		upload.setEnddir(Strings.isNullOrEmpty(uploadEnddir.getText()) ? null : enddirProperty.getValue());
		upload.setFile(uploadFile.getValue());
		upload.setThumbnail(Strings.isNullOrEmpty(uploadThumbnail.getText()) ?
							null :
							new File(uploadThumbnail.getText()));

		if (null != started.getValue()) {
			upload.setDateTimeOfStart(new DateTime(started.getValue()));
		}
		if (null != release.getValue()) {
			upload.setDateTimeOfRelease(new DateTime(release.getValue()));
		}

		final Metadata metadata = new Metadata(uploadTitle.getText(), uploadCategory.getValue(), uploadDescription.getText(), uploadTags
				.getTags(), uploadLicense.getValue());

		final Permissions permissions = null == upload.getPermissions() ? new Permissions() : upload.getPermissions();
		permissions.setCommentvote(uploadCommentvote.isSelected());
		permissions.setComment(uploadComment.getValue());
		permissions.setEmbed(uploadEmbed.isSelected());
		permissions.setRate(uploadRate.isSelected());
		permissions.setVisibility(uploadVisibility.getValue());

		final Social social = null == upload.getSocial() ? new Social() : upload.getSocial();
		social.setFacebook(uploadFacebook.isSelected());
		social.setTwitter(uploadTwitter.isSelected());
		social.setMessage(uploadMessage.getText());

		final Monetization monetization = null == upload.getMonetization() ?
										  new Monetization() :
										  upload.getMonetization();
		monetization.setPartner(monetizePartner.isSelected());

		upload.setMetadata(metadata);
		upload.setPermissions(permissions);
		upload.setSocial(social);
		upload.setMonetization(monetization);
		upload.setPlaylists(playlistTargetList);

		if (monetizePartner.isSelected()) {
			uploadPartnerController.toUpload(upload);
		} else {
			uploadMonetizationController.toUpload(upload);
		}

		return upload;
	}

	private Template toTemplate(final Template template) {

		template.setDefaultdir(Strings.isNullOrEmpty(uploadDefaultdir.getText()) ?
							   null :
							   defaultDirProperty.getValue());

		template.setEnddir(Strings.isNullOrEmpty(uploadEnddir.getText()) ? null : enddirProperty.getValue());
		template.setThumbnail(Strings.isNullOrEmpty(uploadThumbnail.getText()) ?
							  null :
							  new File(uploadThumbnail.getText()));

		final Metadata metadata = new Metadata(uploadTitle.getText(), uploadCategory.getValue(), uploadDescription.getText(), uploadTags
				.getTags(), uploadLicense.getValue());

		final Permissions permissions = null == template.getPermissions() ?
										new Permissions() :
										template.getPermissions();
		permissions.setCommentvote(uploadCommentvote.isSelected());
		permissions.setComment(uploadComment.getValue());
		permissions.setEmbed(uploadEmbed.isSelected());
		permissions.setRate(uploadRate.isSelected());
		permissions.setVisibility(uploadVisibility.getValue());

		final Social social = null == template.getSocial() ? new Social() : template.getSocial();
		social.setFacebook(uploadFacebook.isSelected());
		social.setTwitter(uploadTwitter.isSelected());
		social.setMessage(uploadMessage.getText());

		final Monetization monetization = null == template.getMonetization() ?
										  new Monetization() :
										  template.getMonetization();
		monetization.setPartner(monetizePartner.isSelected());

		template.setMetadata(metadata);
		template.setPermissions(permissions);
		template.setSocial(social);
		template.setMonetization(monetization);

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
		started.setValue(null == upload.getDateTimeOfStart() ?
						 Calendar.getInstance() :
						 upload.getDateTimeOfStart().toCalendar(Locale.getDefault()));
		release.setValue(null == upload.getDateTimeOfRelease() ?
						 Calendar.getInstance() :
						 upload.getDateTimeOfRelease().toCalendar(Locale.getDefault()));
		enddirProperty.setValue(upload.getEnddir());
		uploadFile.getItems().add(upload.getFile());
		uploadFile.getSelectionModel().select(upload.getFile());
		uploadThumbnail.setText(null == upload.getThumbnail() ? "" : upload.getThumbnail().getAbsolutePath());

		final Metadata metadata = null == upload.getMetadata() ? new Metadata() : upload.getMetadata();
		uploadCategory.setValue(metadata.getCategory());
		uploadDescription.setText(metadata.getDescription());
		uploadTags.setTags(metadata.getKeywords());
		uploadLicense.setValue(metadata.getLicense());
		uploadTitle.setText(metadata.getTitle());

		final Permissions permissions = null == upload.getPermissions() ? new Permissions() : upload.getPermissions();
		uploadCommentvote.setSelected(permissions.isCommentvote());
		uploadComment.setValue(permissions.getComment());
		uploadEmbed.setSelected(permissions.isEmbed());
		uploadRate.setSelected(permissions.isRate());
		uploadVisibility.setValue(permissions.getVisibility());

		final Social social = null == upload.getSocial() ? new Social() : upload.getSocial();
		uploadFacebook.setSelected(social.isFacebook());
		uploadTwitter.setSelected(social.isTwitter());
		uploadMessage.setText(social.getMessage());

		monetizePartner.setSelected(null != upload.getMonetization() && upload.getMonetization().isPartner());

		if (monetizePartner.isSelected()) {
			uploadPartnerController.fromUpload(upload);
		} else {
			uploadMonetizationController.fromUpload(upload);
		}

		uploadAccount.getSelectionModel().select(upload.getAccount());

		if (null == uploadAccount.getValue()) {
			uploadAccount.getSelectionModel().selectFirst();
		}
		if (null == uploadCategory.getValue()) {
			uploadCategory.getSelectionModel().selectFirst();
		}

		final Iterator<Playlist> playlistIterator = playlistTargetList.iterator();
		while (playlistIterator.hasNext()) {
			final Playlist playlist = playlistIterator.next();
			playlistSourceList.add(playlist);
			playlistIterator.remove();
		}

		for (final Playlist playlist : upload.getPlaylists()) {
			playlistTargetList.add(playlist);
			playlistSourceList.remove(playlist);
		}
	}

	private void fromTemplate(final Template template) {
		if (null != template.getDefaultdir() && template.getDefaultdir().isDirectory()) {
			fileChooser.setInitialDirectory(template.getDefaultdir());
			directoryChooser.setInitialDirectory(template.getDefaultdir());
		}

		idProperty.setValue(null);
		uploadStore = null;
		enddirProperty.setValue(template.getEnddir());
		uploadThumbnail.setText(null == template.getThumbnail() ? "" : template.getThumbnail().getAbsolutePath());
		defaultDirProperty.setValue(template.getDefaultdir());
		enddirProperty.setValue(template.getEnddir());

		final Metadata metadata = null == template.getMetadata() ? new Metadata() : template.getMetadata();
		uploadCategory.setValue(metadata.getCategory());
		uploadDescription.setText(metadata.getDescription());
		uploadTags.setTags(metadata.getKeywords());
		uploadLicense.setValue(metadata.getLicense());
		uploadTitle.setText(metadata.getTitle());

		final Permissions permissions = null == template.getPermissions() ?
										new Permissions() :
										template.getPermissions();
		uploadCommentvote.setSelected(permissions.isCommentvote());
		uploadComment.setValue(permissions.getComment());
		uploadEmbed.setSelected(permissions.isEmbed());
		uploadRate.setSelected(permissions.isRate());
		uploadVisibility.setValue(permissions.getVisibility());

		final Social social = null == template.getSocial() ? new Social() : template.getSocial();
		uploadFacebook.setSelected(social.isFacebook());
		uploadTwitter.setSelected(social.isTwitter());
		uploadMessage.setText(social.getMessage());

		monetizePartner.setSelected(null != template.getMonetization() && template.getMonetization().isPartner());

		if (monetizePartner.isSelected()) {
			uploadPartnerController.fromTemplate(template);
		} else {
			uploadMonetizationController.fromTemplate(template);
		}

		uploadAccount.getSelectionModel().select(template.getAccount());
		if (null == uploadAccount.getValue()) {
			uploadAccount.getSelectionModel().selectFirst();
		}
		if (null == uploadCategory.getValue()) {
			uploadCategory.getSelectionModel().selectFirst();
		}

		final Iterator<Playlist> playlistIterator = playlistTargetList.iterator();
		while (playlistIterator.hasNext()) {
			final Playlist playlist = playlistIterator.next();
			playlistSourceList.add(playlist);
			playlistIterator.remove();
		}

		if (null != templates.getValue() && null != templates.getValue().getPlaylists()) {
			for (final Playlist playlist : templates.getValue().getPlaylists()) {
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

		final ChoiceBox<?>[] controls = new ChoiceBox[] {uploadVisibility, uploadComment, uploadLicense, uploadAccount,
														 uploadCategory, templates};

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
		if (0 <= model) {
			to.add(from.get(model));
			from.remove(model);
		}
	}

	public void addUploadFiles(final List<File> files) {
		filesList.clear();
		filesList.addAll(files);
		uploadFile.getSelectionModel().selectFirst();
		if (null == uploadTitle.getText() || uploadTitle.getText().isEmpty()) {
			uploadTitle.setText(Files.getNameWithoutExtension(files.get(0).getName()));
		}
	}

	@FxApplicationThread
	private void _triggerPlaylist() {
		playlistTargetzone.setItems(null);
		playlistTargetzone.setItems(playlistTargetList);

		playlistSourcezone.setItems(null);
		playlistSourcezone.setItems(playlistSourceList);
	}

	private final class AccountChangeListener implements ChangeListener<Account> {
		@Override
		public void changed(final ObservableValue<? extends Account> observable, final Account oldValue, final Account newValue) {
			playlistSourceList.clear();
			playlistTargetList.clear();
			if (null != newValue) {
				playlistSourceList.addAll(playlistService.fetchUnhiddenByAccount(newValue));
			}
			_triggerPlaylist();
		}
	}

	private static final class DefaultDirStringConverter extends StringConverter<File> {

		@Override
		public String toString(final File file) {
			return null == file ? null : file.getAbsolutePath();
		}

		@Override
		public File fromString(final String path) {
			return null == path ? null : new File(path);
		}
	}

	private final class PreviewTitleStringConverter extends DefaultStringConverter {
		final ExtendedPlaceholders extendedPlaceholders = new ExtendedPlaceholders(resources);

		@Override
		public String toString(final String value) {
			extendedPlaceholders.setFile(null == uploadFile.getValue() ? null : uploadFile.getValue());
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
			if (null == newValue) {
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
			if (null == idProperty.getValue()) {
				addUpload.setText(resources.getString("button.addUpload"));
				addUpload.setId("addUpload");
			} else {
				addUpload.setText(resources.getString("button.saveUpload"));
				addUpload.setId("saveUpload");
			}
		}
	}

	private static final class UploadFileListViewConverter extends StringConverter<File> {
		@Override
		public String toString(final File object) {
			if (50 < object.getPath().length()) {
				final String fileName = object.getPath();
				return String.format("%s%s...%s", fileName.substring(0, fileName.indexOf(File.separatorChar, fileName.indexOf(File.separatorChar))), File.separator, fileName
						.substring(fileName.lastIndexOf(File.separatorChar, fileName.length())));
			}
			return object.getPath();
		}

		@Override
		public File fromString(final String string) {
			throw new RuntimeException("This method is not implemented: uploadFile is readonly!");
		}
	}

	private final class PlaylistCellFactory implements Callback<GridView<Playlist>, GridCell<Playlist>> {

		private final ObservableList<Playlist> list;

		public PlaylistCellFactory(final ObservableList<Playlist> list) {
			this.list = list;
		}

		@Override
		public GridCell<Playlist> call(final GridView<Playlist> playlistGridView) {
			final PlaylistGridCell cell = new PlaylistGridCell();
			cell.setOnDragDetected(new PlaylistDragDetected(playlistGridView, list, cell));
			cell.setOnMouseClicked(new PlaylistMouseClicked(list, cell));
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
			if (2 == event.getClickCount()) {

				if (playlists.equals(playlistTargetList)) {
					removePlaylistFromDropzone(playlists.indexOf(cell.itemProperty().get()));
				} else if (playlists.equals(playlistSourceList)) {
					movePlaylistToDropzone(playlists.indexOf(cell.itemProperty().get()));
				}
			}
		}
	}

	private static final class DragExitedCallback implements EventHandler<DragEvent> {
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

	private static final class DragOverCallback implements EventHandler<DragEvent> {
		@Override
		public void handle(final DragEvent event) {
			if (event.getGestureSource() != event.getTarget() && event.getDragboard().hasString()) {
				event.acceptTransferModes(TransferMode.ANY);
			}
			event.consume();
		}
	}
}