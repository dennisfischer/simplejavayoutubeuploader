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
import com.mashape.unirest.http.Unirest;
import de.chaosfisch.google.Config;
import de.chaosfisch.google.auth.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.HashMap;

public abstract class AbstractAccountService implements IAccountService {

	private static final int                      SC_OK                = 200;
	private final        HashMap<Integer, String> authtokens           = new HashMap<>(10);
	private static final String                   CLIENT_LOGIN_URL     = "https://accounts.google.com/ClientLogin";
	private static final String                   ISSUE_AUTH_TOKEN_URL = "https://www.google.com/accounts/IssueAuthToken";
	private static final Logger                   logger               = LoggerFactory.getLogger(AbstractAccountService.class);

	private String getAuthToken(final Account account) throws AuthenticationIOException, AuthenticationInvalidException {

		if (!authtokens.containsKey(account.getId())) {

			final String clientLoginContent = _receiveToken(account);
			authtokens.put(account.getId(), clientLoginContent.substring(clientLoginContent.indexOf("Auth=") + 5, clientLoginContent
					.length()).trim());
		}
		return authtokens.get(account.getId());
	}

	private String _receiveToken(final Account account) throws AuthenticationIOException, AuthenticationInvalidException {
		return _receiveToken(account, "youtube", "SimpleJavaYoutubeUploader");

	}

	private String _receiveToken(final Account account, final String service, final String source) throws AuthenticationIOException, AuthenticationInvalidException {
		// STEP 1 CLIENT LOGIN

		try {
			final HttpResponse<String> response = Unirest.post(CLIENT_LOGIN_URL)
					.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8;")
					.header("GData-Version", Config.GDATA_V2)
					.header("X-GData-Key", "key=" + Config.DEVELOPER_KEY)
					.field("Email", account.getName())
					.field("Passwd", account.getPassword())
					.field("service", service)
					.field("PesistentCookie", "0")
					.field("accountType", "HOSTED_OR_GOOGLE")
					.field("source", source)
					.asString();

			if (SC_OK != response.getCode()) {
				throw new AuthenticationInvalidException(response.getCode());
			}

			return response.getBody();
		} catch (final Exception e) {
			throw new AuthenticationIOException(e);
		}
	}

	@Override
	public Authentication getAuthentication(final Account account) {
		try {
			final String header;
			if (authtokens.containsKey(account.getId())) {
				header = authtokens.get(account.getId());
			} else {
				header = getAuthToken(account);
			}
			return new Authentication(header);
		} catch (Exception e) {
			logger.error("Auth invalid", e);
			return new Authentication();
		}
	}

	@Override
	public void verifyAccount(final Account account) throws AuthenticationInvalidException, AuthenticationIOException {
		_receiveToken(account);
	}

	@Override
	public String getLoginContent(final Account account, final String redirectUrl) throws AuthenticationIOException, AuthenticationInvalidException {
		return tokenAuthContent(redirectUrl, issueAuthToken(_receiveToken(account, "gaia", "googletalk")));

	}

	private String tokenAuthContent(final String redirectUrl, final String issueTokenContent) throws AuthenticationIOException {
		// STEP 3 TOKEN AUTH
		try {
			final String tokenAuthUrl = String.format("https://www.google.com/accounts/TokenAuth?auth=%s&service=youtube&continue=%s&source=googletalk", URLEncoder
					.encode(issueTokenContent, Charsets.UTF_8.name()), URLEncoder.encode(redirectUrl, Charsets.UTF_8
					.name()));
			final HttpResponse<String> response = Unirest.get(tokenAuthUrl)
					.header("GData-Version", Config.GDATA_V2)
					.header("X-GData-Key", "key=" + Config.DEVELOPER_KEY)
					.asString();

			return response.getBody();
		} catch (Exception e) {
			throw new AuthenticationIOException(e);
		}
	}

	private String issueAuthToken(final String clientLoginContent) throws AuthenticationIOException {
		// STEP 2 ISSUE AUTH TOKEN
		final String sid = clientLoginContent.substring(clientLoginContent.indexOf("SID=") + 4, clientLoginContent.indexOf("LSID="));
		final String lsid = clientLoginContent.substring(clientLoginContent.indexOf("LSID=") + 5, clientLoginContent.indexOf("Auth="));

		try {
			final HttpResponse<String> response = Unirest.post(ISSUE_AUTH_TOKEN_URL)
					.header("GData-Version", Config.GDATA_V2)
					.header("X-GData-Key", "key=" + Config.DEVELOPER_KEY)
					.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8;")
					.field("SID", sid)
					.field("LSID", lsid)
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
