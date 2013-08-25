/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import de.chaosfisch.google.youtube.upload.IUploadJobFactory;

public class GoogleModule extends AbstractModule {
	@Override
	protected void configure() {
		install(new FactoryModuleBuilder().build(IUploadJobFactory.class));
		bind(HttpTransport.class).toInstance(new NetHttpTransport());
		bind(JsonFactory.class).toInstance(new GsonFactory());
		bind(YouTube.class).toProvider(YouTubeProvider.class).in(Singleton.class);
	}
}
