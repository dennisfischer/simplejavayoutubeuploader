/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.util.http;

import org.apache.http.HttpRequest;

import java.net.HttpURLConnection;

public interface RequestSigner {
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

	/**
	 * Signs a HttpRequest and adds authHeader
	 *
	 * @param request
	 * 		HttpRequest to be signed
	 * @param authHeader
	 * 		AuthHeader to be applied
	 */
	void signWithAuthorization(HttpRequest request, String authHeader);

	/**
	 * Signs a HttpURLConnection and adds authHeader
	 *
	 * @param request
	 * 		HttpURLConnection to be signed
	 * @param authHeader
	 * 		AuthHeader to be aplied
	 */
	void signWithAuthorization(HttpURLConnection request, String authHeader);
}
