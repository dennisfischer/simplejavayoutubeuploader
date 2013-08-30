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

import com.google.common.base.Charsets;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import de.chaosfisch.google.GDATAConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.HashMap;

public abstract class AbstractAccountService implements IAccountService {

	private static final int                     SC_OK                = 200;
	private static final String                  TOKENINFO_URL        = "https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=%s";
	private final        HashMap<Account, Token> authtokens           = new HashMap<>(3);
	private static final String                  REFRESH_TOKEN_URL    = "https://accounts.google.com/o/oauth2/token";
	private static final String                  ISSUE_AUTH_TOKEN_URL = "https://www.google.com/accounts/IssueAuthToken";
	private static final Logger                  logger               = LoggerFactory.getLogger(AbstractAccountService.class);

	private Token getAuthToken(final Account account) throws AuthenticationIOException {
		if (!authtokens.containsKey(account) || !authtokens.get(account).isValid()) {
			authtokens.put(account, _receiveToken(account));
		}
		return authtokens.get(account);
	}

	private Token _receiveToken(final Account account) throws AuthenticationIOException {
		try {
			final HttpResponse<JsonNode> response = Unirest.post(REFRESH_TOKEN_URL)
					.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8;")
					.header("GData-Version", GDATAConfig.GDATA_V2)
					.header("X-GData-Key", "key=" + GDATAConfig.DEVELOPER_KEY)
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
			logger.error("Auth invalid", e);
			return new Authentication();
		}
	}

	@Override
	public String getRefreshToken(final String code) throws AuthenticationIOException {

		try {
			final HttpResponse<JsonNode> response = Unirest.post(REFRESH_TOKEN_URL)
					.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8;")
					.header("GData-Version", GDATAConfig.GDATA_V2)
					.header("X-GData-Key", "key=" + GDATAConfig.DEVELOPER_KEY)
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
	public void verifyAccount(final Account account) throws AuthenticationIOException {
		try {
			final HttpResponse<String> response = Unirest.get(String.format(TOKENINFO_URL, account.getRefreshToken()))
					.asString();
			if (SC_OK != response.getCode()) {
				throw new AuthenticationInvalidException(response.getCode());
			}
		} catch (Exception e) {
			throw new AuthenticationIOException(e);
		}
	}

	@Override
	public String getLoginContent(final Account account, final String redirectUrl) throws AuthenticationIOException {
		return tokenAuthContent(redirectUrl, issueAuthToken(account));
	}

	private String tokenAuthContent(final String redirectUrl, final String issueTokenContent) throws AuthenticationIOException {
		// STEP 3 TOKEN AUTH
		try {
			final String tokenAuthUrl = String.format("https://www.google.com/accounts/TokenAuth?auth=%s&service=youtube&continue=%s&source=googletalk", URLEncoder
					.encode(issueTokenContent, Charsets.UTF_8.name()), URLEncoder.encode(redirectUrl, Charsets.UTF_8
					.name()));
			final HttpResponse<String> response = Unirest.get(tokenAuthUrl)
					.header("GData-Version", GDATAConfig.GDATA_V2)
					.header("X-GData-Key", "key=" + GDATAConfig.DEVELOPER_KEY)
					.asString();

			return response.getBody();
		} catch (Exception e) {
			throw new AuthenticationIOException(e);
		}
	}

	private String issueAuthToken(final Account account) throws AuthenticationIOException {
		// STEP 2 ISSUE AUTH TOKEN
		try {
			final HttpResponse<String> response = Unirest.post(ISSUE_AUTH_TOKEN_URL)
					.header("GData-Version", GDATAConfig.GDATA_V2)
					.header("X-GData-Key", "key=" + GDATAConfig.DEVELOPER_KEY)
					.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8;")
					.field("SID", account.getSID())
					.field("LSID", account.getLSID())
					.field("service", "gaia")
					.field("Session", "true")
					.field("source", "googletalk")
					.asString();

			return response.getBody();
		} catch (Exception e) {
			throw new AuthenticationIOException(e);
		}
	}
}
