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

import com.google.inject.Inject;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.account.IAccountService;
import de.chaosfisch.google.youtube.playlist.IPlaylistService;
import de.chaosfisch.google.youtube.playlist.Playlist;
import de.chaosfisch.google.youtube.playlist.PlaylistIOException;
import de.chaosfisch.google.youtube.playlist.PlaylistInvalidResponseException;
import de.chaosfisch.uploader.renderer.AccountStringConverter;
import de.chaosfisch.uploader.renderer.DialogHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class PlaylistAddController extends UndecoratedDialogController {

	@FXML
	private URL location;

	@FXML
	private ChoiceBox<Account> accounts;

	@FXML
	private CheckBox playlistPrivate;

	@FXML
	private TextArea summary;

	@FXML
	private TextField title;

	private final ObservableList<Account> accountItems = FXCollections.observableArrayList();

	@Inject
	private IPlaylistService playlistService;
	@Inject
	private IAccountService  accountService;
	@Inject
	private DialogHelper     dialogHelper;

	private static final Logger logger = LoggerFactory.getLogger(PlaylistAddController.class);

	@FXML
	void initialize() {
		assert null != accounts : "fx:id=\"accounts\" was not injected: check your FXML file 'PlaylistAddDialog.fxml'.";
		assert null != playlistPrivate : "fx:id=\"playlistPrivate\" was not injected: check your FXML file 'PlaylistAddDialog.fxml'.";
		assert null != summary : "fx:id=\"summary\" was not injected: check your FXML file 'PlaylistAddDialog.fxml'.";
		assert null != title : "fx:id=\"title\" was not injected: check your FXML file 'PlaylistAddDialog.fxml'.";

		accountItems.addAll(accountService.getAll());
		accounts.setItems(accountItems);
		accounts.getSelectionModel().selectFirst();
		accounts.setConverter(new AccountStringConverter());
	}

	@FXML
	public void addPlaylist(final ActionEvent actionEvent) {
		dialogHelper.resetControlls(new Control[] {title, accounts});
		try {
			final Playlist playlist = new Playlist(title.getText(), accounts.getValue());
			playlist.setSummary(summary.getText());
			playlist.setPrivate_(playlistPrivate.isSelected());
			try {
				playlistService.addYoutubePlaylist(playlist);
			} catch (final PlaylistInvalidResponseException | PlaylistIOException e) {
				logger.warn("Playlist add error", e);
				dialogHelper.showErrorDialog(resources.getString("dialog.playlistadd.error.title"), resources.getString("dialog.playlistadd.error.message"));
			}
		} catch (IllegalArgumentException e) {
			switch (e.getMessage()) {
				case Playlist.Validation.TITLE:
				case Playlist.Validation.TITLE_SIZE:
					title.getStyleClass().add("input-invalid");
					title.setTooltip(TooltipBuilder.create()
							.autoHide(true)
							.text(resources.getString("validation.playlisttitle"))
							.build());
					title.getTooltip().show(title, dialogHelper.getTooltipX(title), dialogHelper.getTooltipY(title));
					break;
				case Playlist.Validation.ACCOUNT:
					accounts.getStyleClass().add("input-invalid");
					accounts.setTooltip(TooltipBuilder.create()
							.autoHide(true)
							.text(resources.getString("validation.account"))
							.build());
					accounts.getTooltip()
							.show(accounts, dialogHelper.getTooltipX(accounts), dialogHelper.getTooltipY(accounts));
					break;
			}
		}
	}
}
