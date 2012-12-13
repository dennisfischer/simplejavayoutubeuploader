package org.chaosfisch.youtubeuploader.view.models;

import java.io.File;
import java.sql.Date;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.converter.DefaultStringConverter;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.google.atom.AtomCategory;
import org.chaosfisch.util.ExtendedPlaceholders;
import org.chaosfisch.util.ThreadUtil;
import org.chaosfisch.youtubeuploader.I18nHelper;
import org.chaosfisch.youtubeuploader.controller.ViewController;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.ModelEvents;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.models.Template;
import org.chaosfisch.youtubeuploader.models.Upload;
import org.chaosfisch.youtubeuploader.models.UploadBuilder;
import org.chaosfisch.youtubeuploader.services.youtube.spi.PlaylistService;
import org.javalite.activejdbc.Model;

import com.google.inject.Inject;

public class UploadViewModel
{
	private final ObservableList<Model>			accountItems					= FXCollections.observableArrayList();
	private final ObservableList<Model>			playlistSourceItems				= FXCollections.observableArrayList();
	private final ObservableList<Model>			playlistDropItems				= FXCollections.observableArrayList();
	private final ObservableList<Model>			templateItems					= FXCollections.observableArrayList();

	private final ObservableList<String>		visibilityItems					= FXCollections.observableArrayList();
	private final ObservableList<String>		commentItems					= FXCollections.observableArrayList();
	private final ObservableList<String>		licenseItems					= FXCollections.observableArrayList();
	private final ObservableList<String>		videoresponseItems				= FXCollections.observableArrayList();
	private final ObservableList<File>			fileItems						= FXCollections.observableArrayList();
	private final ObservableList<AtomCategory>	categoryItems					= FXCollections.observableArrayList();

	public SimpleListProperty<Model>			accountProperty					= new SimpleListProperty<>(accountItems);
	public SimpleListProperty<Model>			templateProperty				= new SimpleListProperty<>(templateItems);
	public SimpleListProperty<Model>			playlistDropListProperty		= new SimpleListProperty<>(playlistDropItems);
	public SimpleListProperty<Model>			playlistSourceListProperty		= new SimpleListProperty<>(playlistSourceItems);

	public SimpleListProperty<AtomCategory>		categoryProperty				= new SimpleListProperty<>(categoryItems);
	public SimpleListProperty<String>			commentProperty					= new SimpleListProperty<>(commentItems);
	public SimpleListProperty<File>				fileProperty					= new SimpleListProperty<>(fileItems);
	public SimpleListProperty<String>			licenseProperty					= new SimpleListProperty<>(licenseItems);
	public SimpleListProperty<String>			videoresponseProperty			= new SimpleListProperty<>(videoresponseItems);
	public SimpleListProperty<String>			visibilityProperty				= new SimpleListProperty<>(visibilityItems);

	public SimpleStringProperty					previewTitleProperty			= new SimpleStringProperty();
	public SimpleBooleanProperty				commentVoteProperty				= new SimpleBooleanProperty();
	public SimpleStringProperty					defaultdirProperty				= new SimpleStringProperty();
	public SimpleStringProperty					descriptionProperty				= new SimpleStringProperty();
	public SimpleBooleanProperty				embedProperty					= new SimpleBooleanProperty();
	public SimpleStringProperty					enddirProperty					= new SimpleStringProperty();
	public SimpleBooleanProperty				mobileProperty					= new SimpleBooleanProperty();
	public SimpleBooleanProperty				rateProperty					= new SimpleBooleanProperty();
	public SimpleStringProperty					tagsProperty					= new SimpleStringProperty();
	public SimpleStringProperty					titleProperty					= new SimpleStringProperty();
	public SimpleObjectProperty<Date>			starttimeProperty				= new SimpleObjectProperty<>();
	public SimpleObjectProperty<Date>			releasetimeProperty				= new SimpleObjectProperty<>();
	public SimpleIntegerProperty				numberProperty					= new SimpleIntegerProperty();

