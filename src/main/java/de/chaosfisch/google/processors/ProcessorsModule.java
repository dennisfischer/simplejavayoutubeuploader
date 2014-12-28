/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.google.processors;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import de.chaosfisch.google.youtube.upload.UploadPostProcessor;
import de.chaosfisch.google.youtube.upload.UploadPreProcessor;

public class ProcessorsModule extends AbstractModule {
	@Override
	protected void configure() {
		final Multibinder<UploadPreProcessor> preProcessorMultibinder = Multibinder.newSetBinder(binder(), UploadPreProcessor.class);
		preProcessorMultibinder.addBinding().to(PlaylistPreProcessor.class);
		final Multibinder<UploadPostProcessor> postProcessorMultibinder = Multibinder.newSetBinder(binder(), UploadPostProcessor.class);
		postProcessorMultibinder.addBinding().to(MetadataUpdatePostProcessor.class);
		postProcessorMultibinder.addBinding().to(PlaylistPostProcessor.class);
		postProcessorMultibinder.addBinding().to(ThumbnailPostProcessor.class);
		postProcessorMultibinder.addBinding().to(ExtendedGDataPostProcessor.class);
		postProcessorMultibinder.addBinding().to(EnddirPostProcessor.class);
		postProcessorMultibinder.addBinding().to(SchedulePostProcessor.class);
	}
}

