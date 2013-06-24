/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.http;

import org.apache.http.HttpRequest;

import java.net.HttpURLConnection;

public interface IRequestSigner {
	/**
	 * Signs a HttpRequest
	 *
	 * @param request
	 * 		HttpRequest to be signed
	 */
	void sign(HttpRequest request);

	/**
	 * Signs a HttpUrlConnection
	 *
	 * @param request
	 * 		HttpURLConnection to be signed
	 */
	void sign(HttpURLConnection request);
}