	public SimpleObjectProperty<File>			selectedFileProperty			= new SimpleObjectProperty<>();
	public SimpleObjectProperty<AtomCategory>	selectedCategoryProperty		= new SimpleObjectProperty<>();
	public SimpleIntegerProperty				selectedCategoryIndexProperty	= new SimpleIntegerProperty();
	public SimpleObjectProperty<Model>			selectedAccountProperty			= new SimpleObjectProperty<>();
	public SimpleIntegerProperty				selectedCommentProperty			= new SimpleIntegerProperty();
	public SimpleIntegerProperty				selectedLicenseProperty			= new SimpleIntegerProperty();
	public SimpleIntegerProperty				selectedVideoResponseProperty	= new SimpleIntegerProperty();
	public SimpleIntegerProperty				selectedVisibilityProperty		= new SimpleIntegerProperty();
	public SimpleObjectProperty<Model>			selectedTemplateProperty		= new SimpleObjectProperty<>();

	public SimpleObjectProperty<File>			initialDirectoryProperty		= new SimpleObjectProperty<>();

	@Inject private PlaylistService				playlistService;

	public UploadViewModel()
	{
		AnnotationProcessor.process(this);
		init();
	}

	@SuppressWarnings("unchecked")
	private void init()
	{
		visibilityProperty.addAll(	I18nHelper.message("visibilitylist.public"),
									I18nHelper.message("visibilitylist.unlisted"),
									I18nHelper.message("visibilitylist.private"),
									I18nHelper.message("visibilitylist.scheduled"));

		commentProperty.addAll(	I18nHelper.message("commentlist.allowed"),
								I18nHelper.message("commentlist.moderated"),
								I18nHelper.message("commentlist.denied"),
								I18nHelper.message("commentlist.friendsonly"));
		licenseProperty.addAll(I18nHelper.message("licenselist.youtube"), I18nHelper.message("licenselist.cc"));
		videoresponseProperty.addAll(	I18nHelper.message("videoresponselist.allowed"),
										I18nHelper.message("videoresponselist.moderated"),
										I18nHelper.message("videoresponselist.denied"));

		accountProperty.addAll(Account.where("type = ?", Account.Type.YOUTUBE.name()).include(Playlist.class));
		templateProperty.addAll(Template.findAll().include(Account.class, Playlist.class));

		final InvalidationListener previewTitleChangeListener = new InvalidationListener() {

			@Override
			public void invalidated(final Observable observable)
			{
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
			public String toString(final String value)
			{
				extendedPlaceholders.setFile(selectedFileProperty.get() != null ? selectedFileProperty.get().getAbsolutePath() : "{file-missing}");
				extendedPlaceholders.setNumber(numberProperty.get());

				return extendedPlaceholders.replace(value);
			}
		});

		selectedTemplateProperty.addListener(new InvalidationListener() {

			@Override
			public void invalidated(final Observable arg0)
			{
				resetTemplate();
			}
		});

		selectedAccountProperty.addListener(new ChangeListener<Model>() {

			@Override
			public void changed(final ObservableValue<? extends Model> observable, final Model oldValue, final Model newValue)
			{
				if ((newValue != null) && (newValue.get("playlists") != null))
				{
					playlistSourceListProperty.clear();
					playlistSourceListProperty.addAll((Collection<Model>) newValue.get("playlists"));
				}
			}
		});
	}

	public void resetTemplate()
	{
		_reset((Template) (selectedTemplateProperty.get() == null ? ViewController.standardTemplate : selectedTemplateProperty.get()));
	}

