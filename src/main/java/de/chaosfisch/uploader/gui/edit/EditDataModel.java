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
import de.chaosfisch.youtube.upload.permission.Asset;
import de.chaosfisch.youtube.upload.permission.Comment;
import de.chaosfisch.youtube.upload.permission.ThreeD;
import de.chaosfisch.youtube.upload.permission.Visibility;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;

public class EditDataModel {

	//Services
	private final ICategoryService categoryService;
	private final IUploadService   uploadService;


	//Upload fields
	private final SimpleListProperty<CategoryModel>   categories        = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final SimpleStringProperty                description       = new SimpleStringProperty("");
	private final SimpleListProperty<String>          files             = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final SimpleListProperty<License>         licenses          = new SimpleListProperty<>(FXCollections.observableArrayList(License.values()));
	private final SimpleObjectProperty<AccountModel>  selectedAccount   = new SimpleObjectProperty<>();
	private final SimpleObjectProperty<CategoryModel> selectedCategory  = new SimpleObjectProperty<>();
	private final SimpleStringProperty                selectedFile      = new SimpleStringProperty();
	private final SimpleObjectProperty<License>       selectedLicense   = new SimpleObjectProperty<>(License.YOUTUBE);
	private final SimpleListProperty<PlaylistModel>   selectedPlaylists = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final SimpleStringProperty                tags              = new SimpleStringProperty("");
	private final SimpleStringProperty                title             = new SimpleStringProperty("");
	private final SimpleBooleanProperty               subscribers       = new SimpleBooleanProperty(true);

	//Permission fields
	private final SimpleListProperty<Comment>      comments           = new SimpleListProperty<>(FXCollections.observableArrayList(Comment.values()));
	private final SimpleListProperty<Visibility>   visibilities       = new SimpleListProperty<>(FXCollections.observableArrayList(Visibility.values()));
	private final SimpleListProperty<ThreeD>       threeDs            = new SimpleListProperty<>(FXCollections.observableArrayList(ThreeD.values()));
	private final SimpleBooleanProperty            ageRestricted      = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty            commentvote        = new SimpleBooleanProperty(true);
	private final SimpleObjectProperty<Comment>    selectedComment    = new SimpleObjectProperty<>(Comment.ALLOWED);
	private final SimpleObjectProperty<ThreeD>     selectedThreeD     = new SimpleObjectProperty<>(ThreeD.DEFAULT);
	private final SimpleBooleanProperty            statistics         = new SimpleBooleanProperty(true);
	private final SimpleBooleanProperty            rate               = new SimpleBooleanProperty(true);
	private final SimpleBooleanProperty            embed              = new SimpleBooleanProperty(true);
	private final SimpleObjectProperty<Visibility> selectedVisibility = new SimpleObjectProperty<>(Visibility.PUBLIC);

	//Monetization fields
	private final SimpleBooleanProperty claim                    = new SimpleBooleanProperty(false);
	private final SimpleStringProperty  customId                 = new SimpleStringProperty("");
	private final SimpleStringProperty  monetizationDescription  = new SimpleStringProperty("");
	private final SimpleStringProperty  episodeNb                = new SimpleStringProperty("");
	private final SimpleStringProperty  monetizationEpisodeTitle = new SimpleStringProperty("");
	private final SimpleBooleanProperty instream                 = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty instreamDefaults         = new SimpleBooleanProperty(false);
	private final SimpleStringProperty  isan                     = new SimpleStringProperty("");
	private final SimpleStringProperty  notes                    = new SimpleStringProperty("");
	private final SimpleBooleanProperty overlay                  = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty partner                  = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty product                  = new SimpleBooleanProperty(false);
	private final SimpleStringProperty  seasonNb                 = new SimpleStringProperty("");
	private final SimpleStringProperty  monetizationTitle        = new SimpleStringProperty("");
	private final SimpleStringProperty  tmsid                    = new SimpleStringProperty("");
	private final SimpleBooleanProperty trueview                 = new SimpleBooleanProperty(false);
	private final SimpleStringProperty  eidr                     = new SimpleStringProperty("");

	//Social fields
	private final SimpleStringProperty  socialMessage = new SimpleStringProperty("");
	private final SimpleBooleanProperty facebook      = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty twitter       = new SimpleBooleanProperty(false);
	private final SimpleBooleanProperty gplus         = new SimpleBooleanProperty(false);

