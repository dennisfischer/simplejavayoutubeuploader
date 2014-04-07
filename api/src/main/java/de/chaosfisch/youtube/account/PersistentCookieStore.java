/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.account;

import de.chaosfisch.data.account.CookieDTO;

import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class PersistentCookieStore implements CookieStore {

	private final CookieStore store;

	public PersistentCookieStore() {
		// get the default in memory cookie store
		store = new CookieManager().getCookieStore();
	}

	public List<CookieDTO> getSerializeableCookies(final String accountId) {
		final List<CookieDTO> cookies = new ArrayList<>(store.getCookies().size());

		for (final HttpCookie cookie : store.getCookies()) {
			final CookieDTO cookieDTO = new CookieDTO(accountId, cookie);
			cookies.add(cookieDTO);
		}
		return cookies;
	}

	@Override
	public void add(final URI uri, final HttpCookie cookie) {
		store.remove(uri, cookie);
		store.add(uri, cookie);
	}

	@Override
	public List<HttpCookie> get(final URI uri) {
		return store.get(uri);
	}

	@Override
	public List<HttpCookie> getCookies() {
		return store.getCookies();
	}

	public void setCookies(final List<CookieDTO> cookies) {
		for (final CookieDTO cookie : cookies) {
			add(cookie.getURI(), cookie.getCookie());
		}
	}

	@Override
	public List<URI> getURIs() {
		return store.getURIs();
	}

	@Override
	public boolean remove(final URI uri, final HttpCookie cookie) {
		return store.remove(uri, cookie);
	}

	@Override
	public boolean removeAll() {
		return store.removeAll();
	}
}