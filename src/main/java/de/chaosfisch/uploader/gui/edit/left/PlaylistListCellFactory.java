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

import de.chaosfisch.google.playlist.PlaylistModel;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class PlaylistListCellFactory implements Callback<ListView<PlaylistModel>, ListCell<PlaylistModel>> {
	@Override
	public ListCell<PlaylistModel> call(final ListView<PlaylistModel> playlistModelListView) {
		return new PlaylistListCell();
	}
}
