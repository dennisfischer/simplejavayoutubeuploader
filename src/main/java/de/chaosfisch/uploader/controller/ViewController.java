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
import com.google.inject.Inject;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.account.IAccountService;
import de.chaosfisch.google.youtube.playlist.IPlaylistService;
import de.chaosfisch.google.youtube.playlist.Playlist;
import de.chaosfisch.google.youtube.upload.metadata.License;
import de.chaosfisch.google.youtube.upload.metadata.Metadata;
import de.chaosfisch.google.youtube.upload.metadata.Monetization;
import de.chaosfisch.google.youtube.upload.metadata.Social;
import de.chaosfisch.google.youtube.upload.metadata.permissions.*;
import de.chaosfisch.serialization.IJsonSerializer;
import de.chaosfisch.uploader.ApplicationData;
import de.chaosfisch.uploader.SimpleJavaYoutubeUploader;
import de.chaosfisch.uploader.controller.renderer.AccountStringConverter;
import de.chaosfisch.uploader.controller.renderer.DirectoryOpenErrorDialog;
import de.chaosfisch.uploader.controller.renderer.InputDialog;
import de.chaosfisch.uploader.controller.renderer.URLOpenErrorDialog;
import de.chaosfisch.uploader.template.ITemplateService;
import de.chaosfisch.uploader.template.Template;
import de.chaosfisch.util.DesktopUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import jfxtras.labs.dialogs.MonologFX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

