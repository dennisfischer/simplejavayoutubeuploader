/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.google.auth;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.chaosfisch.util.InputStreams;

public class GoogleAuthorization implements Authorization
{
	private final static String	CLIENT_LOGIN_URL	= "https://www.google.com/accounts/ClientLogin";
	private final static String	SERVICE				= "youtube";
	private final static String	SOURCE				= "CHAOSFISCH Google API";
	private String				authToken;

	public GoogleAuthorization(final String email, final String password) throws AuthenticationException
	{
		try
		{

			final HttpParams httpParams = new BasicHttpParams();
			httpParams.setParameter("Email", email);
			httpParams.setParameter("Passwd", password);
			httpParams.setParameter("service", SERVICE);
			httpParams.setParameter("source", SOURCE);

			final HttpPost httpPost = new HttpPost(CLIENT_LOGIN_URL);
			httpPost.setParams(httpParams);

			final HttpClient httpClient = new DefaultHttpClient();
			final HttpResponse httpResponse = httpClient.execute(httpPost);

			if (httpResponse.getStatusLine().getStatusCode() == 200)
			{
				final String body = InputStreams.toString(httpResponse.getEntity().getContent());
				authToken = body.substring(body.indexOf("Auth=") + 5, body.length());
			} else
			{
				throw new AuthenticationException(String.format("Authentication failed -> %s", httpResponse.getStatusLine().toString()));
			}

		} catch (final MalformedURLException e)
		{
			e.printStackTrace();
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public String getAuthHeader()
	{
		return String.format("GoogleLogin auth=%s", getAuthToken());
	}

	private String getAuthToken()
	{
		return authToken;
	}

}