	private void _reset(final Template template)
	{
		if (!categoryProperty.isEmpty())
		{
			selectedCategoryIndexProperty.set(template.getInteger("category") != null ? template.getInteger("category") : 0);
		}
		selectedCommentProperty.set(template.getInteger("comment") != null ? template.getInteger("comment") : 0);
		commentVoteProperty.set(template.getBoolean("commentvote"));
		defaultdirProperty.set(template.getString("defaultdir"));
		descriptionProperty.set(template.getString("description"));
		embedProperty.set(template.getBoolean("embed"));
		enddirProperty.set(template.getString("enddir"));
		selectedLicenseProperty.set(template.getInteger("license") != null ? template.getInteger("license") : 0);
		mobileProperty.set(template.getBoolean("mobile"));
		rateProperty.set(template.getBoolean("rate"));
		tagsProperty.set(template.getString("keywords"));
		titleProperty.set(template.getString("title"));
		selectedVideoResponseProperty.set(template.getInteger("videoresponse") != null ? template.getInteger("videoresponse") : 1);
		selectedVisibilityProperty.set(template.getInteger("visibility") != null ? template.getInteger("visibility") : 0);
		numberProperty.set(template.getInteger("number") != null ? template.getInteger("number") : 0);

		if (template.parent(Account.class) != null)
		{
			selectedAccountProperty.set(template.parent(Account.class));
		}
		for (final Model playlist : playlistDropListProperty.get())
		{
			removePlaylistFromDropzone(playlistDropListProperty.indexOf(playlist));
		}
		playlistDropListProperty.clear();
		for (final Model playlist : template.getAll(Playlist.class))
		{
			movePlaylistToDropzone(playlistSourceListProperty.indexOf(playlist));
		}

		final File defaultDir = new File(template.getString("defaultdir") != null ? template.getString("defaultdir") : "");
		if (defaultDir.exists() && defaultDir.isDirectory())
		{
			initialDirectoryProperty.set(defaultDir);
		}
	}

	public Upload toUpload()
	{
		final UploadBuilder uploadBuilder = new UploadBuilder(	selectedFileProperty.get(),
																titleProperty.get(),
																selectedCategoryProperty.get().term,
																(Account) selectedAccountProperty.get()).setComment(selectedCommentProperty.get())
				.setCommentvote(commentVoteProperty.get())
				.setDescription(descriptionProperty.get())
				.setEmbed(embedProperty.get())
				.setEnddir(enddirProperty.get())
				.setLicense(selectedLicenseProperty.get())
				.setMobile(mobileProperty.get())
				.setNumber(numberProperty.get())
				.setRate(rateProperty.get())
				.setTags(tagsProperty.get())
				.setVideoresponse(selectedVideoResponseProperty.get())
				.setVisibility(selectedVisibilityProperty.get());

		for (final Model playlist : playlistDropListProperty.get())
		{
			uploadBuilder.addPlaylist((Playlist) playlist);
		}

		if (starttimeProperty.get().getTime() > System.currentTimeMillis())
		{
			uploadBuilder.setStarted(starttimeProperty.get());
		}

		if (releasetimeProperty.get().getTime() > System.currentTimeMillis())
		{
			final Calendar calendar = new GregorianCalendar();
			calendar.setTime(releasetimeProperty.get());
			final int unroundedMinutes = calendar.get(Calendar.MINUTE);
			final int mod = unroundedMinutes % 30;
			calendar.add(Calendar.MINUTE, (mod < 16) ? -mod : (30 - mod));
			uploadBuilder.setRelease(calendar.getTime());
		}

		fileProperty.remove(selectedFileProperty.get());
		return uploadBuilder.build();
	}

	public void saveTemplate()
	{
		final Template template = (Template) selectedTemplateProperty.get();

		if (template == null) { return; }
		template.setInteger("category", selectedCategoryIndexProperty.get());
		template.setInteger("comment", selectedCommentProperty.get());
		template.setBoolean("commentvote", commentVoteProperty.get());
		template.setString("defaultdir", defaultdirProperty.get());
		template.setString("description", descriptionProperty.get());
		template.setBoolean("embed", embedProperty.get());
		template.setString("enddir", enddirProperty.get());
		template.setInteger("license", selectedLicenseProperty.get());
		template.setBoolean("mobile", mobileProperty.get());
		template.setBoolean("rate", rateProperty.get());
		template.setString("keywords", tagsProperty.get());
		template.setString("title", titleProperty.get());
		template.setInteger("videoresponse", selectedVideoResponseProperty.get());
		template.setInteger("visibility", selectedVisibilityProperty.get());
		template.setInteger("number", numberProperty.get());
		if (selectedAccountProperty.get() != null)
		{
			template.setParent(selectedAccountProperty.get());
		}

		for (final Playlist playlist : template.getAll(Playlist.class))
		{
			template.remove(playlist);
		}
		for (final Model playlist : playlistDropListProperty.get())
		{
			template.add(playlist);
		}
		template.saveIt();

	}

