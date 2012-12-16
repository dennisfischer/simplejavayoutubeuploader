package org.chaosfisch.youtubeuploader.view.models;

import java.io.File;
import java.sql.Date;
import java.util.Arrays;
import java.util.Calendar;
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
import javafx.scene.control.SingleSelectionModel;
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

	public final SimpleListProperty<Model>							accountProperty				= new SimpleListProperty<>(
																										FXCollections.<Model> observableArrayList());
	public final SimpleListProperty<Model>							templateProperty			= new SimpleListProperty<>(
																										FXCollections.<Model> observableArrayList());
	public final SimpleListProperty<Model>							playlistDropListProperty	= new SimpleListProperty<>(
																										FXCollections.<Model> observableArrayList());
	public final SimpleListProperty<Model>							playlistSourceListProperty	= new SimpleListProperty<>(
																										FXCollections.<Model> observableArrayList());
	public final SimpleListProperty<AtomCategory>					categoryProperty			= new SimpleListProperty<>(
																										FXCollections.<AtomCategory> observableArrayList());
	public final SimpleListProperty<String>							commentProperty				= new SimpleListProperty<>(
																										FXCollections.<String> observableArrayList());
	public final SimpleListProperty<File>							fileProperty				= new SimpleListProperty<>(
																										FXCollections.<File> observableArrayList());
	public final SimpleListProperty<String>							licenseProperty				= new SimpleListProperty<>(
																										FXCollections.<String> observableArrayList());
	public final SimpleListProperty<String>							videoresponseProperty		= new SimpleListProperty<>(
																										FXCollections.<String> observableArrayList());
	public final SimpleListProperty<String>							visibilityProperty			= new SimpleListProperty<>(
																										FXCollections.<String> observableArrayList());
	public final SimpleStringProperty								previewTitleProperty		= new SimpleStringProperty();
	public final SimpleBooleanProperty								commentVoteProperty			= new SimpleBooleanProperty();
	public final SimpleStringProperty								defaultdirProperty			= new SimpleStringProperty();
	public final SimpleStringProperty								descriptionProperty			= new SimpleStringProperty();
	public final SimpleBooleanProperty								embedProperty				= new SimpleBooleanProperty();
	public final SimpleStringProperty								enddirProperty				= new SimpleStringProperty();
	public final SimpleBooleanProperty								mobileProperty				= new SimpleBooleanProperty();
	public final SimpleBooleanProperty								rateProperty				= new SimpleBooleanProperty();
	public final SimpleStringProperty								tagsProperty				= new SimpleStringProperty();
	public final SimpleStringProperty								titleProperty				= new SimpleStringProperty();
	public final SimpleObjectProperty<Date>							starttimeProperty			= new SimpleObjectProperty<>();
	public final SimpleObjectProperty<Date>							releasetimeProperty			= new SimpleObjectProperty<>();
	public final SimpleIntegerProperty								numberProperty				= new SimpleIntegerProperty();
	public final SimpleObjectProperty<File>							initialDirectoryProperty	= new SimpleObjectProperty<>();

	public SimpleObjectProperty<SingleSelectionModel<AtomCategory>>	selectedCategoryProperty;
	public SimpleObjectProperty<SingleSelectionModel<File>>			selectedFileProperty;
	public SimpleObjectProperty<SingleSelectionModel<Model>>		selectedAccountProperty;
	public SimpleObjectProperty<SingleSelectionModel<String>>		selectedCommentProperty;
	public SimpleObjectProperty<SingleSelectionModel<String>>		selectedLicenseProperty;
	public SimpleObjectProperty<SingleSelectionModel<String>>		selectedVideoResponseProperty;
	public SimpleObjectProperty<SingleSelectionModel<String>>		selectedVisibilityProperty;
	public SimpleObjectProperty<SingleSelectionModel<Model>>		selectedTemplateProperty;

	@Inject private PlaylistService									playlistService;

	public UploadViewModel()
	{
		AnnotationProcessor.process(this);
	}

	@SuppressWarnings("unchecked")
	public void init(final SingleSelectionModel<AtomCategory> categorySelectionModel, final SingleSelectionModel<File> fileSelectionModel,
			final SingleSelectionModel<Model> accountSelectionModel, final SingleSelectionModel<String> commentSelectionModel,
			final SingleSelectionModel<String> licenseSelectionModel, final SingleSelectionModel<String> videoresponseSelectionModel,
			final SingleSelectionModel<String> visibilitySelectionModel, final SingleSelectionModel<Model> templateSelectionModel)
	{
		selectedCategoryProperty = new SimpleObjectProperty<>(categorySelectionModel);
		selectedFileProperty = new SimpleObjectProperty<>(fileSelectionModel);
		selectedAccountProperty = new SimpleObjectProperty<>(accountSelectionModel);
		selectedCommentProperty = new SimpleObjectProperty<>(commentSelectionModel);
		selectedLicenseProperty = new SimpleObjectProperty<>(licenseSelectionModel);
		selectedVideoResponseProperty = new SimpleObjectProperty<>(videoresponseSelectionModel);
		selectedVisibilityProperty = new SimpleObjectProperty<>(visibilitySelectionModel);
		selectedTemplateProperty = new SimpleObjectProperty<>(templateSelectionModel);

		visibilityProperty.addAll(	I18nHelper.message("visibilitylist.public"), I18nHelper.message("visibilitylist.unlisted"),
									I18nHelper.message("visibilitylist.private"), I18nHelper.message("visibilitylist.scheduled"));

		commentProperty.addAll(	I18nHelper.message("commentlist.allowed"), I18nHelper.message("commentlist.moderated"),
								I18nHelper.message("commentlist.denied"), I18nHelper.message("commentlist.friendsonly"));
		licenseProperty.addAll(I18nHelper.message("licenselist.youtube"), I18nHelper.message("licenselist.cc"));
		videoresponseProperty.addAll(	I18nHelper.message("videoresponselist.allowed"), I18nHelper.message("videoresponselist.moderated"),
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
				extendedPlaceholders.setFile(selectedFileProperty.get().getSelectedItem() != null ? selectedFileProperty.get()
						.getSelectedItem()
						.getAbsolutePath() : "{file-missing}");
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

		selectedAccountProperty.get().selectedItemProperty().addListener(new ChangeListener<Model>() {

			@Override
			public void changed(final ObservableValue<? extends Model> observable, final Model oldValue, final Model newValue)
			{
				if ((newValue != null) && (newValue.getAll(Playlist.class) != null))
				{
					playlistSourceListProperty.clear();
					playlistSourceListProperty.addAll(newValue.getAll(Playlist.class));
				}
			}
		});
	}

	public void resetTemplate()
	{
		_reset((Template) (selectedTemplateProperty.get().getSelectedItem() == null ? ViewController.standardTemplate
				: selectedTemplateProperty.get().getSelectedItem()));
	}

	private void _reset(final Template template)
	{
		if (!categoryProperty.isEmpty())
		{
			selectedCategoryProperty.get().select(template.getInteger("category") != null ? template.getInteger("category") : 0);
		}
		selectedCommentProperty.get().select(template.getInteger("comment") != null ? template.getInteger("comment") : 0);
		commentVoteProperty.set(template.getBoolean("commentvote"));
		defaultdirProperty.set(template.getString("defaultdir"));
		descriptionProperty.set(template.getString("description"));
		embedProperty.set(template.getBoolean("embed"));
		enddirProperty.set(template.getString("enddir"));
		selectedLicenseProperty.get().select(template.getInteger("license") != null ? template.getInteger("license") : 0);
		mobileProperty.set(template.getBoolean("mobile"));
		rateProperty.set(template.getBoolean("rate"));
		tagsProperty.set(template.getString("keywords"));
		titleProperty.set(template.getString("title"));
		selectedVideoResponseProperty.get().select(template.getInteger("videoresponse") != null ? template.getInteger("videoresponse") : 1);
		selectedVisibilityProperty.get().select(template.getInteger("visibility") != null ? template.getInteger("visibility") : 0);
		numberProperty.set(template.getInteger("number") != null ? template.getInteger("number") : 0);

		if (template.parent(Account.class) != null)
		{
			selectedAccountProperty.get().select(template.parent(Account.class));
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
		final UploadBuilder uploadBuilder = new UploadBuilder(selectedFileProperty.get().getSelectedItem(), titleProperty.get(),
				selectedCategoryProperty.get().getSelectedItem().term, (Account) selectedAccountProperty.get().getSelectedItem()).setComment(	selectedCommentProperty.get()
																																						.getSelectedIndex())
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
				.setVisibility(selectedVisibilityProperty.get().getSelectedIndex());

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
		final Template template = (Template) selectedTemplateProperty.get().getSelectedItem();

		if (template == null) { return; }
		template.setInteger("category", selectedCategoryProperty.get().getSelectedIndex());
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
			template.setParent(selectedAccountProperty.get().getSelectedItem());
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
		}
	}

	public void removePlaylistFromDropzone(final int model)
	{
		if (model >= 0)
		{
			playlistSourceListProperty.add(playlistDropListProperty.get(model));
			playlistDropListProperty.remove(model);
		}
	}

	public void removeTemplate()
	{
		if (selectedTemplateProperty.get() != null)
		{
			selectedTemplateProperty.get().getSelectedItem().delete();
		}
	}

	@EventTopicSubscriber(topic = ModelEvents.MODEL_POST_SAVED)
	public void onAdded(final String topic, final Model model)
	{
		Platform.runLater(new Runnable() {

			@Override
			public void run()
			{
				if ((model instanceof Account) && model.get("type").equals(Account.Type.YOUTUBE.name()))
				{
					if (accountProperty.contains(model))
					{
						accountProperty.set(accountProperty.indexOf(model), model);
					} else
					{
						accountProperty.add(model);
						if ((selectedAccountProperty.get().getSelectedItem() == null) && (accountProperty.size() > 0))
						{
							selectedAccountProperty.get().select(accountProperty.get(0));
						}
					}
				} else if (model instanceof Template)
				{
					if (templateProperty.contains(model))
					{
						templateProperty.set(templateProperty.indexOf(model), model);
					} else
					{
						templateProperty.add(model);
						if ((selectedTemplateProperty.get().getSelectedItem() == null) && (templateProperty.size() > 0))
						{
							selectedTemplateProperty.get().select(templateProperty.get(0));
						}
					}
				} else if ((model instanceof Playlist) && model.parent(Account.class).equals(selectedAccountProperty.get().getSelectedItem()))
				{
					if (playlistSourceListProperty.contains(model))
					{
						playlistSourceListProperty.set(playlistSourceListProperty.indexOf(model), model);
					} else if (playlistDropListProperty.contains(model))
					{
						playlistDropListProperty.set(playlistDropListProperty.indexOf(model), model);
					} else
					{
						playlistSourceListProperty.add(model);
					}
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
					if ((selectedAccountProperty.get().getSelectedItem() == null) && (accountProperty.size() > 0))
					{
						selectedAccountProperty.get().select(accountProperty.get(0));
					}
				} else if (model instanceof Template)
				{
					templateProperty.remove(model);
					if ((selectedTemplateProperty.get().getSelectedItem() == null) && (templateProperty.size() > 0))
					{
						selectedTemplateProperty.get().select(templateProperty.get(0));
					}
				} else if (model instanceof Playlist)
				{
					playlistSourceListProperty.remove(model);
					playlistDropListProperty.remove(model);
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