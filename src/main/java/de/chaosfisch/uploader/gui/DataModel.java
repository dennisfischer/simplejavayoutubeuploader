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
import de.chaosfisch.youtube.playlist.IPlaylistService;
import de.chaosfisch.youtube.playlist.PlaylistModel;
import de.chaosfisch.youtube.upload.IUploadService;
import de.chaosfisch.youtube.upload.UploadModel;
import de.chaosfisch.youtube.upload.metadata.License;
import de.chaosfisch.youtube.upload.permissions.Comment;
import de.chaosfisch.youtube.upload.permissions.ThreeD;
import de.chaosfisch.youtube.upload.permissions.Visibility;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.List;

public class DataModel {

	private final SimpleListProperty<UploadModel>     uploads           = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final SimpleListProperty<ProjectModel>    projects          = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final SimpleListProperty<AccountModel>    accounts          = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final SimpleListProperty<CategoryModel>   categories        = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final SimpleListProperty<String>          files             = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final SimpleListProperty<UploadModel>     selectedUploads   = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final SimpleListProperty<PlaylistModel>   selectedPlaylists = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final SimpleListProperty<Visibility>      visibilities      = new SimpleListProperty<>(FXCollections.observableArrayList(Visibility.values()));
	private final SimpleListProperty<Comment>         comments          = new SimpleListProperty<>(FXCollections.observableArrayList(Comment.values()));
	private final SimpleListProperty<ThreeD>          threeDs           = new SimpleListProperty<>(FXCollections.observableArrayList(ThreeD.values()));
	private final SimpleListProperty<License>         licenses          = new SimpleListProperty<>(FXCollections.observableArrayList(License.values()));
	private final SimpleObjectProperty<AccountModel>  selectedAccount   = new SimpleObjectProperty<>();
	private final SimpleObjectProperty<CategoryModel> selectedCategory  = new SimpleObjectProperty<>();
	private final SimpleStringProperty                selectedFile      = new SimpleStringProperty();
	private final SimpleIntegerProperty               maxSpeed          = new SimpleIntegerProperty();
	private final SimpleIntegerProperty               maxUploads        = new SimpleIntegerProperty();
	private final SimpleBooleanProperty               running           = new SimpleBooleanProperty();
	private final ICategoryService categoryService;
	private final IAccountService  accountService;
	private final IUploadService   uploadService;
	private final IPlaylistService playlistService;


	public DataModel(final ICategoryService categoryService, final IAccountService accountService, final IUploadService uploadService, final IPlaylistService playlistService) {
		this.categoryService = categoryService;
		this.accountService = accountService;
		this.uploadService = uploadService;
		this.playlistService = playlistService;
		initBindings();
		initData();
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

	private void initBindings() {
		categories.bind(categoryService.categoryModelsProperty());
		accounts.bind(accountService.accountModelsProperty());
		uploads.bind(uploadService.uploadModelsProperty());
		//	projects.bind(projectService.projectModelsProperty());
	}

	private void initData() {
		initCategoryData();
		initUploadData();
		initPlaylists();
	}

	private void initPlaylists() {
		try {
			playlistService.refresh();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void initUploadData() {
		maxUploads.bindBidirectional(uploadService.maxUploadsProperty());
		maxSpeed.bindBidirectional(uploadService.maxSpeedProperty());
		running.bind(uploadService.runningProperty());
	}

	public boolean getRunning() {
		return running.get();
	}

	public SimpleBooleanProperty runningProperty() {
		return running;
	}

	private void initCategoryData() {
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

	public void remove(final AccountModel account) {
		accountService.remove(account);
	}

	public ObservableList<PlaylistModel> getPlaylists(final AccountModel account) {
		if (null == account) {
			return FXCollections.observableArrayList();
		}
		return playlistService.playlistModelsProperty(account);
	}
}
