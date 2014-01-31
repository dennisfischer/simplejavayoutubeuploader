/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.uploader.gui;

import de.chaosfisch.google.youtube.upload.Status;
import de.chaosfisch.uploader.gui.models.ProjectModel;
import de.chaosfisch.uploader.gui.models.UploadModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.util.List;

public class DataModel {

	private final ObservableList<UploadModel> uploads = FXCollections.observableArrayList();
	private final ObservableList<ProjectModel> projects = FXCollections.observableArrayList();
	private ObservableList<UploadModel> selectedUploads;


	public DataModel() {
		projects.addAll(new ProjectModel("Test 1"), new ProjectModel("Projekt 2"), new ProjectModel("Und 3"));

		final UploadModel uploadModel1 = new UploadModel();
		uploadModel1.setTitle("Test");
		uploadModel1.setEnd(LocalDateTime.now());
		uploadModel1.setProgress(0.7f);
		uploadModel1.setRelease(LocalDateTime.now());
		uploadModel1.setStart(LocalDateTime.now());
		uploadModel1.setStopAfter(false);
		uploadModel1.setStatus(Status.WAITING);
		uploads.addAll(uploadModel1);
	}

	public void addUploads(final List<UploadModel> uploads) {
		this.uploads.addAll(uploads);
	}

	public void addProjects(final List<ProjectModel> projects) {
		this.projects.addAll(projects);
	}

	public ObservableList<UploadModel> uploadObservableList() {
		return uploads;
	}

	public ObservableList<ProjectModel> projectObservableList() {
		return projects;
	}

	public void setSelectedUploads(final ObservableList<UploadModel> selectedUploads) {
		this.selectedUploads = selectedUploads;
	}

	public ObservableList<UploadModel> getSelectedUploads() {
		return selectedUploads;
	}

	public void removeUpload(final UploadModel uploadModel) {
		uploads.remove(uploadModel);
	}

	public void addUpload(final UploadModel model) {
		uploads.add(model);
	}
}
