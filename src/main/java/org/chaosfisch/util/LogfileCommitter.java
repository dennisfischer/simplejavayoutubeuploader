/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.chaosfisch.util.io.Request;
import org.chaosfisch.util.io.Request.Method;
import org.chaosfisch.util.io.RequestUtil;
import org.chaosfisch.youtubeuploader.models.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

public class LogfileCommitter {
	private static final String	COMMIT_URL	= "http://youtubeuploader.square7.ch/nightly/receiver.php";
	private static final Logger	logger		= LoggerFactory.getLogger(LogfileCommitter.class);
	private static final File	htmlFile	= new File(System.getProperty("user.home") + "/SimpleJavaYoutubeUploader/applog.html");

	public static void commit() {
		if (!htmlFile.exists()) {
			return;
		}
		final String html = getLogfile();
		if (html == null) {
			return;
		}

		final String uuid = getUniqueId();
		logger.info("UUID:" + uuid);

		final List<BasicNameValuePair> logfileParams = new ArrayList<BasicNameValuePair>();
		logfileParams.add(new BasicNameValuePair("uuid", uuid));
		logfileParams.add(new BasicNameValuePair("data", html));

		final HttpUriRequest request = new Request.Builder(COMMIT_URL, Method.POST)
				.headers(ImmutableMap.of("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8;"))
				.entity(new UrlEncodedFormEntity(logfileParams, Charset.forName("utf-8"))).buildHttpUriRequest();
		// Write the atomData to my webpage
		HttpResponse response = null;
		try {
			response = RequestUtil.execute(request);

		} catch (final IOException e) {
			logger.warn("Loginformationen konnten nicht übermittelt werden. {}", response != null ? response.getStatusLine() : "");
		} finally {
			if (response != null && response.getEntity() != null) {
				EntityUtils.consumeQuietly(response.getEntity());
			}
		}
	}

	private static String getLogfile() {
		if (htmlFile.exists()) {
			try {
				return Files.toString(htmlFile, Charset.forName("utf-8"));
			} catch (final IOException e) {
				return null;
			}
		}
		return null;
	}

	private static String getUniqueId() {
		Setting uuidSetting = Setting.findFirst("key = ?", "hidden.uuid");
		if (uuidSetting == null) {
			uuidSetting = Setting.createIt("key", "hidden.uuid", "value", UUID.randomUUID());
		}

		return uuidSetting.getString("value");
	}
}