	public EditDataModel(final ICategoryService categoryService, final IUploadService uploadService) {
		this.categoryService = categoryService;
		this.uploadService = uploadService;
		initCategoryData();
		categories.bind(categoryService.categoryModelsProperty());
	}

	public boolean getGplus() {
		return gplus.get();
	}

	public void setGplus(final boolean gplus) {
		this.gplus.set(gplus);
	}

	public SimpleBooleanProperty gplusProperty() {
		return gplus;
	}

	public boolean getCommentvote() {
		return commentvote.get();
	}

	public void setCommentvote(final boolean commentvote) {
		this.commentvote.set(commentvote);
	}

	public SimpleBooleanProperty commentvoteProperty() {
		return commentvote;
	}

	public boolean getClaim() {
		return claim.get();
	}

	public void setClaim(final boolean claim) {
		this.claim.set(claim);
	}

	public SimpleBooleanProperty claimProperty() {
		return claim;
	}

	public String getCustomId() {
		return customId.get();
	}

	public void setCustomId(final String customId) {
		this.customId.set(customId);
	}

	public SimpleStringProperty customIdProperty() {
		return customId;
	}

	public String getMonetizationDescription() {
		return monetizationDescription.get();
	}

	public void setMonetizationDescription(final String monetizationDescription) {
		this.monetizationDescription.set(monetizationDescription);
	}

	public SimpleStringProperty monetizationDescriptionProperty() {
		return monetizationDescription;
	}

	public String getEpisodeNb() {
		return episodeNb.get();
	}

	public void setEpisodeNb(final String episodeNb) {
		this.episodeNb.set(episodeNb);
	}

	public SimpleStringProperty episodeNbProperty() {
		return episodeNb;
	}

	public String getMonetizationEpisodeTitle() {
		return monetizationEpisodeTitle.get();
	}

	public void setMonetizationEpisodeTitle(final String monetizationEpisodeTitle) {
		this.monetizationEpisodeTitle.set(monetizationEpisodeTitle);
	}

	public SimpleStringProperty monetizationEpisodeTitleProperty() {
		return monetizationEpisodeTitle;
	}

	public boolean getInstream() {
		return instream.get();
	}

	public void setInstream(final boolean instream) {
		this.instream.set(instream);
	}

	public SimpleBooleanProperty instreamProperty() {
		return instream;
	}

	public boolean getInstreamDefaults() {
		return instreamDefaults.get();
	}

	public void setInstreamDefaults(final boolean instreamDefaults) {
		this.instreamDefaults.set(instreamDefaults);
	}

	public SimpleBooleanProperty instreamDefaultsProperty() {
		return instreamDefaults;
	}

	public String getIsan() {
		return isan.get();
	}

	public void setIsan(final String isan) {
		this.isan.set(isan);
	}

	public SimpleStringProperty isanProperty() {
		return isan;
	}

	public String getNotes() {
		return notes.get();
	}

	public void setNotes(final String notes) {
		this.notes.set(notes);
	}

	public SimpleStringProperty notesProperty() {
		return notes;
	}

	public boolean getOverlay() {
		return overlay.get();
	}

	public void setOverlay(final boolean overlay) {
		this.overlay.set(overlay);
	}

	public SimpleBooleanProperty overlayProperty() {
		return overlay;
	}

	public boolean getPartner() {
		return partner.get();
	}

	public void setPartner(final boolean partner) {
		this.partner.set(partner);
	}

	public SimpleBooleanProperty partnerProperty() {
		return partner;
	}

	public boolean getProduct() {
		return product.get();
	}

	public void setProduct(final boolean product) {
		this.product.set(product);
	}

	public SimpleBooleanProperty productProperty() {
		return product;
	}

	public String getSeasonNb() {
		return seasonNb.get();
	}

	public void setSeasonNb(final String seasonNb) {
		this.seasonNb.set(seasonNb);
	}

	public SimpleStringProperty seasonNbProperty() {
		return seasonNb;
	}

	public String getMonetizationTitle() {
		return monetizationTitle.get();
	}

	public void setMonetizationTitle(final String monetizationTitle) {
		this.monetizationTitle.set(monetizationTitle);
	}

	public SimpleStringProperty monetizationTitleProperty() {
		return monetizationTitle;
	}

	public String getTmsid() {
		return tmsid.get();
	}

