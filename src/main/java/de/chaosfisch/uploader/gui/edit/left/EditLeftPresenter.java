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
import de.chaosfisch.uploader.gui.models.AccountModel;
import de.chaosfisch.uploader.gui.models.CategoryModel;
import de.chaosfisch.uploader.gui.models.PlaylistModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.PopOver;

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
	public Label playlists;

	@Inject
	protected DataModel dataModel;

	@FXML
	public void initialize() {
		bindData();
		selectData();
	}

	private void selectData() {
		categories.getSelectionModel().selectFirst();
		accounts.getSelectionModel().selectFirst();
	}

	private void bindData() {
		categories.itemsProperty().bindBidirectional(dataModel.categoriesProperty());
		accounts.itemsProperty().bindBidirectional(dataModel.accountsProperty());
		accounts.valueProperty().bindBidirectional(dataModel.selectedAccountProperty());
		files.valueProperty().bindBidirectional(dataModel.selectedFileProperty());
		categories.valueProperty().bindBidirectional(dataModel.selectedCategoryProperty());
	}

	@FXML
	public void selectPlaylist(final MouseEvent mouseEvent) {
		final ObservableList<PlaylistModel> playlists = null == accounts.getValue() ? FXCollections.observableArrayList() : accounts.getValue().getPlaylists();
		final CheckListView<PlaylistModel> checkListView = new CheckListView<>(playlists);


		final ScrollPane scrollPane = new ScrollPane(checkListView);
		scrollPane.setFitToHeight(true);
		scrollPane.setFitToWidth(true);
		scrollPane.setMinWidth(500);
		final HBox hBox = new HBox(scrollPane);
		hBox.setPadding(new Insets(10));
		double height = playlists.size() * 15;
		if (350 < height) {
			height = 350;
		}
		scrollPane.setMinHeight(height);
		final PopOver popOver = new PopOver(hBox);
		popOver.setDetachedTitle("Title for PopOver");
		popOver.setCornerRadius(10);
		popOver.setArrowLocation(PopOver.ArrowLocation.BOTTOM_CENTER);
		final Label label = (Label) mouseEvent.getSource();
		final Point2D point2D = label.localToScreen(260, 15);

		popOver.show((Node) mouseEvent.getSource(), point2D.getX(), point2D.getY(), Duration.millis(250));
		popOver.detach();
	}

}
