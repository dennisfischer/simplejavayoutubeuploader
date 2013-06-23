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
import de.chaosfisch.exceptions.SystemException;
import de.chaosfisch.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.chaosfisch.google.account.Account;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientLogin implements IGoogleLogin {

	private final        HashMap<Integer, String> authtokens           = new HashMap<>(10);
	private static final String                   CLIENT_LOGIN_URL     = "https://accounts.google.com/ClientLogin";
	private static final String                   ISSUE_AUTH_TOKEN_URL = "https://www.google.com/accounts/IssueAuthToken";

	private final RequestSigner         requestSigner;
	private final RequestBuilderFactory requestBuilderFactory;

	@Inject
	public ClientLogin(final RequestSigner requestSigner, final RequestBuilderFactory requestBuilderFactory) {
		this.requestSigner = requestSigner;
		this.requestBuilderFactory = requestBuilderFactory;
	}

	private String getAuthToken(final Account account) throws SystemException {

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
		final ArrayList<BasicNameValuePair> clientRequestParams = new ArrayList<>(6);
		clientRequestParams.add(new BasicNameValuePair("Email", account.getName()));
		clientRequestParams.add(new BasicNameValuePair("Passwd", account.getPassword()));
		clientRequestParams.add(new BasicNameValuePair("service", service));
		clientRequestParams.add(new BasicNameValuePair("PesistentCookie", "0"));
		clientRequestParams.add(new BasicNameValuePair("accountType", "HOSTED_OR_GOOGLE"));
		clientRequestParams.add(new BasicNameValuePair("source", source));

		final IRequest clientLoginRequest = requestBuilderFactory.create(CLIENT_LOGIN_URL)
				.post(new UrlEncodedFormEntity(clientRequestParams, Charsets.UTF_8))
				.headers(ImmutableMap.of("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8;"))
				.sign(requestSigner)
				.build();

		try (final IResponse response = clientLoginRequest.execute()) {
			if (200 != response.getStatusCode()) {
				throw new SystemException(AuthCode.RESPONSE_NOT_200).set("respons-code", response.getStatusCode());
			}

			return response.getContent();
		} catch (final HttpIOException e) {
			throw new SystemException(e, AuthCode.AUTH_IO_ERROR);
		}
	}

	@Override
	public String getAuthHeader(final Account account) throws SystemException {
		if (authtokens.containsKey(account.getId())) {
			return String.format("GoogleLogin auth=%s", authtokens.get(account.getId()));
		} else {
			return String.format("GoogleLogin auth=%s", getAuthToken(account));
		}
	}

	@Override
	public void verifyAccount(final Account account) throws SystemException {
		_receiveToken(account);
	}

	@Override
	public String getLoginContent(final Account account, final String redirectUrl) throws SystemException {
		return tokenAuthContent(redirectUrl, issueAuthToken(_receiveToken(account, "gaia", "googletalk")));

	}

	private String tokenAuthContent(final String redirectUrl, final String issueTokenContent) throws SystemException {
		// STEP 3 TOKEN AUTH
		try {
			final String tokenAuthUrl = String.format("https://www.google.com/accounts/TokenAuth?auth=%s&service=youtube&continue=%s&source=googletalk", URLEncoder
					.encode(issueTokenContent, Charsets.UTF_8.name()), URLEncoder.encode(redirectUrl, Charsets.UTF_8
					.name()));

			final IRequest tokenAuthRequest = requestBuilderFactory.create(tokenAuthUrl).get().build();

			try (final IResponse IResponse = tokenAuthRequest.execute()) {
				return IResponse.getContent();
			}
		} catch (UnsupportedEncodingException | HttpIOException e) {
			throw new SystemException(e, AuthCode.AUTH_IO_ERROR);
		}
	}

	private String issueAuthToken(final String clientLoginContent) throws SystemException {
		// STEP 2 ISSUE AUTH TOKEN
		final String sid = clientLoginContent.substring(clientLoginContent.indexOf("SID=") + 4, clientLoginContent.indexOf("LSID="));
		final String lsid = clientLoginContent.substring(clientLoginContent.indexOf("LSID=") + 5, clientLoginContent.indexOf("Auth="));

		final ArrayList<BasicNameValuePair> issueTokenParams = new ArrayList<>(5);
		issueTokenParams.add(new BasicNameValuePair("SID", sid));
		issueTokenParams.add(new BasicNameValuePair("LSID", lsid));
		issueTokenParams.add(new BasicNameValuePair("service", "gaia"));
		issueTokenParams.add(new BasicNameValuePair("Session", "true"));
		issueTokenParams.add(new BasicNameValuePair("source", "googletalk"));

		final IRequest issueTokenRequest = requestBuilderFactory.create(ISSUE_AUTH_TOKEN_URL)
				.post(new UrlEncodedFormEntity(issueTokenParams, Charset.forName("utf-8")))
				.headers(ImmutableMap.of("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8;"))
				.build();
		try (final IResponse IResponse = issueTokenRequest.execute()) {
			return IResponse.getContent();
		} catch (HttpIOException e) {
			throw new SystemException(e, AuthCode.AUTH_IO_ERROR);
		}
	}
}
