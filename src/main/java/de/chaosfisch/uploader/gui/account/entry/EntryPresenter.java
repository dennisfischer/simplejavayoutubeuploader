/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.uploader.gui.account.entry;

import de.chaosfisch.youtube.account.AccountModel;
import de.chaosfisch.youtube.account.IAccountService;
import de.chaosfisch.youtube.playlist.PlaylistModel;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

import javax.inject.Inject;
import java.util.HashMap;

public class EntryPresenter {
	private static final double                       MAX_WIDTH_PANEL = 140;
	private final        HashMap<PlaylistModel, VBox> playlistPanels  = new HashMap<>(10);
	@Inject
	protected IAccountService accountService;
	@FXML
	private   FlowPane        flowpane;
	@FXML
	private   TitledPane      titledpane;
	private   AccountModel    account;

	public AccountModel getAccount() {
		return account;
	}

	public void setAccount(final AccountModel account) {
		this.account = account;
		titledpane.textProperty().bind(account.nameProperty());

		final ObservableList<PlaylistModel> playlists = account.getPlaylists();
		playlists.addListener((ListChangeListener<PlaylistModel>) change -> {
			while (change.next()) {
				if (change.wasRemoved()) {
					change.getRemoved().forEach(this::removePlaylistPanel);
				} else if (change.wasAdded()) {
					change.getAddedSubList().forEach(this::addPlaylistPanel);
				} else {
					System.out.println("DETECTED UNKNOWN CHANGE " + change.toString());
				}
			}
		});
		playlists.forEach(this::addPlaylistPanel);
	}

	private void removePlaylistPanel(final PlaylistModel playlistModel) {
		if (playlistPanels.containsKey(playlistModel)) {
			flowpane.getChildren().remove(playlistPanels.get(playlistModel));
		}
	}

	private void addPlaylistPanel(final PlaylistModel playlistModel) {
		final ImageView imageView = new ImageView(null != playlistModel.getThumbnail() ? playlistModel.getThumbnail() : getClass()
				.getResource("/de/chaosfisch/uploader/gui/edit/left/thumbnail-missing.png").toExternalForm());
		imageView.setPreserveRatio(true);
		imageView.setFitWidth(MAX_WIDTH_PANEL);
		final Label label = new Label();
		label.textProperty().bind(playlistModel.titleProperty());
		label.setWrapText(true);
		label.setMaxWidth(MAX_WIDTH_PANEL);

		final VBox vBox = new VBox(imageView, label);
		playlistPanels.put(playlistModel, vBox);
		flowpane.getChildren().add(vBox);
	}

	public void deleteAccount() {
		final Action action = Dialogs.create()
				.lightweight()
				.owner(titledpane.getParent())
				.title(String.format("Delete account %s?", account.getName()))
				.message(String.format("Do you really want to delete %s?", account.getName()))
				.showConfirm();
		if (Dialog.Actions.YES == action) {
			accountService.remove(account);
		}
	}
}
