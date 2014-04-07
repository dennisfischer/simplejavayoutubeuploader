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

import de.chaosfisch.youtube.upload.IUploadService;
import de.chaosfisch.youtube.upload.UploadModel;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class UploadDataModel {

	private final SimpleIntegerProperty           maxSpeed        = new SimpleIntegerProperty();
	private final SimpleIntegerProperty           maxUploads      = new SimpleIntegerProperty();
	private final SimpleBooleanProperty           running         = new SimpleBooleanProperty();
	private final SimpleListProperty<UploadModel> selectedUploads = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final IUploadService uploadService;
	private final SimpleListProperty<UploadModel> uploads = new SimpleListProperty<>(FXCollections.observableArrayList());

	public UploadDataModel(final IUploadService uploadService) {
		this.uploadService = uploadService;
		initUploadData();
	}


	private void initUploadData() {
		uploads.bind(uploadService.uploadModelsProperty());
		maxUploads.bindBidirectional(uploadService.maxUploadsProperty());
		maxSpeed.bindBidirectional(uploadService.maxSpeedProperty());
		running.bind(uploadService.runningProperty());
	}

	public ObservableList<UploadModel> getSelectedUploads() {
		return selectedUploads.get();
	}

	public void setSelectedUploads(final ObservableList<UploadModel> selectedUploads) {
		this.selectedUploads.set(selectedUploads);
	}

	public SimpleListProperty<UploadModel> selectedUploadsProperty() {
		return selectedUploads;
	}

	public int getMaxSpeed() {
		return maxSpeed.get();
	}

	public void setMaxSpeed(final int maxSpeed) {
		this.maxSpeed.set(maxSpeed);
	}

	public SimpleIntegerProperty maxSpeedProperty() {
		return maxSpeed;
	}

	public int getMaxUploads() {
		return maxUploads.get();
	}

	public void setMaxUploads(final int maxUploads) {
		this.maxUploads.set(maxUploads);
	}

	public SimpleIntegerProperty maxUploadsProperty() {
		return maxUploads;
	}

	public boolean getRunning() {
		return running.get();
	}

	public SimpleBooleanProperty runningProperty() {
		return running;
	}

	public void addUploads(final List<UploadModel> uploads) {
		this.uploads.addAll(uploads);
	}

	public void removeUpload(final UploadModel uploadModel) {
		uploads.remove(uploadModel);
	}

	public void addUpload(final UploadModel model) {
		uploads.add(model);
	}

	public ObservableList<UploadModel> getUploads() {
		return uploads.get();
	}

	public void setUploads(final ObservableList<UploadModel> uploads) {
		this.uploads.set(uploads);
	}

	public SimpleListProperty<UploadModel> uploadsProperty() {
		return uploads;
	}
}
