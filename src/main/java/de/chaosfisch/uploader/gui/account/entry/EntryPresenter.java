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

import de.chaosfisch.uploader.gui.DataModel;
import de.chaosfisch.youtube.account.AccountModel;
import de.chaosfisch.youtube.playlist.PlaylistModel;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
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

	private static final double MAX_WIDTH_PANEL = 140;

	private final HashMap<PlaylistModel, VBox>     playlistPanels                 = new HashMap<>(10);
	private final SimpleSetProperty<PlaylistModel> playlistModelSimpleSetProperty = new SimpleSetProperty<>(FXCollections.observableSet());
	@Inject
	protected DataModel    dataModel;
	@FXML
	private   FlowPane     flowpane;
	@FXML
	private   TitledPane   titledpane;
	private   AccountModel account;

	public AccountModel getAccount() {
		return account;
	}

	public void setAccount(final AccountModel account) {
		this.account = account;
		titledpane.textProperty().bind(account.nameProperty());

		playlistModelSimpleSetProperty.addListener((SetChangeListener<PlaylistModel>) change -> {
			if (change.wasRemoved()) {
				removePlaylistPanel(change.getElementRemoved());
			} else if (change.wasAdded()) {
				addPlaylistPanel(change.getElementAdded());
			}
		});

		playlistModelSimpleSetProperty.set(dataModel.getPlaylists(account));
	}

	private void removePlaylistPanel(final PlaylistModel playlistModel) {
		if (playlistPanels.containsKey(playlistModel)) {
			flowpane.getChildren().remove(playlistPanels.get(playlistModel));
		}
	}

	private void addPlaylistPanel(final PlaylistModel playlistModel) {
		final ImageView imageView = new ImageView(null != playlistModel.getThumbnail() ? playlistModel.getThumbnail() : getClass().getResource(
				"/de/chaosfisch/uploader/gui/edit/left/thumbnail-missing.png").toExternalForm());
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
			dataModel.remove(account);
		}
	}

	public void validateAccount(final ActionEvent actionEvent) {
		throw new UnsupportedOperationException();
	}
}