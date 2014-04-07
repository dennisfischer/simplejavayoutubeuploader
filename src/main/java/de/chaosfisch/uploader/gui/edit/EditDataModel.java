/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.uploader.gui.edit;

import de.chaosfisch.youtube.YouTubeFactory;
import de.chaosfisch.youtube.account.AccountModel;
import de.chaosfisch.youtube.category.CategoryModel;
import de.chaosfisch.youtube.category.ICategoryService;
import de.chaosfisch.youtube.playlist.PlaylistModel;
import de.chaosfisch.youtube.upload.IUploadService;
import de.chaosfisch.youtube.upload.UploadModel;
import de.chaosfisch.youtube.upload.metadata.License;
import de.chaosfisch.youtube.upload.permissions.Comment;
import de.chaosfisch.youtube.upload.permissions.ThreeD;
import de.chaosfisch.youtube.upload.permissions.Visibility;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;

public class EditDataModel {

	private final SimpleBooleanProperty             ageRestriction = new SimpleBooleanProperty(false);
	private final SimpleListProperty<CategoryModel> categories     = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final ICategoryService categoryService;
	private final SimpleListProperty<Comment>         comments           = new SimpleListProperty<>(FXCollections.observableArrayList(Comment.values()));
	private final SimpleStringProperty                description        = new SimpleStringProperty("");
	private final SimpleBooleanProperty               embed              = new SimpleBooleanProperty(true);
	private final SimpleListProperty<String>          files              = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final SimpleListProperty<License>         licenses           = new SimpleListProperty<>(FXCollections.observableArrayList(License.values()));
	private final SimpleBooleanProperty               rate               = new SimpleBooleanProperty(true);
	private final SimpleObjectProperty<AccountModel>  selectedAccount    = new SimpleObjectProperty<>();
	private final SimpleObjectProperty<CategoryModel> selectedCategory   = new SimpleObjectProperty<>();
	private final SimpleObjectProperty<Comment>       selectedComment    = new SimpleObjectProperty<>(Comment.ALLOWED);
	private final SimpleStringProperty                selectedFile       = new SimpleStringProperty();
	private final SimpleObjectProperty<License>       selectedLicense    = new SimpleObjectProperty<>(License.YOUTUBE);
	private final SimpleListProperty<PlaylistModel>   selectedPlaylists  = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final SimpleObjectProperty<ThreeD>        selectedThreeD     = new SimpleObjectProperty<>(ThreeD.DEFAULT);
	private final SimpleObjectProperty<Visibility>    selectedVisibility = new SimpleObjectProperty<>(Visibility.PUBLIC);
	private final SimpleBooleanProperty               statistics         = new SimpleBooleanProperty(true);
	private final SimpleBooleanProperty               subscribers        = new SimpleBooleanProperty(true);
	private final SimpleStringProperty                tags               = new SimpleStringProperty("");
	private final SimpleListProperty<ThreeD>          threeDs            = new SimpleListProperty<>(FXCollections.observableArrayList(ThreeD.values()));
	private final SimpleStringProperty                title              = new SimpleStringProperty("");
	private final IUploadService uploadService;
	private final SimpleListProperty<Visibility> visibilities = new SimpleListProperty<>(FXCollections.observableArrayList(Visibility.values()));

