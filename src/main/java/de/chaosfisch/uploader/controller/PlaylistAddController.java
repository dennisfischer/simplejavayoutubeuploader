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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import jfxtras.labs.dialogs.MonologFX;

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

	@FXML
	void initialize() {
		assert null != accounts : "fx:id=\"accounts\" was not injected: check your FXML file 'PlaylistAddDialog.fxml'.";
		assert null != playlistPrivate : "fx:id=\"playlistPrivate\" was not injected: check your FXML file 'PlaylistAddDialog.fxml'.";
		assert null != summary : "fx:id=\"summary\" was not injected: check your FXML file 'PlaylistAddDialog.fxml'.";
		assert null != title : "fx:id=\"title\" was not injected: check your FXML file 'PlaylistAddDialog.fxml'.";

		accountItems.addAll(accountService.getAll());
		accounts.setItems(accountItems);
		accounts.getSelectionModel().selectFirst();
	}

	@FXML
	public void addPlaylist(final ActionEvent actionEvent) {
		try {
			final Playlist playlist = new Playlist(title.getText(), accounts.getValue());
			playlist.setTitle(title.getText());
			playlist.setSummary(summary.getText());
			playlist.setPrivate_(playlistPrivate.isSelected());
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
		} catch (IllegalArgumentException e) {
			//TODO Handle failed validation
			switch (e.getMessage()) {
				case Playlist.Validation.TITLE:
					break;
				case Playlist.Validation.ACCOUNT:
					break;
			}
		}
	}
}
