/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.services.socialize;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.chaosfisch.util.logger.InjectLogger;
import org.chaosfisch.youtubeuploader.APIData;
import org.chaosfisch.youtubeuploader.I18nHelper;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

public class TwitterSocialProvider
{
	private static final String		TWITTER_ACCESS_TOKEN	= "onTwitterAccessToken";
	private Token					accessToken;
	@InjectLogger private Logger	logger;
	private final OAuthService		oAuthService			= new ServiceBuilder().provider(TwitterApi.class).apiKey(APIData.TWITTER_APIKEY)
																	.apiSecret(APIData.TWITTER_APISECRET).build();

	public void authenticate()
	{
		if (accessToken != null) { return; }

		final Token requestToken = oAuthService.getRequestToken();
		try
		{
			Desktop.getDesktop().browse(new URI(oAuthService.getAuthorizationUrl(requestToken)));
		} catch (final IOException e)
		{
			e.printStackTrace();
		} catch (final URISyntaxException e)
		{
			e.printStackTrace();
		}

		final String verifier = JOptionPane.showInputDialog(null, I18nHelper.message("label.acceptcode"), I18nHelper.message("label.twitteroauth"),
				JOptionPane.INFORMATION_MESSAGE);

		if (verifier != null)
		{
			accessToken = oAuthService.getAccessToken(requestToken, new Verifier(verifier));
		}
	}

	public Token getAccessToken()
	{
		return accessToken;
	}

	public boolean hasValidAccessToken()
	{
		if (accessToken == null) { return false; }
		final OAuthRequest oAuthRequest = new OAuthRequest(Verb.GET, "http://api.twitter.com/1/help/test.json");
		oAuthService.signRequest(accessToken, oAuthRequest);
		try
		{
			final Response response = oAuthRequest.send();
			if (response.getCode() == 200) { return true; }
		} catch (final RuntimeException ignored)
		{
			return false;
		}
		return false;
	}

	public void publish(final String message)
	{
		if (accessToken == null) { return; }
		final OAuthRequest oAuthRequest = new OAuthRequest(Verb.POST, "http://api.twitter.com/1/statuses/update.json");
		oAuthRequest.addBodyParameter("status", message);

		oAuthService.signRequest(accessToken, oAuthRequest);
		final Response response = oAuthRequest.send();
		if (response.getCode() != 200)
		{
			logger.fatal(String.format("Wrong response code: %d", response.getCode()));
			logger.fatal(response.getBody());
		}
	}

	public void setAccessToken(final Token accessToken)
	{
		this.accessToken = accessToken;
	}
}
