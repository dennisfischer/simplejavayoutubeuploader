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
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import jfxtras.labs.scene.control.BeanPathAdapter;
import jfxtras.labs.scene.control.CalendarTextField;
import jfxtras.labs.scene.control.ListSpinner;
import jfxtras.labs.scene.control.grid.GridCell;
import jfxtras.labs.scene.control.grid.GridView;
import jfxtras.labs.scene.control.grid.GridViewBuilder;
import org.chaosfisch.util.ExtendedPlaceholders;
import org.chaosfisch.youtubeuploader.ApplicationData;
import org.chaosfisch.youtubeuploader.command.RefreshPlaylistsCommand;
import org.chaosfisch.youtubeuploader.command.RemoveTemplateCommand;
import org.chaosfisch.youtubeuploader.command.UpdateTemplateCommand;
import org.chaosfisch.youtubeuploader.command.UploadControllerAddCommand;
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
import org.chaosfisch.youtubeuploader.vo.UploadViewVO;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

public class UploadController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ComboBox<Account> uploadAccount;

    @FXML
    private Button addUpload;

    @FXML
    private GridPane extendedSettingsGrid;

    @FXML
    private Slider gridWidthSlider;

    @FXML
    private Label labelContentInformation;

    @FXML
    private Label labelCustomId;

    @FXML
    private Label labelDescription;

    @FXML
    private Label labelEIDR;

    @FXML
    private Label labelISAN;

    @FXML
    private Label labelMonetizeTitle;

    @FXML
    private Label labelNote;

    @FXML
    private Label labelNumberEpisode;

    @FXML
    private Label labelNumberSeason;

    @FXML
    private Label labelTMSID;

    @FXML
    private Label labelTitleEpisode;

    @FXML
    private ComboBox<Asset> monetizeAsset;

    @FXML
    private CheckBox monetizeClaim;

    @FXML
    private ComboBox<ClaimOption> monetizeClaimOption;

    @FXML
    private ComboBox<ClaimType> monetizeClaimType;

    @FXML
    private TextField monetizeCustomID;

    @FXML
    private TextField monetizeDescription;

    @FXML
    private TextField monetizeEIDR;

    @FXML
    private TextField monetizeISAN;

    @FXML
    private CheckBox monetizeInStream;

    @FXML
    private CheckBox monetizeInStreamDefaults;

    @FXML
    private TextField monetizeNotes;

    @FXML
    private TextField monetizeNumberEpisode;

    @FXML
    private TextField monetizeNumberSeason;

    @FXML
    private CheckBox monetizeOverlay;

    @FXML
    private ToggleButton monetizePartner;

    @FXML
    private CheckBox monetizeProductPlacement;

    @FXML
    private ComboBox<Syndication> monetizeSyndication;

    @FXML
    private TextField monetizeTMSID;

    @FXML
    private TextField monetizeTitle;

    @FXML
    private TextField monetizeTitleEpisode;

    @FXML
    private CheckBox monetizeTrueView;

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
    private ComboBox<Template> templates;

    @FXML
    private ComboBox<Category> uploadCategory;

    @FXML
    private ComboBox<Comment> uploadComment;

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
    private ComboBox<File> uploadFile;

    @FXML
    private GridPane uploadGrid;

    @FXML
    private ComboBox<License> uploadLicense;

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
    private ComboBox<Videoresponse> uploadVideoresponse;

    @FXML
    private ComboBox<Visibility> uploadVisibility;

    @FXML
    private TitledPane x2;

    @FXML
    private TitledPane x5;

    private final CalendarTextField started = new CalendarTextField().withValue(Calendar.getInstance())
            .withDateFormat(new SimpleDateFormat("dd.MM.yyyy HH:mm"))
            .withShowTime(true);
    private final CalendarTextField release = new CalendarTextField().withValue(Calendar.getInstance())
            .withDateFormat(new SimpleDateFormat("dd.MM.yyyy HH:mm"))
            .withShowTime(true);
    private final ListSpinner<Integer> number = new ListSpinner<Integer>(-1000,
            1000).withValue(0)
            .withAlignment(Pos.CENTER_RIGHT);
    private final GridView<Playlist> playlistSourcezone = GridViewBuilder.create(Playlist.class)
            .build();
    private final GridView<Playlist> playlistTargetzone = GridViewBuilder.create(Playlist.class)
            .build();

    @Inject
    private FileChooser fileChooser;
    @Inject
    private DirectoryChooser directoryChooser;
    @Inject
    private ICommandProvider commandProvider;
    @Inject
    private AccountDao accountDao;
    @Inject
    private PlaylistDao playlistDao;
    @Inject
    private TemplateDao templateDao;
    @Inject
    private UploadDao uploadDao;
    @Inject
    private EventBus eventBus;

    private final ObservableList<File> filesList = FXCollections.observableArrayList();
    private final ObservableList<Category> categoriesList = FXCollections.observableArrayList();
    private final ObservableList<Account> accountsList = FXCollections.observableArrayList();
    private final ObservableList<Template> templatesList = FXCollections.observableArrayList();
    private final ObservableList<Visibility> visibilityList = FXCollections.observableArrayList();
    private final ObservableList<Comment> commentsList = FXCollections.observableArrayList();
    private final ObservableList<License> licensesList = FXCollections.observableArrayList();
    private final ObservableList<Videoresponse> videoresponsesList = FXCollections.observableArrayList();
    private final ObservableList<ClaimOption> claimOptionsList = FXCollections.observableArrayList();
    private final ObservableList<ClaimType> claimTypesList = FXCollections.observableArrayList();
    private final ObservableList<Playlist> playlistSourceList = FXCollections.observableArrayList();
    private final ObservableList<Playlist> playlistTargetList = FXCollections.observableArrayList();
    private final ObservableList<Asset> assetList = FXCollections.observableArrayList();
    private final ObservableList<Syndication> syndicationList = FXCollections.observableArrayList();
    private final SimpleIntegerProperty idProperty = new SimpleIntegerProperty();
    private final UploadViewVO uploadViewVO = new UploadViewVO();
    private final BeanPathAdapter<UploadViewVO> beanPathAdapter = new BeanPathAdapter<>(uploadViewVO);

    public UploadController() {
        idProperty.setValue(null);
    }

    @FXML
    void addUpload(final ActionEvent event) {
        final Upload upload = beanPathAdapter.getBean()
                .getUpload();

        final UploadControllerAddCommand command = commandProvider.get(UploadControllerAddCommand.class);
        command.upload = upload;
        command.playlists = playlistTargetList;
        command.account = uploadAccount.getValue();
        command.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(final WorkerStateEvent event) {
                // Cleanup (reset form)
                filesList.remove(uploadFile.getValue());
                uploadFile.getSelectionModel()
                        .selectNext();
                idProperty.setValue(null);
                uploadViewVO.setUpload(new Upload());
                uploadViewVO.reset();
                beanPathAdapter.setBean(uploadViewVO);
            }
        });
        command.setOnRunning(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(final WorkerStateEvent workerStateEvent) {
                final Node[] nodes = new Node[]{uploadAccount, uploadCategory,
                        uploadDescription,
                        uploadFile,
                        uploadTags,
                        uploadThumbnail,
                        uploadTitle};
                for (final Node node : nodes) {
                    node.getStyleClass().remove("input-invalid");
                }
            }
        });
        command.setOnFailed(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(final WorkerStateEvent event) {
                try {
                    final UploadValidationCode error = UploadValidationCode.valueOf(event.getSource()
                            .getException()
                            .getMessage());
                    switch (error) {
                        case ACCOUNT_NULL:
                            uploadAccount.getStyleClass().add("input-invalid");
                            break;
                        case CATEGORY_NULL:
                            uploadCategory.getStyleClass().add("input-invalid");
                            break;
                        case DESCRIPTION_ILLEGAL:
                        case DESCRIPTION_LENGTH:
                            uploadDescription.getStyleClass().add("input-invalid");
                            break;
                        case FILE_NULL:
                            uploadFile.getStyleClass().add("input-invalid");
                            break;
                        case TAGS_ILLEGAL:
                            uploadTags.getStyleClass().add("input-invalid");
                            break;
                        case THUMBNAIL_SIZE:
                            uploadThumbnail.getStyleClass().add("input-invalid");
                            break;
                        case TITLE_ILLEGAL:
                        case TITLE_NULL:
                            uploadTitle.getStyleClass().add("input-invalid");
                            break;
                    }
                } catch (final Exception e) {
                    event.getSource()
                            .getException()
                            .printStackTrace();
                }
            }
        });
        command.start();
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
        if (!directoryChooser.getInitialDirectory().isDirectory()) {
            directoryChooser.setInitialDirectory(new File(ApplicationData.HOME));
        }
        final File directory = directoryChooser.showDialog(null);
        if (directory != null) {
            uploadEnddir.setText(directory.getAbsolutePath());
        }
    }

    @FXML
    void openFiles(final ActionEvent event) {
        if (!fileChooser.getInitialDirectory().isDirectory()) {
            fileChooser.setInitialDirectory(new File(ApplicationData.HOME));
        }
        final List<File> files = fileChooser.showOpenMultipleDialog(null);
        if (files != null && files.size() > 0) {
            addUploadFiles(files);
        }
    }

    @FXML
    void openThumbnail(final ActionEvent event) {
        if (!fileChooser.getInitialDirectory().isDirectory()) {
            fileChooser.setInitialDirectory(new File(ApplicationData.HOME));
        }
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
        if ((template = templates.getSelectionModel()
                .getSelectedItem()) != null) {
            final RemoveTemplateCommand command = commandProvider.get(RemoveTemplateCommand.class);
            command.template = template;
            command.start();
        }
    }

    //TODO This reset feature bugs
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
        final Control[] controls = new Control[]{monetizeClaim, monetizeClaimType, monetizeClaimOption, monetizeEIDR, monetizeISAN,
                monetizeNumberEpisode, monetizeNumberSeason, monetizeTitleEpisode, monetizeTMSID, monetizeCustomID, monetizeNotes,
                monetizeDescription, monetizeTitle, monetizeInStreamDefaults, monetizeInStream, monetizeAsset, labelContentInformation,
                labelEIDR, labelISAN, labelNumberEpisode, labelNumberSeason, labelTitleEpisode, labelTMSID, labelCustomId,
                labelMonetizeTitle, labelNote, labelDescription};
        if (monetizePartner.isSelected()) {
            for (final Control control : controls) {
                control.setVisible(true);
            }
        } else {
            for (final Control control : controls) {
                control.setVisible(false);
            }
        }
    }

    @FXML
    void initialize() {
        assert addUpload != null : "fx:id=\"addUpload\" was not injected: check your FXML file 'Upload.fxml'.";
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
        assert monetizeAsset != null : "fx:id=\"monetizeAsset\" was not injected: check your FXML file 'Upload.fxml'.";
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
        assert monetizeSyndication != null : "fx:id=\"monetizeSyndication\" was not injected: check your FXML file 'Upload.fxml'.";
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

        beanPathAdapter.setBean(uploadViewVO);
        eventBus.register(this);
    }

    @Subscribe
    public void onModelAdded(final ModelAddedEvent modelAddedEvent) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                if (modelAddedEvent.getModel() instanceof Account) {
                    accountsList.add((Account) modelAddedEvent.getModel());
                    if (uploadAccount.getValue() == null && accountsList.size() > 0) {
                        uploadAccount.getSelectionModel()
                                .selectFirst();
                    }
                } else if (modelAddedEvent.getModel() instanceof Template) {

                    templatesList.add((Template) modelAddedEvent.getModel());
                    if (templates.getValue() == null && templatesList.size() > 0) {
                        templates.getSelectionModel()
                                .selectFirst();
                    }

                } else if (modelAddedEvent.getModel() instanceof Playlist
                        && playlistDao.fetchOneAccountByPlaylist((Playlist) modelAddedEvent.getModel())
                        .equals(uploadAccount.getValue())) {
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
                    templates.getSelectionModel()
                            .select((Template) modelUpdatedEvent.getModel());
                } else if (modelUpdatedEvent.getModel() instanceof Playlist
                        && playlistDao.fetchOneAccountByPlaylist((Playlist) modelUpdatedEvent.getModel())
                        .equals(uploadAccount.getValue())) {
                    if (((Playlist) modelUpdatedEvent.getModel()).getHidden()) {
                        playlistSourceList.remove(modelUpdatedEvent.getModel());
                        playlistTargetList.remove(modelUpdatedEvent.getModel());
                    } else if (playlistSourceList.contains(modelUpdatedEvent.getModel())) {
                        playlistSourceList.set(playlistSourceList.indexOf(modelUpdatedEvent.getModel()), (Playlist) modelUpdatedEvent.getModel());
                    } else if (playlistTargetList.contains(modelUpdatedEvent.getModel())) {
                        playlistTargetList.set(playlistTargetList.indexOf(modelUpdatedEvent.getModel()), (Playlist) modelUpdatedEvent.getModel());
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
                        uploadAccount.getSelectionModel()
                                .selectFirst();
                    }
                } else if (modelPostRemovedEvent.getModel() instanceof Template) {
                    templatesList.remove(modelPostRemovedEvent.getModel());
                    if (templates.getValue() == null && templatesList.size() > 0) {
                        templates.getSelectionModel()
                                .selectFirst();
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
        claimTypesList.addAll(ClaimType.values());
        claimOptionsList.addAll(ClaimOption.values());
        categoriesList.addAll(Category.values());
        assetList.addAll(Asset.values());
        syndicationList.addAll(Syndication.values());

        accountsList.addAll(accountDao.findAll());
        templatesList.addAll(templateDao.findAll());
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

        monetizeClaimOption.getSelectionModel()
                .selectedItemProperty()
                .addListener(new ClaimOptionChangeListener());
        monetizeAsset.getSelectionModel()
                .selectedItemProperty()
                .addListener(new AssetChangeListener());
        monetizePartner.selectedProperty()
                .addListener(new MonetizePartnerInvalidationListener());
        uploadLicense.getSelectionModel()
                .selectedItemProperty()
                .addListener(new LicenseChangeListener());
        uploadAccount.getSelectionModel()
                .selectedItemProperty()
                .addListener(new AccountChangeListener());

        templates.getSelectionModel()
                .selectedItemProperty()
                .addListener(new ChangeListener<Template>() {

                    @Override
                    public void changed(final ObservableValue<? extends Template> observable, final Template oldValue, final Template newValue) {
                        beanPathAdapter.setBean(uploadViewVO);
                    }
                });

        number.valueProperty()
                .addListener(new PreviewTitleInvalidationListener());
        uploadFile.getSelectionModel().selectedItemProperty()
                .addListener(new PreviewTitleInvalidationListenerFile());

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
        monetizeAsset.setItems(assetList);
        monetizeSyndication.setItems(syndicationList);

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

        previewTitle.textProperty()
                .bindBidirectional(uploadTitle.textProperty(), new PreviewTitleStringConverter());

        beanPathAdapter.bindBidirectional("template.defaultdir", fileChooser.initialDirectoryProperty(), File.class);
        beanPathAdapter.bindBidirectional("template.defaultdir", directoryChooser.initialDirectoryProperty(), File.class);

        beanPathAdapter.bindBidirectional("template.commentvote", uploadCommentvote.selectedProperty());

        final SimpleObjectProperty<File> defaultDirProperty = new SimpleObjectProperty<>();
        uploadDefaultdir.textProperty()
                .bindBidirectional(defaultDirProperty, new DefaultDirStringConverter());
        final SimpleObjectProperty<File> enddirProperty = new SimpleObjectProperty<>();
        uploadEnddir.textProperty()
                .bindBidirectional(enddirProperty, new DefaultDirStringConverter());

        beanPathAdapter.bindBidirectional("template.defaultdir", defaultDirProperty, File.class);
        beanPathAdapter.bindBidirectional("template.enddir", enddirProperty, File.class);
        beanPathAdapter.bindBidirectional("upload.enddir", enddirProperty, File.class);

        beanPathAdapter.bindBidirectional("template.description", uploadDescription.textProperty());
        beanPathAdapter.bindBidirectional("template.embed", uploadEmbed.selectedProperty());
        beanPathAdapter.bindBidirectional("template.mobile", uploadMobile.selectedProperty());
        beanPathAdapter.bindBidirectional("template.rate", uploadRate.selectedProperty());
        beanPathAdapter.bindBidirectional("template.keywords", uploadTags.textProperty());
        beanPathAdapter.bindBidirectional("template.title", uploadTitle.textProperty());
        beanPathAdapter.bindBidirectional("template.thumbnail", uploadThumbnail.textProperty());
        // *****************************************************************************************
        beanPathAdapter.bindBidirectional("template.category", uploadCategory.valueProperty(), Category.class);
        beanPathAdapter.bindBidirectional("template.comment", uploadComment.valueProperty(), Comment.class);
        beanPathAdapter.bindBidirectional("template.visibility", uploadVisibility.valueProperty(), Visibility.class);
        beanPathAdapter.bindBidirectional("template.videoresponse", uploadVideoresponse.valueProperty(), Videoresponse.class);
        beanPathAdapter.bindBidirectional("template.license", uploadLicense.valueProperty(), License.class);
        beanPathAdapter.bindBidirectional("template.monetizeClaimoption", monetizeClaimOption.valueProperty(), ClaimOption.class);
        beanPathAdapter.bindBidirectional("template.monetizeClaimtype", monetizeClaimType.valueProperty(), ClaimType.class);
        beanPathAdapter.bindBidirectional("template.monetizeAsset", monetizeAsset.valueProperty(), Asset.class);
        beanPathAdapter.bindBidirectional("template.monetizeSyndication", monetizeSyndication.valueProperty(), Syndication.class);
        // *****************************************************************************************
        beanPathAdapter.bindBidirectional("template.monetizeClaim", monetizeClaim.selectedProperty());
        beanPathAdapter.bindBidirectional("template.monetizeId", monetizeCustomID.textProperty());
        beanPathAdapter.bindBidirectional("template.monetizeDescription", monetizeDescription.textProperty());
        beanPathAdapter.bindBidirectional("template.monetizeEidr", monetizeEIDR.textProperty());
        beanPathAdapter.bindBidirectional("template.monetizeInstream", monetizeInStream.selectedProperty());
        beanPathAdapter.bindBidirectional("template.monetizeInstreamDefaults", monetizeInStreamDefaults.selectedProperty());
        beanPathAdapter.bindBidirectional("template.monetizeIsan", monetizeISAN.textProperty());
        beanPathAdapter.bindBidirectional("template.monetizeNotes", monetizeNotes.textProperty());
        beanPathAdapter.bindBidirectional("template.monetizeEpisodeNb", monetizeNumberEpisode.textProperty());
        beanPathAdapter.bindBidirectional("template.monetizeSeasonNb", monetizeNumberSeason.textProperty());
        beanPathAdapter.bindBidirectional("template.monetizeOverlay", monetizeOverlay.selectedProperty());
        beanPathAdapter.bindBidirectional("template.monetizeProduct", monetizeProductPlacement.selectedProperty());
        beanPathAdapter.bindBidirectional("template.monetizeTitle", monetizeTitle.textProperty());
        beanPathAdapter.bindBidirectional("template.monetizeTitleepisode", monetizeTitleEpisode.textProperty());
        beanPathAdapter.bindBidirectional("template.monetizeTmsid", monetizeTMSID.textProperty());
        beanPathAdapter.bindBidirectional("template.monetizeTrueview", monetizeTrueView.selectedProperty());
        beanPathAdapter.bindBidirectional("template.monetizePartner", monetizePartner.selectedProperty());
        // *****************************************************************************************
        // *****************************************************************************************
        // *****************************************************************************************
        beanPathAdapter.bindBidirectional("upload.commentvote", uploadCommentvote.selectedProperty());
        beanPathAdapter.bindBidirectional("upload.description", uploadDescription.textProperty());
        beanPathAdapter.bindBidirectional("upload.embed", uploadEmbed.selectedProperty());
        beanPathAdapter.bindBidirectional("upload.mobile", uploadMobile.selectedProperty());
        beanPathAdapter.bindBidirectional("upload.rate", uploadRate.selectedProperty());
        beanPathAdapter.bindBidirectional("upload.keywords", uploadTags.textProperty());
        beanPathAdapter.bindBidirectional("upload.title", uploadTitle.textProperty());
        beanPathAdapter.bindBidirectional("upload.thumbnail", uploadThumbnail.textProperty());
        beanPathAdapter.bindBidirectional("upload.dateOfStart", started.valueProperty(), Calendar.class);
        beanPathAdapter.bindBidirectional("upload.dateOfRelease", release.valueProperty(), Calendar.class);
        // *****************************************************************************************
        beanPathAdapter.bindBidirectional("upload.file", uploadFile.valueProperty(), File.class);
        beanPathAdapter.bindBidirectional("upload.category", uploadCategory.valueProperty(), Category.class);
        beanPathAdapter.bindBidirectional("upload.comment", uploadComment.valueProperty(), Comment.class);
        beanPathAdapter.bindBidirectional("upload.visibility", uploadVisibility.valueProperty(), Visibility.class);
        beanPathAdapter.bindBidirectional("upload.videoresponse", uploadVideoresponse.valueProperty(), Videoresponse.class);
        beanPathAdapter.bindBidirectional("upload.license", uploadLicense.valueProperty(), License.class);
        beanPathAdapter.bindBidirectional("upload.monetizeClaimoption", monetizeClaimOption.valueProperty(), ClaimOption.class);
        beanPathAdapter.bindBidirectional("upload.monetizeClaimtype", monetizeClaimType.valueProperty(), ClaimType.class);
        beanPathAdapter.bindBidirectional("upload.monetizeAsset", monetizeAsset.valueProperty(), Asset.class);
        beanPathAdapter.bindBidirectional("upload.monetizeSyndication", monetizeSyndication.valueProperty(), Syndication.class);
        // *****************************************************************************************
        beanPathAdapter.bindBidirectional("upload.facebook", uploadFacebook.selectedProperty());
        beanPathAdapter.bindBidirectional("upload.twitter", uploadTwitter.selectedProperty());
        beanPathAdapter.bindBidirectional("upload.message", uploadMessage.textProperty());
        // *****************************************************************************************
        beanPathAdapter.bindBidirectional("upload.monetizeClaim", monetizeClaim.selectedProperty());
        beanPathAdapter.bindBidirectional("upload.monetizeId", monetizeCustomID.textProperty());
        beanPathAdapter.bindBidirectional("upload.monetizeDescription", monetizeDescription.textProperty());
        beanPathAdapter.bindBidirectional("upload.monetizeEidr", monetizeEIDR.textProperty());
        beanPathAdapter.bindBidirectional("upload.monetizeInstream", monetizeInStream.selectedProperty());
        beanPathAdapter.bindBidirectional("upload.monetizeInstreamDefaults", monetizeInStreamDefaults.selectedProperty());
        beanPathAdapter.bindBidirectional("upload.monetizeIsan", monetizeISAN.textProperty());
        beanPathAdapter.bindBidirectional("upload.monetizeNotes", monetizeNotes.textProperty());
        beanPathAdapter.bindBidirectional("upload.monetizeEpisodeNb", monetizeNumberEpisode.textProperty());
        beanPathAdapter.bindBidirectional("upload.monetizeSeasonNb", monetizeNumberSeason.textProperty());
        beanPathAdapter.bindBidirectional("upload.monetizeOverlay", monetizeOverlay.selectedProperty());
        beanPathAdapter.bindBidirectional("upload.monetizeProduct", monetizeProductPlacement.selectedProperty());
        beanPathAdapter.bindBidirectional("upload.monetizeTitle", monetizeTitle.textProperty());
        beanPathAdapter.bindBidirectional("upload.monetizeTitleepisode", monetizeTitleEpisode.textProperty());
        beanPathAdapter.bindBidirectional("upload.monetizeTmsid", monetizeTMSID.textProperty());
        beanPathAdapter.bindBidirectional("upload.monetizeTrueview", monetizeTrueView.selectedProperty());
        beanPathAdapter.bindBidirectional("upload.monetizePartner", monetizePartner.selectedProperty());

        beanPathAdapter.bindBidirectional("upload.id", idProperty);
        beanPathAdapter.bindBidirectional("template", templates.valueProperty(), Template.class);
    }

    private void initSelection() {

        final ComboBox<?>[] controls = new ComboBox[]{uploadVisibility, uploadComment, uploadLicense, uploadVideoresponse, uploadAccount,
                monetizeClaimOption, monetizeClaimType, uploadCategory, templates};

        for (final ComboBox<?> comboBox : controls) {
            comboBox.getSelectionModel()
                    .selectFirst();
        }
    }

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

    public void addUploadFiles(final List<File> files) {
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

    public void fromUpload(final Upload item) {
        uploadViewVO.setUpload(item);
        beanPathAdapter.setBean(uploadViewVO);
    }

    // {{ INNER CLASSES
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
            extendedPlaceholders.setNumber(number.getValue());
            return extendedPlaceholders.replace(value);
        }
    }

    private final class PreviewTitleInvalidationListener implements InvalidationListener {

        @Override
        public void invalidated(final Observable observable) {
            final String value = uploadTitle.getText();
            uploadTitle.setText("");
            uploadTitle.setText(value);
        }

    }

    private final class PreviewTitleInvalidationListenerFile implements ChangeListener<File> {

        @Override
        public void changed(final ObservableValue<? extends File> observableValue, final File file, final File file2) {
            final String value = uploadTitle.getText();
            uploadTitle.setText("");
            uploadTitle.setText(value);
        }
    }


    private final class LicenseChangeListener implements ChangeListener<License> {
        final CheckBox[] controls = new CheckBox[]{monetizeClaim, monetizeOverlay, monetizeTrueView, monetizeInStream,
                monetizeInStreamDefaults, monetizeProductPlacement};

        @Override
        public void changed(final ObservableValue<? extends License> observable, final License oldValue, final License newValue) {
            if (newValue == null) {
                return;
            }
            switch (newValue) {
                case CREATIVE_COMMONS:
                    for (final CheckBox checkBox : controls) {
                        checkBox.setSelected(false);
                    }
                    partnerPane.setDisable(true);
                    break;
                case YOUTUBE:
                    partnerPane.setDisable(false);
                    break;
            }
        }
    }

    private final class ClaimOptionChangeListener implements ChangeListener<ClaimOption> {
        final CheckBox[] controls = new CheckBox[]{monetizeClaim, monetizeOverlay, monetizeTrueView, monetizeInStream,
                monetizeInStreamDefaults, monetizeProductPlacement};

        @Override
        public void changed(final ObservableValue<? extends ClaimOption> observable, final ClaimOption oldValue, final ClaimOption newValue) {
            if (!monetizePartner.isSelected()) {
                return;
            }
            if (newValue == null) {
                return;
            }
            switch (newValue) {
                case MONETIZE:
                    for (final CheckBox checkBox : controls) {
                        checkBox.setVisible(true);
                    }
                    break;
                case BLOCK:
                case TRACK:
                    for (final CheckBox checkBox : controls) {
                        checkBox.setSelected(false);
                        checkBox.setVisible(false);
                    }
                    break;
            }
        }
    }

    private final class AssetChangeListener implements ChangeListener<Asset> {
        @Override
        public void changed(final ObservableValue<? extends Asset> observable, final Asset oldValue, final Asset newValue) {
            if (newValue == null) {
                return;
            }
            switch (newValue) {
                case MOVIE:
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
                    break;
                case TV:
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
                    break;
                case WEB:
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
            if (idProperty.getValue() == null) {
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

    private final static class AccountListViewConverter extends StringConverter<Account> {
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

    private final static class DragExitedCallback implements EventHandler<DragEvent> {
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

    private final static class DragOverCallback implements EventHandler<DragEvent> {
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