	public void movePlaylistToDropzone(final int model)
	{
		if (model >= 0)
		{
			playlistDropListProperty.add(playlistSourceListProperty.get(model));
			playlistSourceListProperty.remove(model);
			// TODO CHECK IF STILL NEEDED
			// RefresherUtil.refresh(playlistSourcezone, playlistItems);
			// RefresherUtil.refresh(playlistDropzone, playlistDropList);
		}
	}

	public void removePlaylistFromDropzone(final int model)
	{
		if (model >= 0)
		{
			playlistSourceListProperty.add(playlistDropListProperty.get(model));
			playlistDropListProperty.remove(model);
			// RefresherUtil.refresh(playlistSourcezone, playlistItems);
			// RefresherUtil.refresh(playlistDropzone, playlistDropList);
		}
	}

	public void removeTemplate()
	{
		if (selectedTemplateProperty.get() != null)
		{
			selectedTemplateProperty.get().delete();
		}
	}

	@EventTopicSubscriber(topic = ModelEvents.MODEL_POST_ADDED)
	public void onAdded(final String topic, final Model model)
	{
		Platform.runLater(new Runnable() {

			@Override
			public void run()
			{
				if ((model instanceof Account) && model.get("type").equals(Account.Type.YOUTUBE.name()))
				{
					accountProperty.add(model);
					if ((selectedAccountProperty.get() == null) && (accountProperty.size() > 0))
					{
						selectedAccountProperty.set(accountProperty.get(0));
					}
				} else if (model instanceof Template)
				{
					templateProperty.add(model);
					if ((selectedTemplateProperty.get() == null) && (templateProperty.size() > 0))
					{
						selectedTemplateProperty.set(templateProperty.get(0));
					}
				} else if ((model instanceof Playlist) && model.parent(Account.class).equals(selectedAccountProperty.get()))
				{
					playlistSourceListProperty.add(model);
				}
			}
		});
	}

	@EventTopicSubscriber(topic = ModelEvents.MODEL_POST_REMOVED)
	public void onRemoved(final String topic, final Model model)
	{
		Platform.runLater(new Runnable() {

			@Override
			public void run()
			{
				if (model instanceof Account)
				{
					accountProperty.remove(model);
					if ((selectedAccountProperty.get() == null) && (accountProperty.size() > 0))
					{
						selectedAccountProperty.set(accountProperty.get(0));
					}
				} else if (model instanceof Template)
				{
					templateProperty.remove(model);
					if ((selectedTemplateProperty.get() == null) && (templateProperty.size() > 0))
					{
						selectedTemplateProperty.set(templateProperty.get(0));
					}
				} else if (model instanceof Playlist)
				{
					playlistSourceListProperty.remove(model);
					playlistDropListProperty.remove(model);
				}
			}
		});
	}

	@EventTopicSubscriber(topic = ModelEvents.MODEL_POST_UPDATED)
	public void onUpdated(final String topic, final Model model)
	{
		Platform.runLater(new Runnable() {

			@Override
			public void run()
			{
				if (model instanceof Playlist)
				{
					int index = playlistSourceListProperty.indexOf(model);
					if (index != -1)
					{
						playlistSourceListProperty.set(index, model);
					} else
					{
						index = playlistDropListProperty.indexOf(model);
						if (index != -1)
						{
							playlistDropListProperty.set(index, model);
						}
					}
					// RefresherUtil.refresh(playlistSourcezone, playlistItems);
					// RefresherUtil.refresh(playlistDropzone,
					// playlistDropList);
					// TODO CHECK IF NEEDED
				}
			}
		});
	}

	public void refreshPlaylists()
	{
		final Account[] accountArray = new Account[accountProperty.size()];
		accountProperty.toArray(accountArray);
		ThreadUtil.doInBackground(new Runnable() {

			@Override
			public void run()
			{
				playlistService.synchronizePlaylists(Arrays.asList(accountArray));
			}
		});
	}
}