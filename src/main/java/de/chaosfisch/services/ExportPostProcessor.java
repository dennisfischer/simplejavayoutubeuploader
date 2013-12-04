/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.services;

import com.google.common.base.Charsets;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.youtube.upload.Upload;
import de.chaosfisch.google.youtube.upload.UploadPostProcessor;
import de.chaosfisch.uploader.ApplicationData;
import de.chaosfisch.util.DateTimeTypeConverter;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ExportPostProcessor implements UploadPostProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExportPostProcessor.class);

	@Override
	public Upload process(final Upload upload) {
		LOGGER.info("Running export postprocessor");
		final Gson gson = new GsonBuilder().setPrettyPrinting()
				.addSerializationExclusionStrategy(new ExclusionStrategy() {
					@Override
					public boolean shouldSkipField(final FieldAttributes f) {
						return f.getDeclaringClass().isAssignableFrom(Account.class);
					}

					@Override
					public boolean shouldSkipClass(final Class<?> clazz) {
						return clazz.isAssignableFrom(Account.class);
					}
				})
				.registerTypeAdapter(DateTime.class, new DateTimeTypeConverter())
				.serializeNulls()
				.create();

		try {
			final Upload copy = gson.fromJson(gson.toJson(upload), Upload.class);

			try {
				Files.createDirectories(Paths.get(ApplicationData.DATA_DIR + "/uploads/"));
				Files.write(Paths.get(String.format("%s/uploads/%s.json", ApplicationData.DATA_DIR, copy.getVideoid())), gson
						.toJson(copy)
						.getBytes(Charsets.UTF_8));
			} catch (IOException e) {
				LOGGER.warn("Couldn't write json log.", e);
			}
			LOGGER.info("Finished export postprocessor");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return upload;
	}
}
