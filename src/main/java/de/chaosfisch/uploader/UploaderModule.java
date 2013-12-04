/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import de.chaosfisch.google.GoogleModule;
import de.chaosfisch.google.enddir.EnddirServiceImpl;
import de.chaosfisch.google.enddir.IEnddirService;
import de.chaosfisch.google.youtube.thumbnail.IThumbnailService;
import de.chaosfisch.google.youtube.thumbnail.ThumbnailServiceImpl;
import de.chaosfisch.google.youtube.upload.UploadPostProcessor;
import de.chaosfisch.google.youtube.upload.UploadPreProcessor;
import de.chaosfisch.google.youtube.upload.Uploader;
import de.chaosfisch.google.youtube.upload.metadata.AbstractMetadataService;
import de.chaosfisch.google.youtube.upload.metadata.IMetadataService;
import de.chaosfisch.services.ExportPostProcessor;
import de.chaosfisch.services.PlaceholderPreProcessor;
import de.chaosfisch.uploader.persistence.PersistenceModule;
import de.chaosfisch.uploader.persistence.dao.IPersistenceService;
import de.chaosfisch.util.TextUtil;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class UploaderModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(String.class).annotatedWith(Names.named(IPersistenceService.PERSISTENCE_FOLDER))
				.toInstance(String.format("%s/%s", ApplicationData.DATA_DIR, ApplicationData.VERSION));

		try {
			final String configFile = ApplicationData.DATA_DIR + "/config.properties";
			if (!Files.exists(Paths.get(configFile))) {
				Files.createFile(Paths.get(configFile));
			}
			final PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration(configFile);
			propertiesConfiguration.setAutoSave(true);
			bind(Configuration.class).toInstance(propertiesConfiguration);
		} catch (IOException | ConfigurationException e) {
			throw new RuntimeException(e);
		}
		install(new PersistenceModule());
		install(new GoogleModule());

		final Multibinder<UploadPreProcessor> preProcessorMultibinder = Multibinder.newSetBinder(binder(), UploadPreProcessor.class);
		preProcessorMultibinder.addBinding().to(PlaceholderPreProcessor.class);

		final Multibinder<UploadPostProcessor> uploadPostProcessorMultibinder = Multibinder.newSetBinder(binder(), UploadPostProcessor.class);
		uploadPostProcessorMultibinder.addBinding().to(ExportPostProcessor.class);

		bind(ResourceBundle.class).annotatedWith(Names.named("i18n-resources"))
				.toInstance(ResourceBundle.getBundle("de.chaosfisch.uploader.resources.application"));

		mapServices();
		mapUtil();
	}

	private void mapUtil() {
		final EventBus eventBus = new EventBus();
		bind(EventBus.class).toInstance(eventBus);
		bindListener(Matchers.any(), new TypeListener() {
			@Override
			public <I> void hear(@SuppressWarnings("unused") final TypeLiteral<I> type, final TypeEncounter<I> encounter) {
				encounter.register(new InjectionListener<I>() {
					@Override
					public void afterInjection(final I injectee) {
						eventBus.register(injectee);
					}
				});
			}
		});

		requestStaticInjection(TextUtil.class);
	}

	private void mapServices() {
		bind(IMetadataService.class).to(AbstractMetadataService.class).in(Singleton.class);
		bind(IEnddirService.class).to(EnddirServiceImpl.class).in(Singleton.class);
		bind(IThumbnailService.class).to(ThumbnailServiceImpl.class).in(Singleton.class);
		bind(Uploader.class).in(Singleton.class);
	}
}
