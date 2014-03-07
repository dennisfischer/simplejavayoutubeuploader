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

import de.chaosfisch.uploader.project.ProjectModel;
import de.chaosfisch.youtube.YouTubeFactory;
import de.chaosfisch.youtube.account.AccountModel;
import de.chaosfisch.youtube.account.IAccountService;
import de.chaosfisch.youtube.category.CategoryModel;
import de.chaosfisch.youtube.category.ICategoryService;
import de.chaosfisch.youtube.playlist.PlaylistModel;
import de.chaosfisch.youtube.upload.Status;
import de.chaosfisch.youtube.upload.UploadModel;
import de.chaosfisch.youtube.upload.metadata.License;
import de.chaosfisch.youtube.upload.permissions.Comment;
import de.chaosfisch.youtube.upload.permissions.ThreeD;
import de.chaosfisch.youtube.upload.permissions.Visibility;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class DataModel {

	private final SimpleListProperty<UploadModel>   uploads           = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final SimpleListProperty<ProjectModel>  projects          = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final SimpleListProperty<AccountModel>  accounts          = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final SimpleListProperty<CategoryModel> categories        = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final SimpleListProperty<String>        files             = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final SimpleListProperty<UploadModel>   selectedUploads   = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final SimpleListProperty<PlaylistModel> selectedPlaylists = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final SimpleListProperty<Visibility>    visibilities      = new SimpleListProperty<>(FXCollections.observableArrayList(
			Visibility
					.values()));
	private final SimpleListProperty<Comment>       comments          = new SimpleListProperty<>(FXCollections.observableArrayList(
			Comment.values()));
	private final SimpleListProperty<ThreeD>        threeDs           = new SimpleListProperty<>(FXCollections.observableArrayList(
			ThreeD.values()));
	private final SimpleListProperty<License>       licenses          = new SimpleListProperty<>(FXCollections.observableArrayList(
			License.values()));

	private final SimpleObjectProperty<AccountModel>  selectedAccount  = new SimpleObjectProperty<>();
	private final SimpleObjectProperty<CategoryModel> selectedCategory = new SimpleObjectProperty<>();
	private final SimpleStringProperty                selectedFile     = new SimpleStringProperty();
	private final ICategoryService categoryService;
	private final IAccountService  accountService;
//	private final IPlaylistService playlistService;


	public DataModel(final ICategoryService categoryService, final IAccountService accountService) {
		this.categoryService = categoryService;
		this.accountService = accountService;
		initSampleData();
		initBindings();
		initData();
	}

	private void initBindings() {
		categories.bind(categoryService.categoryModelsProperty());
		accounts.bind(accountService.accountModelsProperty());
		//	projects.bind(projectService.projectModelsProperty());
	}

	private void initData() {
		final Thread categoryThread = new Thread(() -> {
			try {
				categoryService.refresh(YouTubeFactory.getDefault());
			} catch (IOException e) {
				//TODO HANDLE
			}
		}, "Category_Loader");
		categoryThread.setDaemon(true);
		categoryThread.start();
	}

	private void initSampleData() {
		projects.addAll(new ProjectModel("Test 1"), new ProjectModel("Projekt 2"), new ProjectModel("Und 3"));

		final UploadModel uploadModel1 = new UploadModel();
		uploadModel1.setMetadataTitle("Test");
		uploadModel1.setDateTimeOfEnd(LocalDateTime.now());
		final float progress = 0.7f;
		uploadModel1.setProgress(progress);
		uploadModel1.setDateTimeOfRelease(LocalDateTime.now());
		uploadModel1.setDateTimeOfStart(LocalDateTime.now());
		uploadModel1.setStopAfter(false);
		uploadModel1.setStatus(Status.WAITING);
		final UploadModel uploadModel2 = new UploadModel();
		uploadModel1.setMetadataTitle("Test");
		uploadModel1.setDateTimeOfEnd(LocalDateTime.now());
		uploadModel2.setProgress(progress);
		uploadModel1.setDateTimeOfRelease(LocalDateTime.now());
		uploadModel1.setDateTimeOfStart(LocalDateTime.now());
		uploadModel2.setStopAfter(false);
		uploadModel2.setStatus(Status.FAILED);
		final UploadModel uploadModel3 = new UploadModel();
		uploadModel1.setMetadataTitle("Test");
		uploadModel1.setDateTimeOfEnd(LocalDateTime.now());
		uploadModel3.setProgress(progress);
		uploadModel1.setDateTimeOfRelease(LocalDateTime.now());
		uploadModel1.setDateTimeOfStart(LocalDateTime.now());
		uploadModel3.setStopAfter(false);
		uploadModel3.setStatus(Status.RUNNING);
		final UploadModel uploadModel4 = new UploadModel();
		uploadModel1.setMetadataTitle("Test");
		uploadModel1.setDateTimeOfEnd(LocalDateTime.now());
		uploadModel4.setProgress(progress);
		uploadModel1.setDateTimeOfRelease(LocalDateTime.now());
		uploadModel1.setDateTimeOfStart(LocalDateTime.now());
		uploadModel4.setStopAfter(false);
		uploadModel4.setStatus(Status.ABORTED);
		final UploadModel uploadModel5 = new UploadModel();
		uploadModel1.setMetadataTitle("Test");
		uploadModel1.setDateTimeOfEnd(LocalDateTime.now());
		uploadModel5.setProgress(progress);
		uploadModel1.setDateTimeOfRelease(LocalDateTime.now());
		uploadModel1.setDateTimeOfStart(LocalDateTime.now());
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

	public ObservableList<ProjectModel> getProjects() {
		return projects.get();
	}

	public void setProjects(final ObservableList<ProjectModel> projects) {
		this.projects.set(projects);
	}

	public SimpleListProperty<ProjectModel> projectsProperty() {
		return projects;
	}

	public ObservableList<AccountModel> getAccounts() {
		return accounts.get();
	}

	public void setAccounts(final ObservableList<AccountModel> accounts) {
		this.accounts.set(accounts);
	}

	public SimpleListProperty<AccountModel> accountsProperty() {
		return accounts;
	}

	public ObservableList<CategoryModel> getCategories() {
		return categories.get();
	}

	public void setCategories(final ObservableList<CategoryModel> categories) {
		this.categories.set(categories);
	}

	public SimpleListProperty<CategoryModel> categoriesProperty() {
		return categories;
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

	public ObservableList<String> getFiles() {
		return files.get();
	}

	public void setFiles(final ObservableList<String> files) {
		this.files.set(files);
	}

	public SimpleListProperty<String> filesProperty() {
		return files;
	}

	public ObservableList<PlaylistModel> getSelectedPlaylists() {
		return selectedPlaylists.get();
	}

	public void setSelectedPlaylists(final ObservableList<PlaylistModel> selectedPlaylists) {
		this.selectedPlaylists.set(selectedPlaylists);
	}

	public SimpleListProperty<PlaylistModel> selectedPlaylistsProperty() {
		return selectedPlaylists;
	}

	public String getSelectedFile() {
		return selectedFile.get();
	}

	public void setSelectedFile(final String selectedFile) {
		this.selectedFile.set(selectedFile);
	}

	public SimpleStringProperty selectedFileProperty() {
		return selectedFile;
	}

	public CategoryModel getSelectedCategory() {
		return selectedCategory.get();
	}

	public void setSelectedCategory(final CategoryModel selectedCategory) {
		this.selectedCategory.set(selectedCategory);
	}

	public SimpleObjectProperty<CategoryModel> selectedCategoryProperty() {
		return selectedCategory;
	}

	public AccountModel getSelectedAccount() {
		return selectedAccount.get();
	}

	public void setSelectedAccount(final AccountModel selectedAccount) {
		this.selectedAccount.set(selectedAccount);
	}

	public SimpleObjectProperty<AccountModel> selectedAccountProperty() {
		return selectedAccount;
	}

	public ObservableList<Visibility> getVisibilities() {
		return visibilities.get();
	}

	public void setVisibilities(final ObservableList<Visibility> visibilities) {
		this.visibilities.set(visibilities);
	}

	public SimpleListProperty<Visibility> visibilitiesProperty() {
		return visibilities;
	}

	public ObservableList<Comment> getComments() {
		return comments.get();
	}

	public void setComments(final ObservableList<Comment> comments) {
		this.comments.set(comments);
	}

	public SimpleListProperty<Comment> commentsProperty() {
		return comments;
	}

	public ObservableList<ThreeD> getThreeDs() {
		return threeDs.get();
	}

	public void setThreeDs(final ObservableList<ThreeD> threeDs) {
		this.threeDs.set(threeDs);
	}

	public SimpleListProperty<ThreeD> threeDsProperty() {
		return threeDs;
	}

	public ObservableList<License> getLicenses() {
		return licenses.get();
	}

	public void setLicenses(final ObservableList<License> licenses) {
		this.licenses.set(licenses);
	}

	public SimpleListProperty<License> licensesProperty() {
		return licenses;
	}
}
