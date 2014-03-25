/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.upload;

import de.chaosfisch.data.upload.IUploadDAO;
import de.chaosfisch.youtube.upload.metadata.IMetadataService;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

import java.util.Collection;

public class YouTubeUploadService implements IUploadService {

	private final SimpleListProperty<UploadModel> uploadModels = new SimpleListProperty<>(
			FXCollections.observableArrayList());
	private final Uploader   uploader;
	private final IUploadDAO uploadDAO;

	public YouTubeUploadService(final IUploadDAO uploadDAO, final IMetadataService metadataService) {
		this.uploadDAO = uploadDAO;
		uploader = new Uploader(this, metadataService);
	}

	public Collection<UploadModel> getAll() {
		//return uploadDAO.getAll();
		return null;
	}

	@Override
	public void store(final UploadModel uploadModel) {
		uploadModels.add(uploadModel);
	}

	public void remove(final UploadModel uploadModel) {
		uploadModels.remove(uploadModel);
	}

	@Override
	public UploadModel fetchNextUpload() {
		return null;
	}

	public int count() {
		return uploadDAO.count();
	}

	@Override
	public int countUnprocessed() {
		return uploadDAO.countUnprocessed();
	}

	@Override
	public long countReadyStarttime() {
		return uploadDAO.countReadyStarttime();
	}

	@Override
	public void resetUnfinishedUploads() {
		//TODO
	}

	@Override
	public void startUploading() {
		uploader.run();
	}

	@Override
	public void stopUploading() {
		uploader.shutdown();
	}

	@Override
	public void startStarttimeCheck() {
		uploader.runStarttimeChecker();
	}

	@Override
	public void stopStarttimeCheck() {
		uploader.stopStarttimeChecker();
	}

	@Override
	public void abort(final UploadModel uploadModel) {
		uploader.abort(uploadModel);
	}

	@Override
	public long getStarttimeDelay() {
		//TODO
		return 0;
	}

	@Override
	public SimpleIntegerProperty maxSpeedProperty() {
		return uploader.maxSpeedProperty();
	}

	@Override
	public SimpleIntegerProperty maxUploadsProperty() {
		return uploader.maxUploadsProperty();
	}

	@Override
	public ReadOnlyBooleanProperty runningProperty() {
		return uploader.runningProperty();
	}

	@Override
	public SimpleListProperty<UploadModel> uploadModelsProperty() {
		return uploadModels;
	}
}
