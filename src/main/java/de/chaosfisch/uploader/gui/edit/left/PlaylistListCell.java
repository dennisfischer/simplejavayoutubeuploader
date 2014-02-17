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

import de.chaosfisch.uploader.gui.models.PlaylistModel;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PlaylistListCell extends ListCell<PlaylistModel> {
	private static final int MAX_IMAGE_HEIGHT = 60;
	private static final int MAX_NODE_HEIGHT = 100;
	private VBox node;

	@Override
	protected void updateItem(final PlaylistModel playlistModel, final boolean empty) {
		super.updateItem(playlistModel, empty);

		if (null == playlistModel) {
			node = null;
		} else if (null == node) {
			node = new VBox(0);
			node.setMaxHeight(MAX_NODE_HEIGHT);

			final HBox hBox = new HBox(3);
			final ImageView imageView = getImageView();
			final Node delete = getRemoveButton();
			final Label label = getLabel();

			hBox.getChildren().addAll(imageView, delete);
			node.getChildren().addAll(hBox, label);
		}
		setGraphic(node);
	}

	private Label getLabel() {
		return new Label(getItem().getTitle());
	}

	private ImageView getRemoveButton() {
		final ImageView delete = new ImageView(new Image(getClass().getResourceAsStream("/de/chaosfisch/uploader/gui/edit/left/remove.png")));
		delete.getStyleClass().add("image_remove");
		delete.setOnMouseClicked(mouseEvent -> {
			getListView().getItems().remove(getItem());
		});
		return delete;
	}

	private ImageView getImageView() {
		final ImageView imageView = new ImageView(getImage());
		imageView.setFitHeight(MAX_IMAGE_HEIGHT);
		imageView.setPreserveRatio(true);
		return imageView;
	}

	private Image getImage() {
		return new Image(getImageLocation(), true);
	}

	private String getImageLocation() {
		return null == getItem().getThumbnail() ? getClass().getResource("/de/chaosfisch/uploader/gui/edit/left/thumbnail-missing.png").toExternalForm() : getItem().getThumbnail();
	}
}
