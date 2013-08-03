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
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import de.chaosfisch.google.auth.Authentication;
import de.chaosfisch.http.*;
import de.chaosfisch.http.entity.Entity;
import de.chaosfisch.http.entity.EntityBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

public abstract class AbstractAccountService implements IAccountService {

	private static final int                      SC_OK                = 200;
	private final        HashMap<Integer, String> authtokens           = new HashMap<>(10);
	private static final String                   CLIENT_LOGIN_URL     = "https://accounts.google.com/ClientLogin";
	private static final String                   ISSUE_AUTH_TOKEN_URL = "https://www.google.com/accounts/IssueAuthToken";

	private final IRequestSigner        requestSigner;
	private final RequestBuilderFactory requestBuilderFactory;

	@Inject
	public AbstractAccountService(final IRequestSigner requestSigner, final RequestBuilderFactory requestBuilderFactory) {
		this.requestSigner = requestSigner;
		this.requestBuilderFactory = requestBuilderFactory;
	}

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
		final Entity entity = new EntityBuilder().charset(Charsets.UTF_8)
				.add("Email", account.getName())
				.add("Passwd", account.getPassword())
				.add("service", service)
				.add("PesistentCookie", "0")
				.add("accountType", "HOSTED_OR_GOOGLE")
				.add("source", source)
				.build();

		final IRequest clientLoginRequest = requestBuilderFactory.create(CLIENT_LOGIN_URL)
				.post(entity)
				.headers(ImmutableMap.of("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8;"))
				.sign(requestSigner)
				.build();

		try (final IResponse response = clientLoginRequest.execute()) {
			if (SC_OK != response.getStatusCode()) {
				throw new AuthenticationInvalidException(response.getStatusCode());
			}

			return response.getContent();
		} catch (final HttpIOException e) {
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

			final IRequest tokenAuthRequest = requestBuilderFactory.create(tokenAuthUrl).get().build();

			try (final IResponse response = tokenAuthRequest.execute()) {
				return response.getContent();
			}
		} catch (UnsupportedEncodingException | HttpIOException e) {
			throw new AuthenticationIOException(e);
		}
	}

	private String issueAuthToken(final String clientLoginContent) throws AuthenticationIOException {
		// STEP 2 ISSUE AUTH TOKEN
		final String sid = clientLoginContent.substring(clientLoginContent.indexOf("SID=") + 4, clientLoginContent.indexOf("LSID="));
		final String lsid = clientLoginContent.substring(clientLoginContent.indexOf("LSID=") + 5, clientLoginContent.indexOf("Auth="));

		final Entity entity = new EntityBuilder().charset(Charsets.UTF_8)
				.add("SID", sid)
				.add("LSID", lsid)
				.add("service", "gaia")
				.add("Session", "true")
				.add("source", "googletalk")
				.build();

		final IRequest issueTokenRequest = requestBuilderFactory.create(ISSUE_AUTH_TOKEN_URL)
				.post(entity)
				.headers(ImmutableMap.of("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8;"))
				.build();
		try (final IResponse response = issueTokenRequest.execute()) {
			return response.getContent();
		} catch (HttpIOException e) {
			throw new AuthenticationIOException(e);
		}
	}
}
