/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Dennis Fischer
 ******************************************************************************/

package org.chaosfisch.youtubeuploader.services.socialize.providers;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.chaosfisch.google.request.HTTP_STATUS;
import org.chaosfisch.util.logger.InjectLogger;
import org.chaosfisch.youtubeuploader.APIData;
import org.chaosfisch.youtubeuploader.services.socialize.OAuthHTTPDServer;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

public class FacebookSocialProvider implements ISocialProvider
{
	private static final Token	EMPTY_TOKEN				= null;
	private Token				accessToken;
	private final OAuthService	oAuthService			= new ServiceBuilder().provider(FacebookApi.class).scope("publish_stream")
																.callback("http://localhost:8080/oauth").apiKey(APIData.FACEBOOK_APIKEY)
																.apiSecret(APIData.FACEBOOK_APISECRET).build();
	@InjectLogger
	private Logger				logger;
	private static final String	FACEBOOK_ACCES_TOKEN	= "onFacebookAccessToken";
	OAuthHTTPDServer			oAuthHTTPDServer;

	@Override
	public void publish(final String message)
	{
		if (accessToken == null) { return; }

		if (!hasValidAccessToken())
		{
			refreshAccessToken();
		}
		final OAuthRequest oAuthRequest = new OAuthRequest(Verb.POST, "https://graph.facebook.com/me/feed");
		oAuthRequest.addBodyParameter("message", message);
		oAuthRequest.addBodyParameter("link", extractUrl(message));

		oAuthService.signRequest(accessToken, oAuthRequest);
		final Response response = oAuthRequest.send();
		if (response.getCode() != HTTP_STATUS.OK.getCode())
		{
			logger.warn(String.format("Wrong response code: %d", response.getCode()));
			logger.warn(response.getBody());
		}
	}

	private void refreshAccessToken()
	{
		final String url = new StringBuilder().append("https://graph.facebook.com/oauth/access_token?client_id=").append(APIData.FACEBOOK_APIKEY)
				.append("&client_secret=").append(APIData.FACEBOOK_APISECRET).append("&grant_type=fb_exchange_token&fb_exchange_token=")
				.append(accessToken.getToken()).toString();
	}

	@Override
	public void authenticate()
	{
		if ((accessToken != null) && hasValidAccessToken()) { return; }
		try
		{
			Desktop.getDesktop().browse(new URI(oAuthService.getAuthorizationUrl(FacebookSocialProvider.EMPTY_TOKEN)));
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (URISyntaxException e)
		{
			e.printStackTrace();
		}
		try
		{
			logger.info("Facebook-Server started.");
			oAuthHTTPDServer = new OAuthHTTPDServer(8080);

			synchronized (oAuthHTTPDServer)
			{
				oAuthHTTPDServer.wait(60000);
			}
			if (oAuthHTTPDServer.getCode() != null)
			{
				accessToken = oAuthService.getAccessToken(null, new Verifier(oAuthHTTPDServer.getCode()));
			}
			logger.info("Facebook-Server stopped.");
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public Token getAccessToken()
	{
		return accessToken;
	}

	@Override
	public void setAccessToken(final Token accessToken)
	{
		this.accessToken = accessToken;
	}

	@Override
	public boolean hasValidAccessToken()
	{
		if (accessToken == null) { return false; }
		final OAuthRequest oAuthRequest = new OAuthRequest(Verb.GET, "https://graph.facebook.com/me");
		oAuthService.signRequest(accessToken, oAuthRequest);
		try
		{
			final Response response = oAuthRequest.send();
			if (response.getCode() == HTTP_STATUS.OK.getCode()) { return true; }
		} catch (RuntimeException ignored)
		{
			return false;
		}
		return false;
	}

	private String extractUrl(final String value)
	{
		final String urlPattern = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
		final Pattern p = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE);
		final Matcher m = p.matcher(value);
		if (m.find()) { return new String(value.substring(m.start(0), m.end(0))); }
		return "";
	}
}
