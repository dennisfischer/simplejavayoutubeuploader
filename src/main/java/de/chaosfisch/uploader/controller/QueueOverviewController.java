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

import com.cathive.fx.guice.FXMLController;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import de.chaosfisch.google.youtube.upload.IUploadService;
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.google.youtube.upload.events.UploadAdded;
import de.chaosfisch.google.youtube.upload.events.UploadRemoved;
import de.chaosfisch.uploader.controller.renderer.QueueUploadCellRenderer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.ResourceBundle;

@FXMLController
public class QueueOverviewController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private ListView<Upload> queueListView;
	private final ObservableList<Upload> uploads = FXCollections.observableArrayList();

	@Inject
	private IUploadService          uploadService;
	@Inject
	private QueueUploadCellRenderer queueUploadCellRenderer;

	@FXML
	void initialize() {
		assert null != queueListView : "fx:id=\"queueListView\" was not injected: check your FXML file 'QueueOverview.fxml'.";
		queueListView.setCellFactory(queueUploadCellRenderer);
		queueListView.setItems(uploads);

		uploads.addAll(uploadService.getAll());
	}

	@Subscribe
	public void onUploadAdded(final UploadAdded event) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				uploads.add(event.getUpload());
			}
		});
	}

	@Subscribe
	public void onUploadRemoved(final UploadRemoved event) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				uploads.remove(event.getUpload());
			}
		});
	}
}
