/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.controller;

/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import org.chaosfisch.youtubeuploader.controller.renderer.QueueUploadCellRenderer;
import org.chaosfisch.youtubeuploader.db.dao.UploadDao;
import org.chaosfisch.youtubeuploader.db.events.ModelAddedEvent;
import org.chaosfisch.youtubeuploader.db.events.ModelRemovedEvent;
import org.chaosfisch.youtubeuploader.db.events.ModelUpdatedEvent;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class QueueOverviewController {

	@FXML
	private ResourceBundle					resources;

	@FXML
	private URL								location;

	@FXML
	private ListView<Upload>				queueListView;
	private final ObservableList<Upload>	uploads	= FXCollections.observableArrayList();

	@Inject
	private UploadDao						uploadDao;
	@Inject
	private EventBus						eventBus;
	@Inject
	private QueueUploadCellRenderer			queueUploadCellRenderer;

	@FXML
	void initialize() {
		assert queueListView != null : "fx:id=\"queueListView\" was not injected: check your FXML file 'QueueOverview.fxml'.";
		queueListView.setCellFactory(queueUploadCellRenderer);
		queueListView.setItems(uploads);

		uploads.addAll(uploadDao.findAll());
		eventBus.register(this);
	}

	private void onUploadAdded(final Upload upload) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				uploads.add(upload);
			}
		});
	}

	private void onUploadRemoved(final Upload upload) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				uploads.remove(upload);
			}
		});
	}

	private void onUploadUpdated(final Upload upload) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				if (uploads.contains(upload)) {
					final int index = uploads.indexOf(upload);
					uploads.remove(upload);
					uploads.add(index, upload);
				}
			}
		});
	}

	@Subscribe
	public void onModelAdded(final ModelAddedEvent modelAddedEvent) {
		if (modelAddedEvent.getModel() instanceof Upload) {
			onUploadAdded((Upload) modelAddedEvent.getModel());
		}
	}

	@Subscribe
	public void onModelUpdated(final ModelUpdatedEvent modelUpdatedEvent) {
		if (modelUpdatedEvent.getModel() instanceof Upload) {
			onUploadUpdated((Upload) modelUpdatedEvent.getModel());
		}
	}

	@Subscribe
	public void onModelRemoved(final ModelRemovedEvent modelRemovedEvent) {
		if (modelRemovedEvent.getModel() instanceof Upload) {
			onUploadRemoved((Upload) modelRemovedEvent.getModel());
		}
	}
}
