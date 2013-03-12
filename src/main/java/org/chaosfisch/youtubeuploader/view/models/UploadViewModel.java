/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.view.models;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

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
import org.chaosfisch.util.EventBusUtil;
import org.chaosfisch.util.ExtendedPlaceholders;
import org.chaosfisch.youtubeuploader.ApplicationData;
import org.chaosfisch.youtubeuploader.I18nHelper;
import org.chaosfisch.youtubeuploader.controller.ViewController;
import org.chaosfisch.youtubeuploader.db.dao.AccountDao;
import org.chaosfisch.youtubeuploader.db.dao.PlaylistDao;
import org.chaosfisch.youtubeuploader.db.dao.TemplateDao;
import org.chaosfisch.youtubeuploader.db.dao.TemplatePlaylistDao;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Account;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.TemplatePlaylist;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;
import org.chaosfisch.youtubeuploader.models.AccountsType;
import org.chaosfisch.youtubeuploader.models.UploadBuilder;
import org.chaosfisch.youtubeuploader.models.events.ModelPostRemovedEvent;
import org.chaosfisch.youtubeuploader.models.events.ModelPostSavedEvent;
import org.chaosfisch.youtubeuploader.services.youtube.spi.PlaylistService;
import org.jooq.impl.Executor;

import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class UploadViewModel {

	// {{ UploadOptions
	public final SimpleListProperty<Account>						accountProperty					= new SimpleListProperty<>(
																										FXCollections
																											.<Account> observableArrayList());
	public final SimpleListProperty<Template>						templateProperty				= new SimpleListProperty<>(
																										FXCollections
																											.<Template> observableArrayList());
	public final SimpleListProperty<Playlist>						playlistDropListProperty		= new SimpleListProperty<>(
																										FXCollections
																											.<Playlist> observableArrayList());
	public final SimpleListProperty<Playlist>						playlistSourceListProperty		= new SimpleListProperty<>(
																										FXCollections
																											.<Playlist> observableArrayList());
	public final SimpleListProperty<AtomCategory>					categoryProperty				= new SimpleListProperty<>(
																										FXCollections
																											.<AtomCategory> observableArrayList());
	public final SimpleListProperty<String>							commentProperty					= new SimpleListProperty<>(
																										FXCollections
																											.<String> observableArrayList());
	public final SimpleListProperty<File>							fileProperty					= new SimpleListProperty<>(
																										FXCollections
																											.<File> observableArrayList());
	public final SimpleListProperty<String>							licenseProperty					= new SimpleListProperty<>(
																										FXCollections
																											.<String> observableArrayList());
	public final SimpleListProperty<String>							videoresponseProperty			= new SimpleListProperty<>(
																										FXCollections
																											.<String> observableArrayList());
	public final SimpleListProperty<String>							visibilityProperty				= new SimpleListProperty<>(
																										FXCollections
																											.<String> observableArrayList());
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

	public final SimpleObjectProperty<Calendar>						starttimeProperty				= new SimpleObjectProperty<>(
																										Calendar.getInstance());
	public final SimpleObjectProperty<Calendar>						releasetimeProperty				= new SimpleObjectProperty<>(
																										Calendar.getInstance());
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

	public SimpleListProperty<String>								claimTypeProperty				= new SimpleListProperty<>(
																										FXCollections
																											.<String> observableArrayList());
	public SimpleListProperty<String>								claimOptionsProperty			= new SimpleListProperty<>(
																										FXCollections
																											.<String> observableArrayList());

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
	private TemplatePlaylistDao										templatePlaylistDao;
	@Inject
	private Executor												exec;

	@Inject
	@Named(value = ApplicationData.SERVICE_EXECUTOR)
	private ListeningExecutorService								pool;

	public UploadViewModel() {
		EventBusUtil.getInstance().register(this);
	}

	@SuppressWarnings("unchecked")
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

		visibilityProperty.addAll(
			I18nHelper.message("visibilitylist.public"),
			I18nHelper.message("visibilitylist.unlisted"),
			I18nHelper.message("visibilitylist.private"),
			I18nHelper.message("visibilitylist.scheduled"));

		commentProperty.addAll(
			I18nHelper.message("commentlist.allowed"),
			I18nHelper.message("commentlist.moderated"),
			I18nHelper.message("commentlist.denied"),
			I18nHelper.message("commentlist.friendsonly"));
		licenseProperty.addAll(I18nHelper.message("licenselist.youtube"), I18nHelper.message("licenselist.cc"));
		videoresponseProperty.addAll(
			I18nHelper.message("videoresponselist.allowed"),
			I18nHelper.message("videoresponselist.moderated"),
			I18nHelper.message("videoresponselist.denied"));

		claimTypeProperty.addAll(
			I18nHelper.message("claimtype.audiovisual"),
			I18nHelper.message("claimtype.visual"),
			I18nHelper.message("claimtype.audio"));
		claimOptionsProperty.addAll(
			I18nHelper.message("claimoptions.monetize"),
			I18nHelper.message("claimoptions.track"),
			I18nHelper.message("claimoptions.block"));

		accountProperty.addAll(accountDao.fetchByType(AccountsType.YOUTUBE.name()));
		templateProperty.addAll(templateDao.findAll());

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
				extendedPlaceholders.setFile(selectedFileProperty.get().getSelectedItem() != null ? selectedFileProperty
					.get()
					.getSelectedItem()
					.getAbsolutePath() : "{file-missing}");
				extendedPlaceholders.setNumber(numberProperty.get());

				return extendedPlaceholders.replace(value);
			}
		});

		selectedTemplateProperty.get().selectedItemProperty().addListener(new InvalidationListener() {

			@Override
			public void invalidated(final Observable arg0) {
				resetTemplate();
			}
		});

		selectedAccountProperty.get().selectedItemProperty().addListener(new ChangeListener<Account>() {

			@Override
			public void changed(final ObservableValue<? extends Account> observable, final Account oldValue, final Account newValue) {
				if (newValue != null) {
					playlistSourceListProperty.clear();
					playlistSourceListProperty.addAll(playlistDao.fetchByAccountId(newValue.getId()));
				}
			}
		});
	}

	public void resetTemplate() {
		_reset(selectedTemplateProperty.get().getSelectedItem() == null ? ViewController.standardTemplate : selectedTemplateProperty
			.get()
			.getSelectedItem());
	}

	private void _reset(final Template template) {
		if (!categoryProperty.isEmpty()) {
			for (final AtomCategory category : categoryProperty) {
				if (category.term.equals(template.getCategory())) {
					selectedCategoryProperty.get().select(category);
				}
			}
		}
		selectedCommentProperty.get().select(template.getComment().intValue());
		commentVoteProperty.set(template.getCommentvote().booleanValue());
		defaultdirProperty.set(template.getDefaultdir());
		descriptionProperty.set(template.getDescription());
		embedProperty.set(template.getEmbed().booleanValue());
		enddirProperty.set(template.getEnddir());
		selectedLicenseProperty.get().select(template.getLicense().intValue());
		mobileProperty.set(template.getMobile().booleanValue());
		rateProperty.set(template.getRate().booleanValue());
		tagsProperty.set(template.getKeywords());
		titleProperty.set(template.getTitle());
		selectedVideoResponseProperty.get().select(template.getVideoresponse().intValue());
		selectedVisibilityProperty.get().select(template.getVisibility().intValue());
		numberProperty.set(template.getNumber().intValue());
		thumbnailProperty.set(template.getThumbnail());
		facebookProperty.set(template.getFacebook().booleanValue());
		twitterProperty.set(template.getTwitter().booleanValue());
		messageProperty.set(template.getMessage());

		final Account account = accountDao.fetchOneById(template.getAccountId());
		if (account != null) {
			selectedAccountProperty.get().select(account);
		}

		final Iterator<Playlist> playlistDropListIterator = playlistDropListProperty.get().iterator();
		while (playlistDropListIterator.hasNext()) {
			final Playlist playlist = playlistDropListIterator.next();
			playlistSourceListProperty.add(playlist);
			playlistDropListIterator.remove();
		}

		final Iterator<Playlist> playlistSourceListIterator = playlistDao.fetchByTemplate(template).iterator();
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

		claimProperty.set(template.getMonetizepartner().booleanValue());
		overlayProperty.set(template.getOverlay().booleanValue());
		trueViewProperty.set(template.getTrueview().booleanValue());
		inStreamProperty.set(template.getInstream().booleanValue());
		inStreamDefaultsProperty.set(template.getInstreamdefaults().booleanValue());
		productPlacementProperty.set(template.getProduct().booleanValue());
		partnerProperty.set(template.getMonetizepartner().booleanValue());

		selectedClaimTypeProperty.get().select(template.getMonetizeclaimtype().intValue());
		selectedClaimOptionProperty.get().select(template.getMonetizeclaimpolicy().intValue());
		selectedAssetTypeProperty
			.get()
			.get()
			.getToggleGroup()
			.selectToggle(selectedAssetTypeProperty.get().get().getToggleGroup().getToggles().get(template.getMonetizeasset().intValue()));
		selectedContentSyndicationProperty
			.get()
			.get()
			.getToggleGroup()
			.selectToggle(
				selectedContentSyndicationProperty.get().get().getToggleGroup().getToggles().get(template.getSyndication().intValue()));

		monetizeDescriptionProperty.set(template.getMonetizedescription());
		monetizeTitleEpisodeProperty.set(template.getMonetizetitleepisode());
		numberSeasonProperty.set(template.getMonetizeseasonnb());
		numberEpisodeProperty.set(template.getMonetizeepisodenb());
		monetizeTitleProperty.set(template.getMonetizetitle());
		tmsidProperty.set(template.getMonetizetmsid());
		isanProperty.set(template.getMonetizeisan());
		eidrProperty.set(template.getMonetizeeidr());
		monetizeNotesProperty.set(template.getMonetizenotes());
		customidProperty.set(template.getMonetizeid());
	}

	public Upload toUpload() {
		final UploadBuilder uploadBuilder = new UploadBuilder(
			selectedFileProperty.get().getSelectedItem(),
			titleProperty.get(),
			selectedCategoryProperty.get().getSelectedItem().term,
			selectedAccountProperty.get().getSelectedItem())
			.setComment(selectedCommentProperty.get().getSelectedIndex())
			.setCommentvote(commentVoteProperty.get())
			.setDescription(descriptionProperty.get())
			.setEmbed(embedProperty.get())
			.setEnddir(enddirProperty.get())
			.setLicense(selectedLicenseProperty.get().getSelectedIndex())
			.setMobile(mobileProperty.get())
			.setNumber(numberProperty.get())
			.setRate(rateProperty.get())
			.setTags(tagsProperty.get())
			.setVideoresponse(selectedVideoResponseProperty.get().getSelectedIndex())
			.setVisibility(selectedVisibilityProperty.get().getSelectedIndex())
			.setThumbnail(thumbnailProperty.get());
		if (idProperty.get() != -1) {
			uploadBuilder.setId(idProperty.get());
		}

		for (final Playlist playlist : playlistDropListProperty.get()) {
			uploadBuilder.addPlaylist(playlist);
		}

		if (starttimeProperty.get() != null && starttimeProperty.get().getTimeInMillis() > System.currentTimeMillis()) {
			uploadBuilder.setStarted(starttimeProperty.get().getTime());
		}

		if (releasetimeProperty.get() != null && releasetimeProperty.get().getTimeInMillis() > System.currentTimeMillis()) {
			final Calendar calendar = new GregorianCalendar();
			calendar.setTime(releasetimeProperty.get().getTime());
			final int unroundedMinutes = calendar.get(Calendar.MINUTE);
			final int mod = unroundedMinutes % 30;
			calendar.add(Calendar.MINUTE, mod < 16 ? -mod : 30 - mod);
			uploadBuilder.setRelease(calendar.getTime());

			if ((facebookProperty.get() || twitterProperty.get()) && messageProperty.get() != null && !messageProperty.get().isEmpty()) {
				uploadBuilder.setFacebook(facebookProperty.get()).setTwitter(twitterProperty.get()).setMessage(messageProperty.get());
			}
		}

		final Upload upload = uploadBuilder.build();
		if (upload.isValid()) {
			fileProperty.remove(selectedFileProperty.get().getSelectedItem());
			selectedFileProperty.get().selectNext();
			idProperty.setValue(-1);
			uploadBuilder.finalize(upload);

			upload.setClaim(overlayProperty.get() || trueViewProperty.get() || inStreamProperty.get() || productPlacementProperty.get()
					|| claimProperty.get());
			upload.setOverlay(overlayProperty.get());
			upload.setTrueview(trueViewProperty.get());
			upload.setInstream(inStreamProperty.get());
			upload.setInstreamdefaults(inStreamDefaultsProperty.get());
			upload.setProduct(productPlacementProperty.get());
			upload.setSyndication(selectedContentSyndicationProperty
				.get()
				.get()
				.getToggleGroup()
				.getToggles()
				.indexOf(selectedContentSyndicationProperty.get().get()));
			upload.setMonetizepartner(claimProperty.get());
			upload.setMonetizeclaimtype(selectedClaimTypeProperty.get().selectedIndexProperty().get());
			upload.setMonetizeclaimpolicy(selectedClaimOptionProperty.get().selectedIndexProperty().get());
			upload.setMonetizeasset(selectedAssetTypeProperty
				.get()
				.get()
				.getToggleGroup()
				.getToggles()
				.indexOf(selectedAssetTypeProperty.get().get()));
			upload.setMonetizetmsid(tmsidProperty.get());
			upload.setMonetizeisan(isanProperty.get());
			upload.setMonetizeeidr(eidrProperty.get());
			upload.setMonetizenotes(monetizeNotesProperty.get());
			upload.setMonetizeid(customidProperty.get());
			upload.setMonetizetitle(monetizeTitleProperty.get());
			upload.setMonetizedescription(monetizeDescriptionProperty.get());
			upload.setMonetizetitleepisode(monetizeTitleEpisodeProperty.get());
			upload.setMonetizeseasonnb(numberSeasonProperty.get());
			upload.setMonetizeepisodenb(numberEpisodeProperty.get());
		}

		return upload;
	}

	public void fromUpload(final Upload upload) {
		final File file = new File(upload.getFile());
		idProperty.set(upload.getId());
		if (!fileProperty.contains(file)) {
			fileProperty.add(file);
		}
		selectedFileProperty.get().select(file);
		selectedAccountProperty.get().select(accountDao.fetchOneById(upload.getId()));
		selectedCommentProperty.get().select(upload.getComment().intValue());
		selectedLicenseProperty.get().select(upload.getLicense().intValue());
		selectedVideoResponseProperty.get().select(upload.getVideoresponse().intValue());
		selectedVisibilityProperty.get().select(upload.getVisibility().intValue());
		commentVoteProperty.set(upload.getCommentvote());
		descriptionProperty.set(upload.getDescription());
		embedProperty.set(upload.getEmbed().booleanValue());
		enddirProperty.set(upload.getEnddir());
		mobileProperty.set(upload.getMobile().booleanValue());
		numberProperty.set(upload.getNumber().intValue());
		rateProperty.set(upload.getRate().booleanValue());
		tagsProperty.set(upload.getKeywords());
		titleProperty.set(upload.getTitle());
		thumbnailProperty.set(upload.getThumbnail());
		twitterProperty.set(upload.getTwitter().booleanValue());
		facebookProperty.set(upload.getFacebook().booleanValue());
		messageProperty.set(upload.getMessage());
		Calendar calendar = Calendar.getInstance();
		if (upload.getStarted() != null) {
			calendar.setTime(upload.getStarted());
		}
		starttimeProperty.set(calendar);
		calendar = Calendar.getInstance();
		if (upload.getRelease() != null) {
			calendar.setTime(upload.getRelease());
		}
		releasetimeProperty.set(calendar);

		for (final AtomCategory category : categoryProperty) {
			if (category.term.equals(upload.getCategory())) {
				selectedCategoryProperty.get().select(category);
			}
		}

		final Iterator<Playlist> playlistDropListIterator = playlistDropListProperty.get().iterator();
		while (playlistDropListIterator.hasNext()) {
			final Playlist playlist = playlistDropListIterator.next();
			playlistSourceListProperty.add(playlist);
			playlistDropListIterator.remove();
		}

		final Iterator<Playlist> PlaylistourceListIterator = playlistDao.fetchByUpload(upload).iterator();
		while (PlaylistourceListIterator.hasNext()) {
			final Playlist playlist = PlaylistourceListIterator.next();
			playlistDropListProperty.add(playlist);
			playlistSourceListProperty.remove(playlist);
		}

		claimProperty.set(upload.getMonetizepartner().booleanValue());
		overlayProperty.set(upload.getOverlay().booleanValue());
		trueViewProperty.set(upload.getTrueview().booleanValue());
		inStreamProperty.set(upload.getInstream().booleanValue());
		inStreamDefaultsProperty.set(upload.getInstreamdefaults().booleanValue());
		productPlacementProperty.set(upload.getProduct().booleanValue());
		partnerProperty.set(upload.getMonetizepartner().booleanValue());

		selectedClaimTypeProperty.get().select(upload.getMonetizeclaimtype().intValue());
		selectedClaimOptionProperty.get().select(upload.getMonetizeclaimpolicy().intValue());
		selectedAssetTypeProperty
			.get()
			.get()
			.getToggleGroup()
			.selectToggle(selectedAssetTypeProperty.get().get().getToggleGroup().getToggles().get(upload.getMonetizeasset().intValue()));
		selectedContentSyndicationProperty
			.get()
			.get()
			.getToggleGroup()
			.selectToggle(selectedContentSyndicationProperty.get().get().getToggleGroup().getToggles().get(upload.getSyndication()));

		monetizeDescriptionProperty.set(upload.getMonetizedescription());
		monetizeTitleEpisodeProperty.set(upload.getMonetizetitleepisode());
		numberSeasonProperty.set(upload.getMonetizeseasonnb());
		numberEpisodeProperty.set(upload.getMonetizeepisodenb());
		monetizeTitleProperty.set(upload.getMonetizetitle());
		tmsidProperty.set(upload.getMonetizetmsid());
		isanProperty.set(upload.getMonetizeisan());
		eidrProperty.set(upload.getMonetizeeidr());
		monetizeNotesProperty.set(upload.getMonetizenotes());
		customidProperty.set(upload.getMonetizeid());
	}

	public void saveTemplate() {
		final Template template = selectedTemplateProperty.get().getSelectedItem();

		if (template == null) {
			return;
		}
		template.setCategory(selectedCategoryProperty.get().getSelectedItem().term);
		template.setComment((short) selectedCommentProperty.get().getSelectedIndex());
		template.setCommentvote(commentVoteProperty.get());
		template.setDefaultdir(defaultdirProperty.get());
		template.setDescription(descriptionProperty.get());
		template.setEmbed(embedProperty.get());
		template.setEnddir(enddirProperty.get());
		template.setLicense((short) selectedLicenseProperty.get().getSelectedIndex());
		template.setMobile(mobileProperty.get());
		template.setRate(rateProperty.get());
		template.setKeywords(tagsProperty.get());
		template.setTitle(titleProperty.get());
		template.setVideoresponse((short) selectedVideoResponseProperty.get().getSelectedIndex());
		template.setVisibility((short) selectedVisibilityProperty.get().getSelectedIndex());
		template.setNumber((short) numberProperty.get());
		if (selectedAccountProperty.get() != null) {
			template.setAccountId(selectedAccountProperty.get().getSelectedItem().getId());
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
		template.setOverlay(overlayProperty.getValue());
		template.setTrueview(trueViewProperty.getValue());
		template.setInstream(inStreamProperty.getValue());
		template.setInstreamdefaults(inStreamDefaultsProperty.getValue());
		template.setProduct(productPlacementProperty.getValue());
		template.setSyndication(selectedContentSyndicationProperty
			.get()
			.get()
			.getToggleGroup()
			.getToggles()
			.indexOf(selectedContentSyndicationProperty.get().get()));
		template.setMonetizepartner(claimProperty.get());
		template.setMonetizeclaimtype(selectedClaimTypeProperty.get().selectedIndexProperty().get());
		template.setMonetizeclaimpolicy(selectedClaimOptionProperty.get().selectedIndexProperty().get());
		template.setMonetizeasset(selectedAssetTypeProperty
			.get()
			.get()
			.getToggleGroup()
			.getToggles()
			.indexOf(selectedAssetTypeProperty.get().get()));
		template.setMonetizetmsid(tmsidProperty.get());
		template.setMonetizeisan(isanProperty.get());
		template.setMonetizeeidr(eidrProperty.get());
		template.setMonetizenotes(monetizeNotesProperty.get());
		template.setMonetizeid(customidProperty.get());
		template.setMonetizetitle(monetizeTitleProperty.get());
		template.setMonetizedescription(monetizeDescriptionProperty.get());
		template.setMonetizetitleepisode(monetizeTitleEpisodeProperty.get());
		template.setMonetizeseasonnb(numberSeasonProperty.get());
		template.setMonetizeepisodenb(numberEpisodeProperty.get());

		templateDao.update(template);
	}

	public void movePlaylistToDropzone(final int model) {
		if (model >= 0) {
			playlistDropListProperty.add(playlistSourceListProperty.get(model));
			playlistSourceListProperty.remove(model);
		}
	}

	public void removePlaylistFromDropzone(final int model) {
		if (model >= 0) {
			playlistSourceListProperty.add(playlistDropListProperty.get(model));
			playlistDropListProperty.remove(model);
		}
	}

	public void removeTemplate() {
		if (selectedTemplateProperty.get().getSelectedItem() != null) {
			templateDao.delete(selectedTemplateProperty.get().getSelectedItem());
		}
	}

	@Subscribe
	public void onModelAdded(final ModelPostSavedEvent modelPostSavedEvent) {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				if (modelPostSavedEvent.getModel() instanceof Account
						&& modelPostSavedEvent.getModel().get("type").equals(AccountsType.YOUTUBE.name())) {
					if (accountProperty.contains(modelPostSavedEvent.getModel())) {
						accountProperty.set(accountProperty.indexOf(modelPostSavedEvent.getModel()), modelPostSavedEvent.getModel());
					} else {
						accountProperty.add(modelPostSavedEvent.getModel());
						if (selectedAccountProperty.get().getSelectedItem() == null && accountProperty.size() > 0) {
							selectedAccountProperty.get().select(accountProperty.get(0));
						}
					}
				} else if (modelPostSavedEvent.getModel() instanceof Template) {
					if (templateProperty.contains(modelPostSavedEvent.getModel())) {
						templateProperty.set(templateProperty.indexOf(modelPostSavedEvent.getModel()), modelPostSavedEvent.getModel());
						selectedTemplateProperty.get().select(modelPostSavedEvent.getModel());
					} else {
						templateProperty.add(modelPostSavedEvent.getModel());
						if (selectedTemplateProperty.get().getSelectedItem() == null && templateProperty.size() > 0) {
							selectedTemplateProperty.get().select(templateProperty.get(0));
						}
					}
				} else if (modelPostSavedEvent.getModel() instanceof Playlist
						&& modelPostSavedEvent.getModel().parent(Account.class).equals(selectedAccountProperty.get().getSelectedItem())) {
					if (playlistSourceListProperty.contains(modelPostSavedEvent.getModel())) {
						playlistSourceListProperty.set(
							playlistSourceListProperty.indexOf(modelPostSavedEvent.getModel()),
							modelPostSavedEvent.getModel());
					} else if (playlistDropListProperty.contains(modelPostSavedEvent.getModel())) {
						playlistDropListProperty.set(
							playlistDropListProperty.indexOf(modelPostSavedEvent.getModel()),
							modelPostSavedEvent.getModel());
					} else {
						playlistSourceListProperty.add(modelPostSavedEvent.getModel());
					}
				}
			}
		});
	}

	@Subscribe
	public void onModelRemoved(final ModelPostRemovedEvent modelPostRemovedEvent) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (modelPostRemovedEvent.getModel() instanceof Account) {
					accountProperty.remove(modelPostRemovedEvent.getModel());
					if (selectedAccountProperty.get().getSelectedItem() == null && accountProperty.size() > 0) {
						selectedAccountProperty.get().select(accountProperty.get(0));
					}
				} else if (modelPostRemovedEvent.getModel() instanceof Template) {
					templateProperty.remove(modelPostRemovedEvent.getModel());
					if (selectedTemplateProperty.get().getSelectedItem() == null && templateProperty.size() > 0) {
						selectedTemplateProperty.get().select(templateProperty.get(0));
					}
				} else if (modelPostRemovedEvent.getModel() instanceof Playlist) {
					playlistSourceListProperty.remove(modelPostRemovedEvent.getModel());
					playlistDropListProperty.remove(modelPostRemovedEvent.getModel());
				}
			}
		});

	}

	public void refreshPlaylists() {
		final Account[] accountArray = new Account[accountProperty.size()];
		accountProperty.toArray(accountArray);

		final ListenableFuture<Map<Account, List<Playlist>>> future = pool.submit(new Callable<Map<Account, List<Playlist>>>() {
			@Override
			public Map<Account, List<Playlist>> call() throws Exception {
				return playlistService.synchronizePlaylists(Arrays.asList(accountArray));
			}
		});

		Futures.addCallback(future, new FutureCallback<Map<Account, List<Playlist>>>() {
			@Override
			public void onFailure(final Throwable t) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onSuccess(final Map<Account, List<Playlist>> result) {
				// TODO Auto-generated method stub
			}
		});
	}
}
