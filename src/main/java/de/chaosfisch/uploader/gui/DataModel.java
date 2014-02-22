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

import de.chaosfisch.google.account.AccountModel;
import de.chaosfisch.google.category.CategoryModel;
import de.chaosfisch.google.category.ICategoryService;
import de.chaosfisch.google.playlist.PlaylistModel;
import de.chaosfisch.google.upload.Status;
import de.chaosfisch.google.upload.metadata.License;
import de.chaosfisch.google.upload.permissions.Comment;
import de.chaosfisch.google.upload.permissions.ThreeD;
import de.chaosfisch.google.upload.permissions.Visibility;
import de.chaosfisch.uploader.gui.models.UploadModel;
import de.chaosfisch.uploader.project.ProjectModel;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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
	private final SimpleListProperty<Visibility>    visibilities      = new SimpleListProperty<>(FXCollections.observableArrayList(Visibility
																																		   .values()));
	private final SimpleListProperty<Comment>       comments          = new SimpleListProperty<>(FXCollections.observableArrayList(Comment.values()));
	private final SimpleListProperty<ThreeD>        threeDs           = new SimpleListProperty<>(FXCollections.observableArrayList(ThreeD.values()));
	private final SimpleListProperty<License>       licenses          = new SimpleListProperty<>(FXCollections.observableArrayList(License.values()));

	private final SimpleObjectProperty<AccountModel>  selectedAccount  = new SimpleObjectProperty<>();
	private final SimpleObjectProperty<CategoryModel> selectedCategory = new SimpleObjectProperty<>();
	private final SimpleStringProperty                selectedFile     = new SimpleStringProperty();
	private final ICategoryService categoryService;
//	private final IPlaylistService playlistService;


	public DataModel(final ICategoryService categoryService) {
		this.categoryService = categoryService;
		initSampleData();
		initBindings();
		initData();
	}

	private void initSampleData() {
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

		final AccountModel accountModel = new AccountModel();
		accountModel.setName("Account 1");
		final ObservableList<PlaylistModel> playlists = FXCollections.observableArrayList();
		final PlaylistModel model1 = new PlaylistModel();
		model1.setTitle("Playlist 1");
		final PlaylistModel model2 = new PlaylistModel();
		model2.setTitle("Playlist 2");
		final PlaylistModel model3 = new PlaylistModel();
		model3.setTitle("Playlist 3");
		final PlaylistModel model4 = new PlaylistModel();
		model4.setTitle("Playlist 4");
		final PlaylistModel model5 = new PlaylistModel();
		model5.setTitle("Playlist 5");
		final PlaylistModel model6 = new PlaylistModel();
		model6.setTitle("Playlist 6");
		final PlaylistModel model7 = new PlaylistModel();
		model7.setTitle("Playlist 7");
		final PlaylistModel model8 = new PlaylistModel();
		model8.setTitle("Playlist 8");
		final PlaylistModel model9 = new PlaylistModel();
		model9.setTitle("Playlist 9");
		final PlaylistModel model10 = new PlaylistModel();
		model10.setTitle("Playlist 10");
		final PlaylistModel model11 = new PlaylistModel();
		model11.setTitle("Playlist 11");
		playlists.addAll(model1);
		playlists.addAll(model2);
		playlists.addAll(model3);
		playlists.addAll(model4);
		playlists.addAll(model5);
		playlists.addAll(model6);
		playlists.addAll(model7);
		playlists.addAll(model8);

		accountModel.setPlaylists(playlists);
		accounts.addAll(accountModel);
	}

	private void initData() {
		final Thread categoryThread = new Thread(categoryService::refresh, "Category_Loader");
		categoryThread.setDaemon(true);
		categoryThread.start();
	}

	private void initBindings() {
		categories.bind(categoryService.categoryModelsProperty());
		//	projects.bind(projectService.projectModelsProperty());
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
