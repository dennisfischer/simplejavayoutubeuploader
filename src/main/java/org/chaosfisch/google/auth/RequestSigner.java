/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.google.auth;

import java.net.HttpURLConnection;

import org.apache.http.HttpRequest;

public interface RequestSigner
{
	void sign(HttpRequest request);

	void sign(HttpURLConnection request);

	void signWithAuthorization(HttpRequest request, String authHeader);

	void signWithAuthorization(HttpURLConnection request, String authHeader);
}
