/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.uploader.gui.upload;

import de.chaosfisch.uploader.gui.models.UploadModel;
import de.chaosfisch.uploader.gui.upload.entry.UploadEntryPresenter;
import de.chaosfisch.uploader.gui.upload.entry.UploadEntryView;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class UploadCellFactory implements Callback<ListView<UploadModel>, ListCell<UploadModel>> {
	@Override
	public ListCell<UploadModel> call(final ListView<UploadModel> uploadListView) {
		return new ListCell<UploadModel>() {

			protected UploadEntryView entry;

			@Override
			protected void updateItem(final UploadModel upload, final boolean empty) {
				super.updateItem(upload, empty);
				if (!empty) {
					if (null == entry) {
						entry = new UploadEntryView();
						((UploadEntryPresenter) entry.getPresenter()).setUploadModel(upload);
					}
				} else {
					entry = null;
				}
				setGraphic(entry);
			}
		};
	}
}