@FXMLController
public class ViewController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private MenuItem menuAddPlaylist;

	@FXML
	private MenuItem menuAddTemplate;

	@FXML
	private MenuItem menuClose;

	@FXML
	private MenuItem menuOpen;

	@FXML
	private MenuItem migrateDatabase;

	@FXML
	private MenuItem openDocumentation;

	@FXML
	private MenuItem openFAQ;

	@FXML
	private MenuItem openLogs;

	private static final Logger logger = LoggerFactory.getLogger(ViewController.class);
	@Inject
	private DesktopUtil     desktopUtil;
	@Inject
	private IJsonSerializer jsonSerializer;

	@FXML
	void fileDragDropped(final DragEvent event) {
		final Dragboard db = event.getDragboard();

		if (db.hasFiles()) {
			uploadController.addUploadFiles(db.getFiles());
			event.setDropCompleted(true);
		} else {
			event.setDropCompleted(false);
		}
		event.consume();
	}

	@FXML
	void fileDragOver(final DragEvent event) {
		final Dragboard db = event.getDragboard();
		if (db.hasFiles()) {
			event.acceptTransferModes(TransferMode.COPY);
		}
		event.consume();
	}

	@FXML
	void menuAddPlaylist(final ActionEvent event) {
		final TextField title = new TextField();
		final CheckBox playlistPrivate = new CheckBox();
		final TextArea summary = new TextArea();
		final ChoiceBox<Account> accounts = new ChoiceBox<>();
		accounts.setMaxWidth(Double.MAX_VALUE);
		accounts.setConverter(new AccountStringConverter());
		accounts.setItems(FXCollections.observableList(accountService.getAll()));
		accounts.getSelectionModel().selectFirst();

		final Object[] message = {resources.getString("playlistDialog.playlistLabel"), title,
								  resources.getString("playlistDialog.descriptionLabel"), summary,
								  resources.getString("playlistDialog.playlistPrivate"), playlistPrivate,
								  resources.getString("playlistDialog.playlistAccount"), accounts};
		final InputDialog myDialog = new InputDialog(resources.getString("playlistDialog.addPlaylistLabel"), message);

		myDialog.setCallback(new PlaylistAddDialogCallback(playlistPrivate, summary, accounts, title, myDialog));
	}

	@FXML
	void menuAddTemplate(final ActionEvent event) {
		final TextField textfield = new TextField();
		final Object[] message = {resources.getString("templateDialog.templateLabel"), textfield};

		final InputDialog myDialog = new InputDialog(resources.getString("templateDialog.addTemplateLabel"), message);

		myDialog.setCallback(new TemplateAddDialogCallback(myDialog, textfield));
	}

	@FXML
	void menuClose(final ActionEvent event) {
		Platform.exit();
	}

	@FXML
	void menuOpen(final ActionEvent event) {
		uploadController.openFiles(event);
	}

	@FXML
	void migrateDatabase(final ActionEvent event) {
		final Preferences prefs = Preferences.userNodeForPackage(SimpleJavaYoutubeUploader.class);
		prefs.putInt("version", 0);

		final MonologFX monologFX = new MonologFX(MonologFX.Type.INFO);
		monologFX.setTitleText(resources.getString("dialog.migratedatabase.title"));
		monologFX.setMessage(resources.getString("dialog.migratedatabase.text"));
		monologFX.showDialog();
	}

	@FXML
	void openDocumentation(final ActionEvent event) {
		final String url = "http://uploader.chaosfisch.com/documentation.html";
		if (!desktopUtil.openBrowser(url)) {
			new URLOpenErrorDialog(url, resources);
		}
	}

	@FXML
	void openFAQ(final ActionEvent event) {
		final String url = "http://uploader.chaosfisch.com/faq.html";
		if (!desktopUtil.openBrowser(url)) {
			new URLOpenErrorDialog(url, resources);
		}
	}

	@FXML
	void openLogs(final ActionEvent event) {
		final String directory = ApplicationData.DATA_DIR;
		if (!desktopUtil.openDirectory(directory)) {
			new DirectoryOpenErrorDialog(directory, resources);
		}
	}

	@FXML
	void initialize() {
		assert null != menuAddPlaylist : "fx:id=\"menuAddPlaylist\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert null != menuAddTemplate : "fx:id=\"menuAddTemplate\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert null != menuClose : "fx:id=\"menuClose\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert null != menuOpen : "fx:id=\"menuOpen\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert null != migrateDatabase : "fx:id=\"migrateDatabase\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert null != openDocumentation : "fx:id=\"openDocumentation\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert null != openFAQ : "fx:id=\"openFAQ\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert null != openLogs : "fx:id=\"openLogs\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";

		loadMenuGraphics();
	}

	private void loadMenuGraphics() {
		try (InputStream addPlaylistStream = getClass().getResourceAsStream("/de/chaosfisch/uploader/resources/images/table_add.png");
			 InputStream addTemplateStream = getClass().getResourceAsStream("/de/chaosfisch/uploader/resources/images/page_add.png");
			 InputStream closeStream = getClass().getResourceAsStream("/de/chaosfisch/uploader/resources/images/cancel.png");
			 InputStream openStream = getClass().getResourceAsStream("/de/chaosfisch/uploader/resources/images/folder_explore.png");
			 InputStream databaseStream = getClass().getResourceAsStream("/de/chaosfisch/uploader/resources/images/database_refresh.png");
			 InputStream documentationStream = getClass().getResourceAsStream("/de/chaosfisch/uploader/resources/images/book.png");
			 InputStream faqStream = getClass().getResourceAsStream("/de/chaosfisch/uploader/resources/images/help.png");
			 InputStream logsStream = getClass().getResourceAsStream("/de/chaosfisch/uploader/resources/images/report.png")

		) {
			menuAddPlaylist.setGraphic(new ImageView(new Image(addPlaylistStream)));
			menuAddTemplate.setGraphic(new ImageView(new Image(addTemplateStream)));
			menuClose.setGraphic(new ImageView(new Image(closeStream)));
			menuOpen.setGraphic(new ImageView(new Image(openStream)));
			migrateDatabase.setGraphic(new ImageView(new Image(databaseStream)));
			openDocumentation.setGraphic(new ImageView(new Image(documentationStream)));
			openFAQ.setGraphic(new ImageView(new Image(faqStream)));
			openLogs.setGraphic(new ImageView(new Image(logsStream)));
		} catch (IOException e) {
			logger.warn("Icons not loaded", e);
		}
	}

	@Inject
	private IPlaylistService playlistService;
	@Inject
	private UploadController uploadController;
	@Inject
	private IAccountService  accountService;
	@Inject
	private ITemplateService templateService;

	public static final Template standardTemplate;

	static {
		final Permissions permissions = new Permissions();
		permissions.setEmbed(true);
		permissions.setCommentvote(true);
		permissions.setComment(Comment.ALLOWED);
		permissions.setRate(true);
		permissions.setVisibility(Visibility.PUBLIC);
		permissions.setVideoresponse(Videoresponse.MODERATED);

		final Social social = new Social();
		social.setFacebook(false);
		social.setTwitter(false);
		social.setMessage("");

		final Monetization monetization = new Monetization();
		monetization.setClaim(false);
		monetization.setOverlay(false);
		monetization.setTrueview(false);
		monetization.setProduct(false);
		monetization.setInstream(false);
		monetization.setInstreamDefaults(false);
		monetization.setPartner(false);
		monetization.setClaimtype(ClaimType.AUDIO_VISUAL);
		monetization.setClaimoption(ClaimOption.MONETIZE);
		monetization.setAsset(Asset.WEB);
		monetization.setSyndication(Syndication.GLOBAL);

		final Metadata metadata = new Metadata();
		metadata.setLicense(License.YOUTUBE);
		metadata.setDescription("");
		metadata.setTitle("");
		metadata.setKeywords("");

		standardTemplate = new Template();
		standardTemplate.setPermissions(permissions);
		standardTemplate.setSocial(social);
		standardTemplate.setMonetization(monetization);
		standardTemplate.setMetadata(metadata);
		standardTemplate.setThumbnail(null);
		standardTemplate.setDefaultdir(new File(ApplicationData.HOME));
	}

	private final class TemplateAddDialogCallback implements EventHandler<ActionEvent> {
		private final InputDialog myDialog;
		private final TextField   textfield;

		private TemplateAddDialogCallback(final InputDialog myDialog, final TextField textfield) {
			this.myDialog = myDialog;
			this.textfield = textfield;
		}

		@Override
		public void handle(final ActionEvent event) {
			if (!textfield.getText().isEmpty()) {

				final Template template = jsonSerializer.fromJSON(jsonSerializer.toJSON(standardTemplate), Template.class);
				template.setName(textfield.getText());
				template.setDefaultdir(new File(template.getDefaultdir().getPath()));
				templateService.insert(template);
				myDialog.close();
			}
		}
	}

	private final class PlaylistAddDialogCallback implements EventHandler<ActionEvent> {
		private final CheckBox           playlistPrivate;
		private final TextArea           summary;
		private final ChoiceBox<Account> accounts;
		private final TextField          title;
		private final InputDialog        myDialog;

		private PlaylistAddDialogCallback(final CheckBox playlistPrivate, final TextArea summary, final ChoiceBox<Account> accounts, final TextField title, final InputDialog myDialog) {
			this.playlistPrivate = playlistPrivate;
			this.summary = summary;
			this.accounts = accounts;
			this.title = title;
			this.myDialog = myDialog;
		}

		@Override
		public void handle(final ActionEvent event) {
			if (!title.getText().isEmpty() && !accounts.getSelectionModel().isEmpty()) {
				final Playlist playlist = new Playlist();
				playlist.setTitle(title.getText());
				playlist.setSummary(summary.getText());
				playlist.setPrivate(playlistPrivate.isSelected());
				playlist.setAccount(accounts.getValue());
				try {
					playlistService.addYoutubePlaylist(playlist);
				} catch (final Exception e) {
					final MonologFX monologFX = new MonologFX(MonologFX.Type.ERROR);
					monologFX.setTitleText(resources.getString("dialog.playlistadd.error.title"));
					//FIXME error message
					monologFX.setMessage(resources.getString(""));
					monologFX.showDialog();
				}
				myDialog.close();
			}
		}
	}
}
