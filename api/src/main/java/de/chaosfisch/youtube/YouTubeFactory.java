/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpBackOffIOExceptionHandler;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import de.chaosfisch.youtube.account.AccountModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

public final class YouTubeFactory {

	private static final JsonFactory                    jsonFactory   = new GsonFactory();
	private static final HttpTransport                  httpTransport = new NetHttpTransport();
	private static final Logger                         LOGGER        = LoggerFactory.getLogger(YouTubeFactory.class);
	private static final HashMap<AccountModel, YouTube> services      = new HashMap<>(1);
	private static YouTube youTubeDefault;

	private YouTubeFactory() {
	}

	public static YouTube getYouTube(final AccountModel accountModel) {

		if (!services.containsKey(accountModel)) {
			final Credential credential = buildCredential(accountModel);
			final YouTube youTube = buildYouTube(credential);
			services.put(accountModel, youTube);
		}
		return services.get(accountModel);
	}

	private static Credential buildCredential(final AccountModel accountModel) {
		final Credential credential = new GoogleCredential.Builder()
				.setJsonFactory(jsonFactory)
				.setTransport(httpTransport)
				.setClientSecrets(GDataConfig.CLIENT_ID, GDataConfig.CLIENT_SECRET)
				.setRequestInitializer(request -> request.setIOExceptionHandler(new HttpBackOffIOExceptionHandler(new ExponentialBackOff())))
				.addRefreshListener(new CredentialRefreshListener() {
					@Override
					public void onTokenResponse(final Credential credential, final TokenResponse tokenResponse) throws IOException {
						LOGGER.info("Token refreshed");
					}

					@Override
					public void onTokenErrorResponse(final Credential credential, final TokenErrorResponse tokenErrorResponse) throws IOException {
						LOGGER.error("Token refresh error {}", tokenErrorResponse.toPrettyString());
					}
				})
				.build();
		credential.setRefreshToken(accountModel.getRefreshToken());
		return credential;
	}

	private static YouTube buildYouTube(final Credential credential) {
		return new YouTube.Builder(httpTransport, jsonFactory, credential)
				.setApplicationName("simple-java-youtube-uploader")
				.build();
	}

	public static YouTube getDefault() {
		if (null == youTubeDefault) {
			youTubeDefault = new YouTube.Builder(httpTransport, jsonFactory, request -> {
				request.setInterceptor(request1 -> {
					request1.getUrl().set("key", GDataConfig.ACCESS_KEY);
				});
			}).setApplicationName("simple-java-youtube-uploader").build();
		}
		return youTubeDefault;
	}
}
