/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.account;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import de.chaosfisch.google.GDATAConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

public abstract class AbstractAccountService implements IAccountService {

	private static final int                     SC_OK             = 200;
	private final        HashMap<Account, Token> authtokens        = new HashMap<>(3);
	private static final String                  REFRESH_TOKEN_URL = "https://accounts.google.com/o/oauth2/token";
	private static final String                  TOKEN_TEST_URL    = "https://www.googleapis.com/youtube/v3/activities?part=id&mine=true&maxResults=0";
	private static final Logger                  LOGGER            = LoggerFactory.getLogger(AbstractAccountService.class);

	private Token getAuthToken(final Account account) throws AuthenticationIOException {
		return getAuthToken(account, false);
	}

	private Token getAuthToken(final Account account, final boolean uncached) throws AuthenticationIOException {
		if (uncached || !authtokens.containsKey(account) || !authtokens.get(account).isValid()) {
			authtokens.put(account, _receiveToken(account));
		}
		return authtokens.get(account);
	}

	private Token _receiveToken(final Account account) throws AuthenticationIOException {
		try {
			final HttpResponse<JsonNode> response = Unirest.post(REFRESH_TOKEN_URL)
					.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8;")
					.field("grant_type", "refresh_token")
					.field("client_id", GDATAConfig.CLIENT_ID)
					.field("client_secret", GDATAConfig.CLIENT_SECRET)
					.field("refresh_token", account.getRefreshToken())
					.asJson();
			if (SC_OK != response.getCode()) {
				throw new AuthenticationInvalidException(response.getCode());
			}

			return new Token(response.getBody().getObject().getString("access_token"), response.getBody()
					.getObject()
					.getInt("expires_in"));
		} catch (final Exception e) {
			throw new AuthenticationIOException(e);
		}
	}

	@Override
	public Authentication getAuthentication(final Account account) {
		try {
			return new Authentication(getAuthToken(account).getToken());
		} catch (Exception e) {
			LOGGER.error("Auth invalid", e);
			return new Authentication();
		}
	}

	@Override
	public String getRefreshToken(final String code) throws AuthenticationIOException {

		try {
			final HttpResponse<JsonNode> response = Unirest.post(REFRESH_TOKEN_URL)
					.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8;")
					.field("grant_type", "authorization_code")
					.field("client_id", GDATAConfig.CLIENT_ID)
					.field("client_secret", GDATAConfig.CLIENT_SECRET)
					.field("redirect_uri", GDATAConfig.REDIRECT_URI)
					.field("code", code)
					.asJson();

			if (SC_OK != response.getCode()) {
				throw new AuthenticationInvalidException(response.getCode());
			}

			return response.getBody().getObject().getString("refresh_token");
		} catch (Exception e) {
			throw new AuthenticationIOException(e);
		}
	}

	@Override
	public boolean verifyAccount(final Account account) {
		try {
			getAuthToken(account, true);
			_testExtended(account);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	private void _testExtended(final Account account) throws AuthenticationIOException {
		final HttpResponse<String> response = Unirest.get(TOKEN_TEST_URL)
				.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8;")
				.header("Authorization", getAuthentication(account).getHeader())
				.asString();

		if (SC_OK != response.getCode()) {
			throw new AuthenticationIOException(String.format("Code %d during token test;\n%s", response.getCode(), response
					.getBody()));
		}
	}
}
