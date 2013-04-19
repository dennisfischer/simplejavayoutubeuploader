/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.util;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.chaosfisch.exceptions.SystemException;
import org.chaosfisch.io.http.Request;
import org.chaosfisch.io.http.Response;
import org.chaosfisch.youtubeuploader.SimpleJavaYoutubeUploader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.prefs.Preferences;

public final class LogfileCommitter {
	private static final String SETTING_UUID = "hidden.uuid";
	private static final String COMMIT_URL   = "http://youtubeuploader.square7.ch/nightly/receiver.php";
	private static final Logger logger       = LoggerFactory.getLogger(LogfileCommitter.class);
	private static final File   htmlFile     = new File(System.getProperty("user.home") + "/SimpleJavaYoutubeUploader/applog.html");

	public static void commit() throws SystemException {
		if (!htmlFile.exists()) {
			return;
		}
		final String html = getLogfile();
		if (html == null) {
			return;
		}

		final String uuid = getUniqueId();
		logger.info("UUID:" + uuid);

		final List<BasicNameValuePair> logfileParams = new ArrayList<>();
		logfileParams.add(new BasicNameValuePair("uuid", uuid));
		logfileParams.add(new BasicNameValuePair("data", html));

		final Request request = new Request.Builder(COMMIT_URL).post(new UrlEncodedFormEntity(logfileParams, Charsets.UTF_8))
				.headers(ImmutableMap.of("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8;"))
				.build();
		// Write the atomData to my webpage

		try (final Response response = request.execute()) {
		} catch (final IOException e) {
			throw SystemException.wrap(e, LogfileCode.IO_ERROR);
		}
	}

	private static String getLogfile() {
		if (htmlFile.exists()) {
			try {
				return Files.toString(htmlFile, Charsets.UTF_8);
			} catch (final IOException e) {
				logger.error(e.getMessage(), e);
				return null;
			}
		}
		return null;
	}

	private static String getUniqueId() {
		final Preferences prefs = Preferences.userNodeForPackage(SimpleJavaYoutubeUploader.class);
		String uuid = prefs.get(SETTING_UUID, null);
		if (uuid == null) {
			uuid = UUID.randomUUID().toString();
			prefs.put(SETTING_UUID, uuid);
		}
		return uuid;
	}
}
