/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.uploader;

public class UploaderModule {

	protected void configure() {
	/*	try {
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
				encounter.register((InjectionListener<I>) eventBus::register);
			}
		});
	}

	private void mapServices() {
		bind(IMetadataService.class).to(AbstractMetadataService.class).in(Singleton.class);
		bind(IEnddirService.class).to(EnddirServiceImpl.class).in(Singleton.class);
		bind(IThumbnailService.class).to(ThumbnailServiceImpl.class).in(Singleton.class);
		bind(Uploader.class).in(Singleton.class);*/
	}
}
