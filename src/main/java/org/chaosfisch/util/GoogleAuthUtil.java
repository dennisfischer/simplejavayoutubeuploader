/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.chaosfisch.google.auth.AuthenticationException;
import org.chaosfisch.google.auth.RequestSigner;
import org.chaosfisch.util.io.Request;
import org.chaosfisch.util.io.Request.Method;
import org.chaosfisch.util.io.RequestUtil;
import org.chaosfisch.youtubeuploader.models.Account;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

public class GoogleAuthUtil {

	final Map<Long, String>		authtokens				= new WeakHashMap<Long, String>();
	@Inject RequestSigner		requestSigner;

	private static final String	CLIENT_LOGIN_URL		= "https://accounts.google.com/ClientLogin";
	private static final String	ISSUE_AUTH_TOKEN_URL	= "https://www.google.com/accounts/IssueAuthToken";

	public String getAuthToken(final Account account) throws AuthenticationException {

		if (!authtokens.containsKey(account.getLongId())) {

			final String clientLoginContent = _receiveToken(account);
			authtokens.put(account.getLongId(),
					clientLoginContent.substring(clientLoginContent.indexOf("Auth=") + 5, clientLoginContent.length()).trim());
		}
		return authtokens.get(account.getLongId());
	}

	private String _receiveToken(final Account account) throws AuthenticationException {
		return _receiveToken(account, "youtube", "SimpleJavaYoutubeUploader");

	}

	private String _receiveToken(final Account account, final String service, final String source) throws AuthenticationException {
		// STEP 1 CLIENT LOGIN
		final List<BasicNameValuePair> clientLoginRequestParams = new ArrayList<BasicNameValuePair>();
		clientLoginRequestParams.add(new BasicNameValuePair("Email", account.getString("name")));
		clientLoginRequestParams.add(new BasicNameValuePair("Passwd", account.getString("password")));
		clientLoginRequestParams.add(new BasicNameValuePair("service", service));
		clientLoginRequestParams.add(new BasicNameValuePair("PesistentCookie", "0"));
		clientLoginRequestParams.add(new BasicNameValuePair("accountType", "HOSTED_OR_GOOGLE"));
		clientLoginRequestParams.add(new BasicNameValuePair("source", source));

		final HttpUriRequest clientLoginRequest = new Request.Builder(CLIENT_LOGIN_URL, Method.POST)
				.headers(ImmutableMap.of("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8;"))
				.entity(new UrlEncodedFormEntity(clientLoginRequestParams, Charset.forName("utf-8"))).buildHttpUriRequest();

		requestSigner.sign(clientLoginRequest);

		HttpResponse clientLoginResponse = null;
		HttpEntity clientLoginEntity = null;
		try {
			clientLoginResponse = RequestUtil.execute(clientLoginRequest);
			clientLoginEntity = clientLoginResponse.getEntity();
			if (clientLoginResponse.getStatusLine().getStatusCode() != 200) {
				throw new AuthenticationException(String.format("Authentication failed --> %s", clientLoginResponse.getStatusLine()
						.toString()));
			}

			return EntityUtils.toString(clientLoginEntity, Charset.forName("UTF-8"));
		} catch (final IOException e) {
			throw new AuthenticationException(String.format("Authentication failed --> %s",
					clientLoginResponse != null ? clientLoginResponse.getStatusLine().toString() : "response is null"), e);
		} finally {
			if (clientLoginEntity != null) {
				EntityUtils.consumeQuietly(clientLoginEntity);
			}
		}
	}

	public String getAuthHeader(final Account account) throws AuthenticationException {
		if (authtokens.containsKey(account.getLongId())) {
			return String.format("GoogleLogin auth=%s", authtokens.get(account.getLongId()));
		} else {
			return String.format("GoogleLogin auth=%s", getAuthToken(account));
		}
	}

	public boolean verifyAccount(final Account account) {
		try {
			_receiveToken(account);
		} catch (final AuthenticationException e) {
			return false;
		}
		return true;
	}

	public String getLoginContent(final Account account, final String redirectUrl) throws AuthenticationException, IOException {
		return tokenAuthContent(redirectUrl, issueAuthToken(_receiveToken(account, "gaia", "googletalk")));

	}

	private String tokenAuthContent(final String redirectUrl, final String issueAuthTokenContent) throws UnsupportedEncodingException,
			ClientProtocolException, IOException {
		String content = null;
		HttpEntity tokenAuthEntity = null;
		// STEP 3 TOKEN AUTH
		try {
			final String tokenAuthUrl = String.format(
					"https://www.google.com/accounts/TokenAuth?auth=%s&service=youtube&continue=%s&source=googletalk",
					URLEncoder.encode(issueAuthTokenContent, "UTF-8"), URLEncoder.encode(redirectUrl, "UTF-8"));

			final HttpUriRequest tokenAuthRequest = new Request.Builder(tokenAuthUrl, Method.GET)

			.buildHttpUriRequest();
			final HttpResponse tokenAuthResponse = RequestUtil.execute(tokenAuthRequest);
			tokenAuthEntity = tokenAuthResponse.getEntity();
			content = EntityUtils.toString(tokenAuthEntity, Charset.forName("UTF-8"));
		} finally {
			if (tokenAuthEntity != null) {
				EntityUtils.consumeQuietly(tokenAuthEntity);
			}
		}
		return content;
	}

	private String issueAuthToken(final String clientLoginContent) throws ClientProtocolException, IOException {
		// STEP 2 ISSUE AUTH TOKEN
		String issueAuthTokenContent = null;
		HttpEntity issueAuthTokenEntity = null;
		try {
			final String sid = clientLoginContent.substring(clientLoginContent.indexOf("SID=") + 4, clientLoginContent.indexOf("LSID="));
			final String lsid = clientLoginContent.substring(clientLoginContent.indexOf("LSID=") + 5, clientLoginContent.indexOf("Auth="));

			final List<BasicNameValuePair> issueAuthTokenParams = new ArrayList<BasicNameValuePair>();
			issueAuthTokenParams.add(new BasicNameValuePair("SID", sid));
			issueAuthTokenParams.add(new BasicNameValuePair("LSID", lsid));
			issueAuthTokenParams.add(new BasicNameValuePair("service", "gaia"));
			issueAuthTokenParams.add(new BasicNameValuePair("Session", "true"));
			issueAuthTokenParams.add(new BasicNameValuePair("source", "googletalk"));

			final HttpUriRequest issueAuthTokenRequest = new Request.Builder(ISSUE_AUTH_TOKEN_URL, Method.POST)
					.headers(ImmutableMap.of("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8;"))
					.entity(new UrlEncodedFormEntity(issueAuthTokenParams, Charset.forName("utf-8"))).buildHttpUriRequest();

			final HttpResponse issueAuthTokenResponse = RequestUtil.execute(issueAuthTokenRequest);
			issueAuthTokenEntity = issueAuthTokenResponse.getEntity();
			issueAuthTokenContent = EntityUtils.toString(issueAuthTokenEntity, Charset.forName("UTF-8"));
		} finally {
			if (issueAuthTokenEntity != null) {
				EntityUtils.consumeQuietly(issueAuthTokenEntity);
			}
		}
		return issueAuthTokenContent;
	}
}
