/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.google.auth;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.chaosfisch.exceptions.SystemException;
import org.chaosfisch.util.http.Request;
import org.chaosfisch.util.http.RequestSigner;
import org.chaosfisch.util.http.Response;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Account;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientLogin {

	private final        HashMap<Integer, String> authtokens           = new HashMap<>(10);
	private static final String                   CLIENT_LOGIN_URL     = "https://accounts.google.com/ClientLogin";
	private static final String                   ISSUE_AUTH_TOKEN_URL = "https://www.google.com/accounts/IssueAuthToken";

	@Inject
	RequestSigner requestSigner;

	String getAuthToken(final Account account) throws SystemException {

		if (!authtokens.containsKey(account.getId())) {

			final String clientLoginContent = _receiveToken(account);
			authtokens.put(account.getId(), clientLoginContent.substring(clientLoginContent.indexOf("Auth=") + 5, clientLoginContent
					.length()).trim());
		}
		return authtokens.get(account.getId());
	}

	private String _receiveToken(final Account account) throws SystemException {
		return _receiveToken(account, "youtube", "SimpleJavaYoutubeUploader");

	}

	private String _receiveToken(final Account account, final String service, final String source) throws SystemException {
		// STEP 1 CLIENT LOGIN
		final ArrayList<BasicNameValuePair> clientLoginRequestParams = new ArrayList<>(6);
		clientLoginRequestParams.add(new BasicNameValuePair("Email", account.getName()));
		clientLoginRequestParams.add(new BasicNameValuePair("Passwd", account.getPassword()));
		clientLoginRequestParams.add(new BasicNameValuePair("service", service));
		clientLoginRequestParams.add(new BasicNameValuePair("PesistentCookie", "0"));
		clientLoginRequestParams.add(new BasicNameValuePair("accountType", "HOSTED_OR_GOOGLE"));
		clientLoginRequestParams.add(new BasicNameValuePair("source", source));

		final Request clientLoginRequest = new Request.Builder(CLIENT_LOGIN_URL).post(new UrlEncodedFormEntity(clientLoginRequestParams, Charsets.UTF_8))
				.headers(ImmutableMap.of("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8;"))
				.sign(requestSigner)
				.build();

		try (final Response response = clientLoginRequest.execute()) {
			final HttpEntity clientLoginEntity = response.getEntity();
			if (response.getStatusCode() != 200) {
				throw new SystemException(AuthCode.RESPONSE_NOT_200).set("respons-code", response.getStatusCode());
			}

			return EntityUtils.toString(clientLoginEntity, Charsets.UTF_8);
		} catch (final IOException e) {
			throw new SystemException(e, AuthCode.AUTH_IO_ERROR);
		}
	}

	public String getAuthHeader(final Account account) throws SystemException {
		if (authtokens.containsKey(account.getId())) {
			return String.format("GoogleLogin auth=%s", authtokens.get(account.getId()));
		} else {
			return String.format("GoogleLogin auth=%s", getAuthToken(account));
		}
	}

	public void verifyAccount(final Account account) throws SystemException {
		_receiveToken(account);
	}

	public String getLoginContent(final Account account, final String redirectUrl) throws SystemException, IOException {
		return tokenAuthContent(redirectUrl, issueAuthToken(_receiveToken(account, "gaia", "googletalk")));

	}

	private String tokenAuthContent(final String redirectUrl, final String issueAuthTokenContent) throws IOException, SystemException {
		// STEP 3 TOKEN AUTH
		try {
			final String tokenAuthUrl = String.format("https://www.google.com/accounts/TokenAuth?auth=%s&service=youtube&continue=%s&source=googletalk", URLEncoder
					.encode(issueAuthTokenContent, Charsets.UTF_8.name()), URLEncoder.encode(redirectUrl, Charsets.UTF_8
					.name()));

			final Request tokenAuthRequest = new Request.Builder(tokenAuthUrl).get().build();

			try (final Response response = tokenAuthRequest.execute()) {
				return response.getContent();
			}
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private String issueAuthToken(final String clientLoginContent) throws IOException {
		// STEP 2 ISSUE AUTH TOKEN
		final String sid = clientLoginContent.substring(clientLoginContent.indexOf("SID=") + 4, clientLoginContent.indexOf("LSID="));
		final String lsid = clientLoginContent.substring(clientLoginContent.indexOf("LSID=") + 5, clientLoginContent.indexOf("Auth="));

		final ArrayList<BasicNameValuePair> issueAuthTokenParams = new ArrayList<>(5);
		issueAuthTokenParams.add(new BasicNameValuePair("SID", sid));
		issueAuthTokenParams.add(new BasicNameValuePair("LSID", lsid));
		issueAuthTokenParams.add(new BasicNameValuePair("service", "gaia"));
		issueAuthTokenParams.add(new BasicNameValuePair("Session", "true"));
		issueAuthTokenParams.add(new BasicNameValuePair("source", "googletalk"));

		final Request issueAuthTokenRequest = new Request.Builder(ISSUE_AUTH_TOKEN_URL).post(new UrlEncodedFormEntity(issueAuthTokenParams, Charset
				.forName("utf-8")))
				.headers(ImmutableMap.of("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8;"))
				.build();
		try (final Response response = issueAuthTokenRequest.execute()) {
			return EntityUtils.toString(response.getEntity(), Charsets.UTF_8);
		}
	}
}