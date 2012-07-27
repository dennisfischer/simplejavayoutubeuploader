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
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.APIData;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.I18nSupport;
import org.chaosfisch.youtubeuploader.util.logger.InjectLogger;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.GoogleApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 14.04.12
 * Time: 12:39
 * To change this template use File | Settings | File Templates.
 */
public class GooglePlusSocialProvider implements ISocialProvider
{
	private Token accessToken;
	private final OAuthService oAuthService = new ServiceBuilder().provider(GoogleApi.class).scope("https://www.googleapis.com/auth/plus.me")  //NON-NLS
			.apiKey(APIData.GOOGLE_APIKEY).apiSecret(APIData.GOOGLE_APISECRET).build();
	@InjectLogger private Logger logger;
	private static final String GOOGLE_ACCESS_TOKEN = "onGoogleAccessToken"; //NON-NLS

	@Override public Token getAccessToken()
	{
		return accessToken;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override public void setAccessToken(final Token accessToken)
	{
		this.accessToken = accessToken;
	}

	@Override public void authenticate()
	{
		if (accessToken != null) {
			return;
		}

		final Token requestToken = oAuthService.getRequestToken();
		try {
			Desktop.getDesktop().browse(new URI(oAuthService.getAuthorizationUrl(requestToken)));
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (URISyntaxException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

		final String verifier = JOptionPane.showInputDialog(null, I18nSupport.message("googleOAuth.code.label"), I18nSupport.message("googleOAuth.account.label"), JOptionPane.INFORMATION_MESSAGE);

		if (verifier != null) {
			accessToken = oAuthService.getAccessToken(requestToken, new Verifier(verifier));
		}
	}

	@Override public void publish(final String message)
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override public boolean hasValidAccessToken()
	{
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
