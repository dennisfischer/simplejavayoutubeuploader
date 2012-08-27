/*
 * Copyright (c) 2012, Dennis Fischer
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.chaosfisch.youtubeuploader.plugins.socializeplugin.services.providers;

import org.apache.log4j.Logger;
import org.chaosfisch.google.request.HTTP_STATUS;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.APIData;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.services.OAuthHTTPDServer;
import org.chaosfisch.youtubeuploader.util.logger.InjectLogger;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 14.04.12
 * Time: 12:38
 * To change this template use File | Settings | File Templates.
 */
public class FacebookSocialProvider implements ISocialProvider
{
	private static final Token EMPTY_TOKEN = null;
	private Token accessToken;
	private final OAuthService oAuthService = new ServiceBuilder().provider(FacebookApi.class).scope("publish_stream").callback("http://localhost:8080/oauth")  //NON-NLS
			.apiKey(APIData.FACEBOOK_APIKEY).apiSecret(APIData.FACEBOOK_APISECRET).build();
	@InjectLogger private Logger logger;
	private static final String FACEBOOK_ACCES_TOKEN = "onFacebookAccessToken"; //NON-NLS
	OAuthHTTPDServer oAuthHTTPDServer;

	@Override
	public void publish(final String message)
	{
		if (accessToken == null) {
			return;
		}

		if (!hasValidAccessToken()) {
			refreshAccessToken();
		}
		final OAuthRequest oAuthRequest = new OAuthRequest(Verb.POST, "https://graph.facebook.com/me/feed"); //NON-NLS
		oAuthRequest.addBodyParameter("message", message); //NON-NLS
		oAuthRequest.addBodyParameter("link", extractUrl(message)); //NON-NLS

		oAuthService.signRequest(accessToken, oAuthRequest);
		final Response response = oAuthRequest.send();
		if (response.getCode() != HTTP_STATUS.OK.getCode()) {
			logger.warn(String.format("Wrong response code: %d", response.getCode()));//NON-NLS
			logger.warn(response.getBody());
		}
	}

	private void refreshAccessToken()
	{
		final String url = new StringBuilder().append("https://graph.facebook.com/oauth/access_token?client_id=").append(APIData.FACEBOOK_APIKEY).append("&client_secret=").append(
				APIData.FACEBOOK_APISECRET).append("&grant_type=fb_exchange_token&fb_exchange_token=").append(accessToken.getToken()).toString();
	}

	@Override
	public void authenticate()
	{
		if ((accessToken != null) && hasValidAccessToken()) {
			return;
		}
		try {
			Desktop.getDesktop().browse(new URI(oAuthService.getAuthorizationUrl(FacebookSocialProvider.EMPTY_TOKEN)));
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (URISyntaxException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		try {
			logger.info("Facebook-Server started.");//NON-NLS
			oAuthHTTPDServer = new OAuthHTTPDServer(8080);

			//noinspection SynchronizeOnNonFinalField
			synchronized (oAuthHTTPDServer) {
				oAuthHTTPDServer.wait(60000);
			}
			if (oAuthHTTPDServer.getCode() != null) {
				accessToken = oAuthService.getAccessToken(null, new Verifier(oAuthHTTPDServer.getCode()));
			}
			logger.info("Facebook-Server stopped.");//NON-NLS
		} catch (InterruptedException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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

	@Override public boolean hasValidAccessToken()
	{
		if (accessToken == null) {
			return false;
		}
		final OAuthRequest oAuthRequest = new OAuthRequest(Verb.GET, "https://graph.facebook.com/me"); //NON-NLS
		oAuthService.signRequest(accessToken, oAuthRequest);
		try {
			final Response response = oAuthRequest.send();
			if (response.getCode() == HTTP_STATUS.OK.getCode()) {
				return true;
			}
		} catch (RuntimeException ignored) {
			return false;
		}
		return false;
	}

	private String extractUrl(final String value)
	{
		final String urlPattern = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)"; //NON-NLS
		final Pattern p = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE);
		final Matcher m = p.matcher(value);
		if (m.find()) {
			return new String(value.substring(m.start(0), m.end(0)));
		}
		return "";
	}
}
