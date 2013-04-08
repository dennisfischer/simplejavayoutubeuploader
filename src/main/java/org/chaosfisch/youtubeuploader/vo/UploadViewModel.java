/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.vo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Toggle;
import javafx.util.converter.DefaultStringConverter;

import org.chaosfisch.google.atom.AtomCategory;
import org.chaosfisch.util.ExtendedPlaceholders;
import org.chaosfisch.youtubeuploader.db.dao.AccountDao;
import org.chaosfisch.youtubeuploader.db.dao.PlaylistDao;
import org.chaosfisch.youtubeuploader.db.dao.TemplateDao;
import org.chaosfisch.youtubeuploader.db.dao.TemplatePlaylistDao;
import org.chaosfisch.youtubeuploader.db.dao.UploadDao;
import org.chaosfisch.youtubeuploader.db.dao.UploadPlaylistDao;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Account;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.TemplatePlaylist;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;
import org.chaosfisch.youtubeuploader.models.events.ModelAddedEvent;
import org.chaosfisch.youtubeuploader.models.events.ModelRemovedEvent;
import org.chaosfisch.youtubeuploader.services.PlaylistService;
import org.jooq.impl.Executor;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class UploadViewModel {

	// {{ UploadOptions
	public final SimpleListProperty<Account>						accountProperty					= new SimpleListProperty<>(FXCollections.<Account> observableArrayList());
	public final SimpleListProperty<Template>						templateProperty				= new SimpleListProperty<>(FXCollections.<Template> observableArrayList());
	public final SimpleListProperty<Playlist>						playlistDropListProperty		= new SimpleListProperty<>(FXCollections.<Playlist> observableArrayList());
	public final SimpleListProperty<Playlist>						playlistSourceListProperty		= new SimpleListProperty<>(FXCollections.<Playlist> observableArrayList());
	public final SimpleListProperty<AtomCategory>					categoryProperty				= new SimpleListProperty<>(FXCollections.<AtomCategory> observableArrayList());
	public final SimpleListProperty<String>							commentProperty					= new SimpleListProperty<>(FXCollections.<String> observableArrayList());
	public final SimpleListProperty<File>							fileProperty					= new SimpleListProperty<>(FXCollections.<File> observableArrayList());
	public final SimpleListProperty<String>							licenseProperty					= new SimpleListProperty<>(FXCollections.<String> observableArrayList());
	public final SimpleListProperty<String>							videoresponseProperty			= new SimpleListProperty<>(FXCollections.<String> observableArrayList());
	public final SimpleListProperty<String>							visibilityProperty				= new SimpleListProperty<>(FXCollections.<String> observableArrayList());
	public final SimpleStringProperty								previewTitleProperty			= new SimpleStringProperty();
	public final SimpleBooleanProperty								commentVoteProperty				= new SimpleBooleanProperty();
	public final SimpleStringProperty								defaultdirProperty				= new SimpleStringProperty();
	public final SimpleStringProperty								descriptionProperty				= new SimpleStringProperty();
	public final SimpleBooleanProperty								embedProperty					= new SimpleBooleanProperty();
	public final SimpleStringProperty								enddirProperty					= new SimpleStringProperty();
	public final SimpleBooleanProperty								mobileProperty					= new SimpleBooleanProperty();
	public final SimpleBooleanProperty								rateProperty					= new SimpleBooleanProperty();
	public final SimpleStringProperty								tagsProperty					= new SimpleStringProperty();
	public final SimpleStringProperty								titleProperty					= new SimpleStringProperty();
	public final SimpleBooleanProperty								facebookProperty				= new SimpleBooleanProperty();
	public final SimpleBooleanProperty								twitterProperty					= new SimpleBooleanProperty();
	public final SimpleStringProperty								messageProperty					= new SimpleStringProperty();

	public final SimpleObjectProperty<Calendar>						starttimeProperty				= new SimpleObjectProperty<>(Calendar.getInstance());
	public final SimpleObjectProperty<Calendar>						releasetimeProperty				= new SimpleObjectProperty<>(Calendar.getInstance());
	public final SimpleIntegerProperty								numberProperty					= new SimpleIntegerProperty();
	public final SimpleObjectProperty<File>							initialDirectoryProperty		= new SimpleObjectProperty<>();
	public final SimpleStringProperty								thumbnailProperty				= new SimpleStringProperty();
	public final SimpleIntegerProperty								idProperty						= new SimpleIntegerProperty(-1);

	public SimpleObjectProperty<SingleSelectionModel<AtomCategory>>	selectedCategoryProperty;
	public SimpleObjectProperty<SingleSelectionModel<File>>			selectedFileProperty;
	public SimpleObjectProperty<SingleSelectionModel<Account>>		selectedAccountProperty;
	public SimpleObjectProperty<SingleSelectionModel<String>>		selectedCommentProperty;
	public SimpleObjectProperty<SingleSelectionModel<String>>		selectedLicenseProperty;
	public SimpleObjectProperty<SingleSelectionModel<String>>		selectedVideoResponseProperty;
	public SimpleObjectProperty<SingleSelectionModel<String>>		selectedVisibilityProperty;
	public SimpleObjectProperty<SingleSelectionModel<Template>>		selectedTemplateProperty;
	// }} UploadOptions
	// {{ MonetizeOptions
	public SimpleBooleanProperty									claimProperty					= new SimpleBooleanProperty(false);
	public SimpleBooleanProperty									overlayProperty					= new SimpleBooleanProperty(false);
	public SimpleBooleanProperty									trueViewProperty				= new SimpleBooleanProperty(false);
	public SimpleBooleanProperty									inStreamProperty				= new SimpleBooleanProperty(false);
	public SimpleBooleanProperty									inStreamDefaultsProperty		= new SimpleBooleanProperty(false);
	public SimpleBooleanProperty									productPlacementProperty		= new SimpleBooleanProperty(false);
	public SimpleBooleanProperty									partnerProperty					= new SimpleBooleanProperty(false);

	public SimpleListProperty<String>								claimTypeProperty				= new SimpleListProperty<>(FXCollections.<String> observableArrayList());
	public SimpleListProperty<String>								claimOptionsProperty			= new SimpleListProperty<>(FXCollections.<String> observableArrayList());

	public SimpleStringProperty										tmsidProperty					= new SimpleStringProperty();
	public SimpleStringProperty										isanProperty					= new SimpleStringProperty();
	public SimpleStringProperty										eidrProperty					= new SimpleStringProperty();
	public SimpleStringProperty										customidProperty				= new SimpleStringProperty();
	public SimpleStringProperty										numberSeasonProperty			= new SimpleStringProperty();
	public SimpleStringProperty										numberEpisodeProperty			= new SimpleStringProperty();
	public SimpleStringProperty										monetizeTitleEpisodeProperty	= new SimpleStringProperty();
	public SimpleStringProperty										monetizeTitleProperty			= new SimpleStringProperty();
	public SimpleStringProperty										monetizeNotesProperty			= new SimpleStringProperty();
	public SimpleStringProperty										monetizeDescriptionProperty		= new SimpleStringProperty();

	private SimpleObjectProperty<SingleSelectionModel<String>>		selectedClaimTypeProperty;
	private SimpleObjectProperty<SingleSelectionModel<String>>		selectedClaimOptionProperty;
	private SimpleObjectProperty<ReadOnlyObjectProperty<Toggle>>	selectedAssetTypeProperty;
	private SimpleObjectProperty<ReadOnlyObjectProperty<Toggle>>	selectedContentSyndicationProperty;
	// }} MonetizeOptions

	@Inject
	private PlaylistService											playlistService;

	@Inject
	private AccountDao												accountDao;
	@Inject
	private TemplateDao												templateDao;
	@Inject
	private PlaylistDao												playlistDao;
	@Inject
	private UploadDao												uploadDao;
	@Inject
	private TemplatePlaylistDao										templatePlaylistDao;
	@Inject
	private UploadPlaylistDao										uploadPlaylistDao;
	@Inject
	private Executor												exec;

	@Inject
	@Named("i18n-resources")
	private ResourceBundle											resources;

	public void init(final SingleSelectionModel<AtomCategory> categorySelectionModel, final SingleSelectionModel<File> fileSelectionModel,
			final SingleSelectionModel<Account> AccountelectionModel, final SingleSelectionModel<String> commentSelectionModel,
			final SingleSelectionModel<String> licenseSelectionModel, final SingleSelectionModel<String> videoresponseSelectionModel,
			final SingleSelectionModel<String> visibilitySelectionModel, final SingleSelectionModel<Template> TemplateelectionModel,
			final SingleSelectionModel<String> claimTypeSelectionModel, final SingleSelectionModel<String> claimOptionSelectionModel,
			final ReadOnlyObjectProperty<Toggle> selectedAssetTypeModel, final ReadOnlyObjectProperty<Toggle> contentSyndicationModel) {
		selectedCategoryProperty = new SimpleObjectProperty<>(categorySelectionModel);
		selectedFileProperty = new SimpleObjectProperty<>(fileSelectionModel);
		selectedAccountProperty = new SimpleObjectProperty<>(AccountelectionModel);
		selectedCommentProperty = new SimpleObjectProperty<>(commentSelectionModel);
		selectedLicenseProperty = new SimpleObjectProperty<>(licenseSelectionModel);
		selectedVideoResponseProperty = new SimpleObjectProperty<>(videoresponseSelectionModel);
		selectedVisibilityProperty = new SimpleObjectProperty<>(visibilitySelectionModel);
		selectedTemplateProperty = new SimpleObjectProperty<>(TemplateelectionModel);
		selectedClaimTypeProperty = new SimpleObjectProperty<>(claimTypeSelectionModel);
		selectedClaimOptionProperty = new SimpleObjectProperty<>(claimOptionSelectionModel);
		selectedAssetTypeProperty = new SimpleObjectProperty<>(selectedAssetTypeModel);
		selectedContentSyndicationProperty = new SimpleObjectProperty<>(contentSyndicationModel);

		final InvalidationListener previewTitleChangeListener = new InvalidationListener() {

			@Override
			public void invalidated(final Observable observable) {
				final String value = titleProperty.get();
				titleProperty.set("");
				titleProperty.set(value);
			}
		};
		numberProperty.addListener(previewTitleChangeListener);
		selectedFileProperty.addListener(previewTitleChangeListener);
		previewTitleProperty.bindBidirectional(titleProperty, new DefaultStringConverter() {

			final ExtendedPlaceholders	extendedPlaceholders	= new ExtendedPlaceholders();

			@Override
			public String toString(final String value) {
				extendedPlaceholders.setFile(selectedFileProperty.get()
					.getSelectedItem() != null ? selectedFileProperty.get()
					.getSelectedItem()
					.getAbsolutePath() : "{file-missing}");
				extendedPlaceholders.setNumber(numberProperty.get());

				return extendedPlaceholders.replace(value);
			}
		});

		selectedTemplateProperty.get()
			.selectedItemProperty()
			.addListener(new InvalidationListener() {

				@Override
				public void invalidated(final Observable arg0) {
					// resetTemplate();
				}
			});

		selectedAccountProperty.get()
			.selectedItemProperty()
			.addListener(new ChangeListener<Account>() {

				@Override
				public void changed(final ObservableValue<? extends Account> observable, final Account oldValue, final Account newValue) {
					if (newValue != null) {
						playlistSourceListProperty.clear();
						playlistSourceListProperty.addAll(playlistDao.fetchByAccountId(newValue.getId()));
					}
				}
			});
	}

	private void _reset(final Template template) {

		final Account account = accountDao.fetchOneById(template.getAccountId());
		if (account != null) {
			selectedAccountProperty.get()
				.select(account);
		}

		final Iterator<Playlist> playlistDropListIterator = playlistDropListProperty.get()
			.iterator();
		while (playlistDropListIterator.hasNext()) {
			final Playlist playlist = playlistDropListIterator.next();
			playlistSourceListProperty.add(playlist);
			playlistDropListIterator.remove();
		}

		final Iterator<Playlist> playlistSourceListIterator = playlistDao.fetchByTemplate(template)
			.iterator();
		while (playlistSourceListIterator.hasNext()) {
			final Playlist playlist = playlistSourceListIterator.next();
			playlistDropListProperty.add(playlist);
			playlistSourceListProperty.remove(playlist);
		}

		final File defaultDir = new File(template.getDefaultdir());
		if (defaultDir.exists() && defaultDir.isDirectory()) {
			initialDirectoryProperty.set(defaultDir);
		}

		releasetimeProperty.set(Calendar.getInstance());
		starttimeProperty.set(Calendar.getInstance());
		thumbnailProperty.set("");
		idProperty.setValue(-1);

		selectedAssetTypeProperty.get()
			.get()
			.getToggleGroup()
			.selectToggle(selectedAssetTypeProperty.get()
				.get()
				.getToggleGroup()
				.getToggles()
				.get(template.getMonetizeasset()
					.intValue()));
		selectedContentSyndicationProperty.get()
			.get()
			.getToggleGroup()
			.selectToggle(selectedContentSyndicationProperty.get()
				.get()
				.getToggleGroup()
				.getToggles()
				.get(template.getSyndication()
					.intValue()));
	}

	public Upload toUpload() {
		Upload upload = new Upload();

		upload.setFacebook(false);
		upload.setTwitter(false);
		upload.setArchived(false);
		upload.setFailed(false);
		upload.setInprogress(false);
		upload.setLocked(false);
		upload.setPauseonfinish(false);

		upload.setFile(selectedFileProperty.get()
			.getSelectedItem()
			.getAbsolutePath());
		upload.setAccountId(selectedAccountProperty.get()
			.getSelectedItem()
			.getId());

		String tmpType = null;
		try {
			if (selectedFileProperty.get()
				.getSelectedItem() != null && selectedFileProperty.get()
				.getSelectedItem()
				.isFile()) {
				tmpType = Files.probeContentType(Paths.get(selectedFileProperty.get()
					.getSelectedItem()
					.getAbsolutePath()));
			}

		} catch (final IOException e) {}
		upload.setMimetype(tmpType != null ? tmpType : "application/octet-stream");

		if (idProperty.get() != -1) {
			upload.setId(idProperty.get());
		}

		if (starttimeProperty.get() != null && starttimeProperty.get()
			.getTimeInMillis() > System.currentTimeMillis()) {
			final GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTimeInMillis(starttimeProperty.get()
				.getTimeInMillis());
			upload.setDateOfStart(calendar);
		}

		if (releasetimeProperty.get() != null && releasetimeProperty.get()
			.getTimeInMillis() > System.currentTimeMillis()) {
			final GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(releasetimeProperty.get()
				.getTime());
			final int unroundedMinutes = calendar.get(Calendar.MINUTE);
			final int mod = unroundedMinutes % 30;
			calendar.add(Calendar.MINUTE, mod < 16 ? -mod : 30 - mod);
			upload.setDateOfRelease(calendar);

			if ((facebookProperty.get() || twitterProperty.get()) && messageProperty.get() != null && !messageProperty.get()
				.isEmpty()) {
				upload.setFacebook(facebookProperty.get());
				upload.setTwitter(twitterProperty.get());
				upload.setMessage(messageProperty.get());
			}
		}

		upload.setClaim(overlayProperty.get() || trueViewProperty.get() || inStreamProperty.get() || productPlacementProperty.get()
				|| claimProperty.get());

		upload.setSyndication(selectedContentSyndicationProperty.get()
			.get()
			.getToggleGroup()
			.getToggles()
			.indexOf(selectedContentSyndicationProperty.get()
				.get()));
		upload.setMonetizepartner(claimProperty.get());

		upload.setMonetizeasset(selectedAssetTypeProperty.get()
			.get()
			.getToggleGroup()
			.getToggles()
			.indexOf(selectedAssetTypeProperty.get()
				.get()));

		upload = uploadDao.insertReturning(upload);
		/*
		 * TODO
		 * if (upload.isValid()) {
		 * fileProperty.remove(selectedFileProperty.get().getSelectedItem());
		 * selectedFileProperty.get().selectNext();
		 * idProperty.setValue(-1);
		 * upload = uploadDao.insertReturning(upload);
		 * for (final Playlist playlist : playlistDropListProperty.get()) {
		 * final UploadPlaylist relation = new UploadPlaylist();
		 * relation.setPlaylistId(playlist.getId());
		 * relation.setUploadId(upload.getId());
		 * uploadPlaylistDao.insert(relation);
		 * }
		 * }
		 */

		return upload;
	}

	public void fromUpload(final Upload upload) {
		final File file = new File(upload.getFile());
		idProperty.set(upload.getId());
		if (!fileProperty.contains(file)) {
			fileProperty.add(file);
		}
		selectedFileProperty.get()
			.select(file);
		selectedAccountProperty.get()
			.select(accountDao.fetchOneById(upload.getId()));

		final Iterator<Playlist> playlistDropListIterator = playlistDropListProperty.get()
			.iterator();
		while (playlistDropListIterator.hasNext()) {
			final Playlist playlist = playlistDropListIterator.next();
			playlistSourceListProperty.add(playlist);
			playlistDropListIterator.remove();
		}

		final Iterator<Playlist> PlaylistourceListIterator = playlistDao.fetchByUpload(upload)
			.iterator();
		while (PlaylistourceListIterator.hasNext()) {
			final Playlist playlist = PlaylistourceListIterator.next();
			playlistDropListProperty.add(playlist);
			playlistSourceListProperty.remove(playlist);
		}

		selectedAssetTypeProperty.get()
			.get()
			.getToggleGroup()
			.selectToggle(selectedAssetTypeProperty.get()
				.get()
				.getToggleGroup()
				.getToggles()
				.get(upload.getMonetizeasset()
					.intValue()));
		selectedContentSyndicationProperty.get()
			.get()
			.getToggleGroup()
			.selectToggle(selectedContentSyndicationProperty.get()
				.get()
				.getToggleGroup()
				.getToggles()
				.get(upload.getSyndication()));
	}

	public void saveTemplate() {
		final Template template = selectedTemplateProperty.get()
			.getSelectedItem();

		if (template == null) {
			return;
		}

		if (selectedAccountProperty.get() != null) {
			template.setAccountId(selectedAccountProperty.get()
				.getSelectedItem()
				.getId());
		}

		// Clear all existing template playlist relations
		templatePlaylistDao.delete(templatePlaylistDao.fetchByTemplateId(template.getId()));
		for (final Playlist playlist : playlistDropListProperty.get()) {
			final TemplatePlaylist relation = new TemplatePlaylist();
			relation.setPlaylistId(playlist.getId());
			relation.setTemplateId(template.getId());
			templatePlaylistDao.insert(relation);
		}

		template.setClaim(overlayProperty.getValue() || trueViewProperty.getValue() || inStreamProperty.getValue()
				|| productPlacementProperty.getValue() || claimProperty.get());

		template.setSyndication(selectedContentSyndicationProperty.get()
			.get()
			.getToggleGroup()
			.getToggles()
			.indexOf(selectedContentSyndicationProperty.get()
				.get()));

		template.setMonetizeasset(selectedAssetTypeProperty.get()
			.get()
			.getToggleGroup()
			.getToggles()
			.indexOf(selectedAssetTypeProperty.get()
				.get()));

		templateDao.update(template);
	}

	@Subscribe
	public void onModelAdded(final ModelAddedEvent modelPostSavedEvent) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				if (modelPostSavedEvent.getModel() instanceof Account) {
					if (accountProperty.contains(modelPostSavedEvent.getModel())) {
						accountProperty.set(accountProperty.indexOf(modelPostSavedEvent.getModel()),
							(Account) modelPostSavedEvent.getModel());
					} else {
						accountProperty.add((Account) modelPostSavedEvent.getModel());
						if (selectedAccountProperty.get()
							.getSelectedItem() == null && accountProperty.size() > 0) {
							selectedAccountProperty.get()
								.select(accountProperty.get(0));
						}
					}
				} else if (modelPostSavedEvent.getModel() instanceof Template) {
					if (templateProperty.contains(modelPostSavedEvent.getModel())) {
						templateProperty.set(templateProperty.indexOf(modelPostSavedEvent.getModel()),
							(Template) modelPostSavedEvent.getModel());
						selectedTemplateProperty.get()
							.select((Template) modelPostSavedEvent.getModel());
					} else {
						templateProperty.add((Template) modelPostSavedEvent.getModel());
						if (selectedTemplateProperty.get()
							.getSelectedItem() == null && templateProperty.size() > 0) {
							selectedTemplateProperty.get()
								.select(templateProperty.get(0));
						}
					}
				} else if (modelPostSavedEvent.getModel() instanceof Playlist
						&& playlistDao.fetchOneAccountByPlaylist((Playlist) modelPostSavedEvent.getModel())
							.equals(selectedAccountProperty.get()
								.getSelectedItem())) {
					if (playlistSourceListProperty.contains(modelPostSavedEvent.getModel())) {
						playlistSourceListProperty.set(playlistSourceListProperty.indexOf(modelPostSavedEvent.getModel()),
							(Playlist) modelPostSavedEvent.getModel());
					} else if (playlistDropListProperty.contains(modelPostSavedEvent.getModel())) {
						playlistDropListProperty.set(playlistDropListProperty.indexOf(modelPostSavedEvent.getModel()),
							(Playlist) modelPostSavedEvent.getModel());
					} else {
						playlistSourceListProperty.add((Playlist) modelPostSavedEvent.getModel());
					}
				}
			}
		});
	}

	@Subscribe
	public void onModelRemoved(final ModelRemovedEvent modelPostRemovedEvent) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (modelPostRemovedEvent.getModel() instanceof Account) {
					accountProperty.remove(modelPostRemovedEvent.getModel());
					if (selectedAccountProperty.get()
						.getSelectedItem() == null && accountProperty.size() > 0) {
						selectedAccountProperty.get()
							.select(accountProperty.get(0));
					}
				} else if (modelPostRemovedEvent.getModel() instanceof Template) {
					templateProperty.remove(modelPostRemovedEvent.getModel());
					if (selectedTemplateProperty.get()
						.getSelectedItem() == null && templateProperty.size() > 0) {
						selectedTemplateProperty.get()
							.select(templateProperty.get(0));
					}
				} else if (modelPostRemovedEvent.getModel() instanceof Playlist) {
					playlistSourceListProperty.remove(modelPostRemovedEvent.getModel());
					playlistDropListProperty.remove(modelPostRemovedEvent.getModel());
				}
			}
		});

	}
}