	public void setTmsid(final String tmsid) {
		this.tmsid.set(tmsid);
	}

	public SimpleStringProperty tmsidProperty() {
		return tmsid;
	}

	public boolean getTrueview() {
		return trueview.get();
	}

	public void setTrueview(final boolean trueview) {
		this.trueview.set(trueview);
	}

	public SimpleBooleanProperty trueviewProperty() {
		return trueview;
	}

	public String getEidr() {
		return eidr.get();
	}

	public void setEidr(final String eidr) {
		this.eidr.set(eidr);
	}

	public SimpleStringProperty eidrProperty() {
		return eidr;
	}

	public String getSocialMessage() {
		return socialMessage.get();
	}

	public void setSocialMessage(final String socialMessage) {
		this.socialMessage.set(socialMessage);
	}

	public SimpleStringProperty socialMessageProperty() {
		return socialMessage;
	}

	public boolean getFacebook() {
		return facebook.get();
	}

	public void setFacebook(final boolean facebook) {
		this.facebook.set(facebook);
	}

	public SimpleBooleanProperty facebookProperty() {
		return facebook;
	}

	public boolean getTwitter() {
		return twitter.get();
	}

	public void setTwitter(final boolean twitter) {
		this.twitter.set(twitter);
	}

	public SimpleBooleanProperty twitterProperty() {
		return twitter;
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

	public boolean getAgeRestricted() {
		return ageRestricted.get();
	}

	public void setAgeRestricted(final boolean ageRestricted) {
		this.ageRestricted.set(ageRestricted);
	}

	public SimpleBooleanProperty ageRestrictedProperty() {
		return ageRestricted;
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

		//Metadata
		uploadModel.setMetadataTitle(title.get());
		uploadModel.setMetadataDescription(description.get());
		uploadModel.setMetadataTags(tags.get());
		uploadModel.setMetadataLicense(selectedLicense.get());
		uploadModel.setMetadataCategory(selectedCategory.get());

		//Permission
		uploadModel.setPermissionAgeRestricted(ageRestricted.get());
		uploadModel.setPermissionComment(selectedComment.get());
		uploadModel.setPermissionCommentvote(commentvote.get());
		uploadModel.setPermissionEmbed(embed.get());
		uploadModel.setPermissionPublicStatsViewable(statistics.get());
		uploadModel.setPermissionRate(rate.get());
		uploadModel.setPermissionThreeD(selectedThreeD.get());
		uploadModel.setPermissionVisibility(selectedVisibility.get());
		uploadModel.setPermissionSubcribers(subscribers.get());

		//Social
		uploadModel.setSocialMessage(socialMessage.get());
		uploadModel.setSocialFacebook(facebook.get());
		uploadModel.setSocialTwitter(twitter.get());
		uploadModel.setSocialGplus(gplus.get());

		//Monetization
		//TODO monetization partner fields connection
		//	uploadModel.setMonetizationAsset(selectedAsset.get());
		uploadModel.setMonetizationAsset(Asset.TV);
		uploadModel.setMonetizationClaim(claim.get());
		//	uploadModel.setMonetizationClaimOption(selectedClaimOption.get());
		//	uploadModel.setMonetizationClaimType(selectedClaimType.get());
		uploadModel.setMonetizationCustomId(customId.get());
		uploadModel.setMonetizationDescription(monetizationDescription.get());
		uploadModel.setMonetizationEidr(eidr.get());
		uploadModel.setMonetizationEpisodeNb(episodeNb.get());
		uploadModel.setMonetizationEpisodeTitle(monetizationEpisodeTitle.get());
		uploadModel.setMonetizationInstream(instream.get());
		uploadModel.setMonetizationInstreamDefaults(instreamDefaults.get());
		uploadModel.setMonetizationIsan(isan.get());
		uploadModel.setMonetizationNotes(notes.get());
		uploadModel.setMonetizationOverlay(overlay.get());
		uploadModel.setMonetizationPartner(partner.get());
		uploadModel.setMonetizationProduct(product.get());
		uploadModel.setMonetizationSeasonNb(seasonNb.get());
		//	uploadModel.setMonetizationSyndication(selectedSyndication.get());
		uploadModel.setMonetizationTitle(monetizationTitle.get());
		uploadModel.setMonetizationTmsid(tmsid.get());
		uploadModel.setMonetizationTrueview(trueview.get());
		uploadService.store(uploadModel);
	}
}
