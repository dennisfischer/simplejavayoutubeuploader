/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.auth;

import com.google.inject.Inject;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.account.IAccountService;
import de.chaosfisch.youtubeuploader.ApplicationData;
import org.apache.http.HttpRequest;

import java.net.HttpURLConnection;

public class GDataRequestSigner implements IGoogleRequestSigner {

	private final IAccountService accountService;
	private       Account         account;

	@Inject
	public GDataRequestSigner(final IAccountService accountService) {
		this.accountService = accountService;
	}

	@Override
	public void sign(final HttpRequest request) {
		request.addHeader("GData-Version", ApplicationData.GDATA_VERSION);
		request.addHeader("X-GData-Key", String.format("key=%s", ApplicationData.DEVELOPER_KEY));

		final Authentication authentication = getAuthentication();
		if (authentication.isValid()) {
			request.addHeader("Authorization", authentication.getHeader());
		}
	}

	@Override
	public void sign(final HttpURLConnection request) {
		request.setRequestProperty("GData-Version", ApplicationData.GDATA_VERSION);
		request.setRequestProperty("X-GData-Key", String.format("key=%s", ApplicationData.DEVELOPER_KEY));

		final Authentication authentication = getAuthentication();
		if (authentication.isValid()) {
			request.setRequestProperty("Authorization", authentication.getHeader());
		}
	}

	private Authentication getAuthentication() {
		return accountService.getAuthentication(account);
	}

	@Override
	public void setAccount(final Account account) {
		this.account = account;
	}
}
