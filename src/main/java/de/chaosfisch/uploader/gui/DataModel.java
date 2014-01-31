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
		final UploadModel uploadModel2 = new UploadModel();
		uploadModel2.setTitle("Test");
		uploadModel2.setEnd(LocalDateTime.now());
		uploadModel2.setProgress(0.7f);
		uploadModel2.setRelease(LocalDateTime.now());
		uploadModel2.setStart(LocalDateTime.now());
		uploadModel2.setStopAfter(false);
		uploadModel2.setStatus(Status.FAILED);
		final UploadModel uploadModel3 = new UploadModel();
		uploadModel3.setTitle("Test");
		uploadModel3.setEnd(LocalDateTime.now());
		uploadModel3.setProgress(0.7f);
		uploadModel3.setRelease(LocalDateTime.now());
		uploadModel3.setStart(LocalDateTime.now());
		uploadModel3.setStopAfter(false);
		uploadModel3.setStatus(Status.RUNNING);
		final UploadModel uploadModel4 = new UploadModel();
		uploadModel4.setTitle("Test");
		uploadModel4.setEnd(LocalDateTime.now());
		uploadModel4.setProgress(0.7f);
		uploadModel4.setRelease(LocalDateTime.now());
		uploadModel4.setStart(LocalDateTime.now());
		uploadModel4.setStopAfter(false);
		uploadModel4.setStatus(Status.ABORTED);
		final UploadModel uploadModel5 = new UploadModel();
		uploadModel5.setTitle("Test");
		uploadModel5.setEnd(LocalDateTime.now());
		uploadModel5.setProgress(0.7f);
		uploadModel5.setRelease(LocalDateTime.now());
		uploadModel5.setStart(LocalDateTime.now());
		uploadModel5.setStopAfter(false);
		uploadModel5.setStatus(Status.FINISHED);
		uploads.addAll(uploadModel1, uploadModel2, uploadModel3, uploadModel4, uploadModel5);
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

	public ObservableList<UploadModel> getSelectedUploads() {
		return selectedUploads;
	}

	public void setSelectedUploads(final ObservableList<UploadModel> selectedUploads) {
		this.selectedUploads = selectedUploads;
	}

	public void removeUpload(final UploadModel uploadModel) {
		uploads.remove(uploadModel);
	}

	public void addUpload(final UploadModel model) {
		uploads.add(model);
	}
}
