/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.uploader.gui.edit.left;

import de.chaosfisch.uploader.gui.DataModel;
import de.chaosfisch.youtube.account.AccountModel;
import de.chaosfisch.youtube.category.CategoryModel;
import de.chaosfisch.youtube.playlist.PlaylistModel;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javax.inject.Inject;

public class EditLeftPresenter {
	@FXML
	public ComboBox<String> files;

	@FXML
	public TextField title;

	@FXML
	public ComboBox<CategoryModel> categories;

	@FXML
	public TextArea description;

	@FXML
	public TextArea tags;

	@FXML
	public ComboBox<AccountModel> accounts;

	@FXML
	public ComboBox<PlaylistModel> playlists;

	@FXML
	public ListView<PlaylistModel> selectedPlaylists;

	@Inject
	protected DataModel dataModel;

	@FXML
	public void initialize() {
		bindData();
		selectData();
	}

	private void bindData() {
		categories.itemsProperty().bindBidirectional(dataModel.categoriesProperty());
		accounts.itemsProperty().bindBidirectional(dataModel.accountsProperty());
		accounts.valueProperty().bindBidirectional(dataModel.selectedAccountProperty());
		files.valueProperty().bindBidirectional(dataModel.selectedFileProperty());
		categories.valueProperty().bindBidirectional(dataModel.selectedCategoryProperty());
		accounts.valueProperty().addListener((observableValue, oldAccount, newAccount) -> {
			playlists.itemsProperty().unbind();
			playlists.itemsProperty().bind(newAccount.playlistsProperty());
		});

		playlists.getSelectionModel()
				.selectedItemProperty()
				.addListener((observableValue, oldPlaylist, newPlaylist) -> {
					if (null != newPlaylist) {
						selectedPlaylists.getItems().add(newPlaylist);
						playlists.getItems().remove(newPlaylist);
						playlists.setValue(null);
					}
				});

		selectedPlaylists.setCellFactory(new PlaylistListCellFactory());
		selectedPlaylists.itemsProperty().bindBidirectional(dataModel.selectedPlaylistsProperty());
		selectedPlaylists.getItems().addListener((ListChangeListener<PlaylistModel>) change -> {
			change.next();
			playlists.getItems().addAll(change.getRemoved());
		});
	}

	private void selectData() {
		categories.getSelectionModel().selectFirst();
		accounts.getSelectionModel().selectFirst();
	}
}
