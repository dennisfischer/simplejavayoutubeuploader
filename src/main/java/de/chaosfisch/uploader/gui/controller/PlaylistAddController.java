/*
 * Copyright (c) 2014 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.gui.controller;

import com.google.inject.Inject;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.account.IAccountService;
import de.chaosfisch.google.youtube.playlist.IPlaylistService;
import de.chaosfisch.google.youtube.playlist.Playlist;
import de.chaosfisch.uploader.gui.renderer.AccountStringConverter;
import de.chaosfisch.uploader.gui.renderer.DialogHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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

	private static final Logger logger = LoggerFactory.getLogger(PlaylistAddController.class);
	private final ObservableList<Account> accountItems = FXCollections.observableArrayList();

	private final IPlaylistService playlistService;
	private final IAccountService accountService;
	private final DialogHelper dialogHelper;

	@Inject
	private PlaylistAddController(final IPlaylistService playlistService, final IAccountService accountService, final DialogHelper dialogHelper) {
		this.playlistService = playlistService;
		this.accountService = accountService;
		this.dialogHelper = dialogHelper;
	}

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
		dialogHelper.resetControlls(new Control[]{title, accounts});
		try {
			final Playlist playlist = new Playlist(title.getText(), accounts.getValue());
			playlist.setSummary(summary.getText());
			playlist.setPrivate_(playlistPrivate.isSelected());

			final Thread th = new Thread(createPlaylistAddTask(playlist));
			th.setDaemon(true);
			th.start();
		} catch (final IllegalArgumentException e) {
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

	private Task<Void> createPlaylistAddTask(final Playlist playlist) {
		final Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				updateProgress(-1, -1);
				updateMessage("Adding playlist...");
				playlistService.addYoutubePlaylist(playlist);
				return null;
			}

			@Override
			protected void failed() {
				logger.warn("Playlist add error", getException());
				dialogHelper.showErrorDialog(resources.getString("dialog.playlistadd.error.title"), resources.getString("dialog.playlistadd.error.message"));
			}

			@Override
			protected void succeeded() {
				updateMessage("Playlist added!");
				updateProgress(1, 1);
				closeDialog(null);
			}
		};
		dialogHelper.registerBusyTask(task);
		return task;
	}
}