	public EditDataModel(final ICategoryService categoryService, final IUploadService uploadService) {
		this.categoryService = categoryService;
		this.uploadService = uploadService;
		initCategoryData();
		categories.bind(categoryService.categoryModelsProperty());
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

	public ObservableList<CategoryModel> getCategories() {
		return categories.get();
	}

	public void setCategories(final ObservableList<CategoryModel> categories) {
		this.categories.set(categories);
	}

	public SimpleListProperty<CategoryModel> categoriesProperty() {
		return categories;
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

	public String getTitle() {
		return title.get();
	}

	public void setTitle(final String title) {
		this.title.set(title);
	}

	public SimpleStringProperty titleProperty() {
		return title;
	}

	public String getDescription() {
		return description.get();
	}

	public void setDescription(final String description) {
		this.description.set(description);
	}

	public SimpleStringProperty descriptionProperty() {
		return description;
	}

	public String getTags() {
		return tags.get();
	}

	public void setTags(final String tags) {
		this.tags.set(tags);
	}

	public SimpleStringProperty tagsProperty() {
		return tags;
	}

	public Visibility getSelectedVisibility() {
		return selectedVisibility.get();
	}

	public void setSelectedVisibility(final Visibility selectedVisibility) {
		this.selectedVisibility.set(selectedVisibility);
	}

	public SimpleObjectProperty<Visibility> selectedVisibilityProperty() {
		return selectedVisibility;
	}

	public Comment getSelectedComment() {
		return selectedComment.get();
	}

	public void setSelectedComment(final Comment selectedComment) {
		this.selectedComment.set(selectedComment);
	}

	public SimpleObjectProperty<Comment> selectedCommentProperty() {
		return selectedComment;
	}

	public ThreeD getSelectedThreeD() {
		return selectedThreeD.get();
	}

	public void setSelectedThreeD(final ThreeD selectedThreeD) {
		this.selectedThreeD.set(selectedThreeD);
	}

	public SimpleObjectProperty<ThreeD> selectedThreeDProperty() {
		return selectedThreeD;
	}

	public License getSelectedLicense() {
		return selectedLicense.get();
	}

	public void setSelectedLicense(final License selectedLicense) {
		this.selectedLicense.set(selectedLicense);
	}

	public SimpleObjectProperty<License> selectedLicenseProperty() {
		return selectedLicense;
	}

	public boolean getAgeRestriction() {
		return ageRestriction.get();
	}

	public void setAgeRestriction(final boolean ageRestriction) {
		this.ageRestriction.set(ageRestriction);
	}

	public SimpleBooleanProperty ageRestrictionProperty() {
		return ageRestriction;
	}

	public boolean getStatistics() {
		return statistics.get();
	}

	public void setStatistics(final boolean statistics) {
		this.statistics.set(statistics);
	}

	public SimpleBooleanProperty statisticsProperty() {
		return statistics;
	}

	public boolean getRate() {
		return rate.get();
	}

	public void setRate(final boolean rate) {
		this.rate.set(rate);
	}

	public SimpleBooleanProperty rateProperty() {
		return rate;
	}

	public boolean getEmbed() {
		return embed.get();
	}

	public void setEmbed(final boolean embed) {
		this.embed.set(embed);
	}

	public SimpleBooleanProperty embedProperty() {
		return embed;
	}

	public boolean getSubscribers() {
		return subscribers.get();
	}

	public void setSubscribers(final boolean subscribers) {
		this.subscribers.set(subscribers);
	}

	public SimpleBooleanProperty subscribersProperty() {
		return subscribers;
	}

	public void addFile(final File file) {
		final String filePath = file.getAbsolutePath();
		files.add(filePath);
		if (null == selectedFile.get()) {
			selectedFile.set(filePath);
		}
	}

	public void createUpload() {
		final UploadModel uploadModel = new UploadModel();
		uploadModel.setAccount(selectedAccount.get());
		//TODO		uploadModel.setDateTimeOfStart();
		//TODO		uploadModel.setDateTimeOfRelease();
		//TODO		uploadModel.setDateTimeOfEnd();
		//TODO		uploadModel.setEnddir();
		uploadModel.setFile(selectedFile.get());
		uploadModel.setFileSize(new File(selectedFile.get()).length());
		uploadModel.setMetadataTitle(title.get());
		uploadModel.setMetadataDescription(description.get());
		uploadModel.setMetadataTags(tags.get());
		uploadModel.setMetadataLicense(selectedLicense.get());
		uploadModel.setMetadataCategory(selectedCategory.get());
		uploadService.store(uploadModel);
	}
}
