/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.google.auth;

import org.apache.http.HttpRequest;
import org.chaosfisch.http.RequestSigner;
import org.chaosfisch.youtubeuploader.ApplicationData;

import java.net.HttpURLConnection;

public class GDataRequestSigner implements RequestSigner {

	private String authHeader;

	@Override
	public void sign(final HttpRequest request) {
		request.addHeader("GData-Version", ApplicationData.GDATA_VERSION);
		request.addHeader("X-GData-Key", String.format("key=%s", ApplicationData.DEVELOPER_KEY));
		if (null != authHeader) {
			request.addHeader("Authorization", authHeader);
		}
	}

	public void setAuthHeader(final String authHeader) {
		this.authHeader = authHeader;
	}

	@Override
	public void sign(final HttpURLConnection request) {
		request.setRequestProperty("GData-Version", ApplicationData.GDATA_VERSION);
		request.setRequestProperty("X-GData-Key", String.format("key=%s", ApplicationData.DEVELOPER_KEY));
		if (null != authHeader) {
			request.setRequestProperty("Authorization", authHeader);
		}
	}
}
