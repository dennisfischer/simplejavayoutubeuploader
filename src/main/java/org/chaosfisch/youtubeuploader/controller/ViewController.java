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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.chaosfisch.exceptions.SystemException;
import org.chaosfisch.util.GsonHelper;
import org.chaosfisch.util.InputDialog;
import org.chaosfisch.youtubeuploader.db.dao.TemplateDao;
import org.chaosfisch.youtubeuploader.db.data.*;
import org.chaosfisch.youtubeuploader.db.generated.tables.daos.AccountDao;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Account;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template;
import org.chaosfisch.youtubeuploader.services.PlaylistService;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ViewController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

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
	void menuConnectServer(final ActionEvent event) {
		//TODO for @version 3.0.1.0
	}

	@FXML
	void menuOpen(final ActionEvent event) {
		uploadController.openFiles(event);
	}

	@Inject
	private PlaylistService  playlistService;
	@Inject
	private UploadController uploadController;
	@Inject
	private AccountDao       accountDao;
	@Inject
	private TemplateDao      templateDao;

	// @Inject private RemoteClient remoteClient;

	public static final Template standardTemplate;

	static {
		standardTemplate = new Template();
		standardTemplate.setEmbed(true);
		standardTemplate.setMobile(true);
		standardTemplate.setCommentvote(true);
		standardTemplate.setRate(true);
		standardTemplate.setComment(Comment.ALLOWED);
		standardTemplate.setVisibility(Visibility.PUBLIC);
		standardTemplate.setVideoresponse(Videoresponse.MODERATED);
		standardTemplate.setLicense(License.YOUTUBE);
		standardTemplate.setNumber(0);
		standardTemplate.setFacebook(false);
		standardTemplate.setTwitter(false);
		standardTemplate.setDefaultdir(new File(System.getProperty("user.home")));
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
					e.printStackTrace();
				}
				myDialog.close();
			}
		}
	}
}
