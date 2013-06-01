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

import com.google.inject.Inject;
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
import org.chaosfisch.exceptions.SystemException;
import org.chaosfisch.google.youtube.PlaylistService;
import org.chaosfisch.util.DesktopUtil;
import org.chaosfisch.util.GsonHelper;
import org.chaosfisch.util.InputDialog;
import org.chaosfisch.youtubeuploader.ApplicationData;
import org.chaosfisch.youtubeuploader.SimpleJavaYoutubeUploader;
import org.chaosfisch.youtubeuploader.controller.renderer.AccountStringConverter;
import org.chaosfisch.youtubeuploader.controller.renderer.DirectoryOpenErrorDialog;
import org.chaosfisch.youtubeuploader.controller.renderer.URLOpenErrorDialog;
import org.chaosfisch.youtubeuploader.db.dao.AccountDao;
import org.chaosfisch.youtubeuploader.db.dao.TemplateDao;
import org.chaosfisch.youtubeuploader.db.data.*;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Account;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

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
		accounts.setItems(FXCollections.observableList(accountDao.findAll()));
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
		if (!DesktopUtil.openBrowser(url)) {
			new URLOpenErrorDialog(url);
		}
	}

	@FXML
	void openFAQ(final ActionEvent event) {
		final String url = "http://uploader.chaosfisch.com/faq.html";
		if (!DesktopUtil.openBrowser(url)) {
			new URLOpenErrorDialog(url);
		}
	}

	@FXML
	void openLogs(final ActionEvent event) {
		final String directory = ApplicationData.DATA_DIR;
		if (!DesktopUtil.openDirectory(directory)) {
			new DirectoryOpenErrorDialog(directory);
		}
	}

	@FXML
	void initialize() {
		assert menuAddPlaylist != null : "fx:id=\"menuAddPlaylist\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuAddTemplate != null : "fx:id=\"menuAddTemplate\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuClose != null : "fx:id=\"menuClose\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert menuOpen != null : "fx:id=\"menuOpen\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert migrateDatabase != null : "fx:id=\"migrateDatabase\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert openDocumentation != null : "fx:id=\"openDocumentation\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert openFAQ != null : "fx:id=\"openFAQ\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";
		assert openLogs != null : "fx:id=\"openLogs\" was not injected: check your FXML file 'SimpleJavaYoutubeUploader.fxml'.";

		try (InputStream addPlaylistStream = getClass().getResourceAsStream("/org/chaosfisch/youtubeuploader/resources/images/table_add.png");
			 InputStream addTemplateStream = getClass().getResourceAsStream("/org/chaosfisch/youtubeuploader/resources/images/page_add.png");
			 InputStream closeStream = getClass().getResourceAsStream("/org/chaosfisch/youtubeuploader/resources/images/cancel.png");
			 InputStream openStream = getClass().getResourceAsStream("/org/chaosfisch/youtubeuploader/resources/images/folder_explore.png");
			 InputStream databaseStream = getClass().getResourceAsStream("/org/chaosfisch/youtubeuploader/resources/images/database_refresh.png");
			 InputStream documentationStream = getClass().getResourceAsStream("/org/chaosfisch/youtubeuploader/resources/images/book.png");
			 InputStream faqStream = getClass().getResourceAsStream("/org/chaosfisch/youtubeuploader/resources/images/help.png");
			 InputStream logsStream = getClass().getResourceAsStream("/org/chaosfisch/youtubeuploader/resources/images/report.png")

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
	private PlaylistService  playlistService;
	@Inject
	private UploadController uploadController;
	@Inject
	private AccountDao       accountDao;
	@Inject
	private TemplateDao      templateDao;

	public static final Template standardTemplate;

	static {
		standardTemplate = new Template();
		standardTemplate.setEmbed(true);
		standardTemplate.setCommentvote(true);
		standardTemplate.setRate(true);
		standardTemplate.setComment(Comment.ALLOWED);
		standardTemplate.setVisibility(Visibility.PUBLIC);
		standardTemplate.setVideoresponse(Videoresponse.MODERATED);
		standardTemplate.setLicense(License.YOUTUBE);
		standardTemplate.setFacebook(false);
		standardTemplate.setTwitter(false);
		standardTemplate.setDefaultdir(new File(ApplicationData.HOME));
		standardTemplate.setMonetizeClaim(false);
		standardTemplate.setMonetizeOverlay(false);
		standardTemplate.setMonetizeTrueview(false);
		standardTemplate.setMonetizeProduct(false);
		standardTemplate.setMonetizeInstream(false);
		standardTemplate.setMonetizeInstreamDefaults(false);
		standardTemplate.setMonetizePartner(false);
		standardTemplate.setMonetizeClaimtype(ClaimType.AUDIO_VISUAL);
		standardTemplate.setMonetizeClaimoption(ClaimOption.MONETIZE);
		standardTemplate.setMonetizeAsset(Asset.WEB);
		standardTemplate.setMonetizeSyndication(Syndication.GLOBAL);
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

				final Template template = GsonHelper.fromJSON(GsonHelper.toJSON(standardTemplate), Template.class);
				template.setName(textfield.getText());
				template.setDefaultdir(new File(template.getDefaultdir().getPath()));
				templateDao.insert(template);
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
				playlist.setAccountId(accounts.getValue().getId());
				try {
					playlistService.addYoutubePlaylist(playlist);
				} catch (final SystemException e) {
					final MonologFX monologFX = new MonologFX(MonologFX.Type.ERROR);
					monologFX.setTitleText(resources.getString("dialog.playlistadd.error.title"));
					monologFX.setMessage(resources.getString((e.getErrorCode()
							.getClass()
							.getName() + '.' + e.getErrorCode().name()).toLowerCase(Locale.getDefault())));
					monologFX.showDialog();
				}
				myDialog.close();
			}
		}
	}
}
