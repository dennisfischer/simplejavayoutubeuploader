/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.data.account.cookies;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.net.HttpCookie;
import java.net.URI;

public class CookieDTO {
	@NotNull
	private String  accountId;
	@NotNull
	private String  name;
	@NotNull
	private String  value;
	@NotNull
	private String  domain;
	private boolean discard;
	@NotNull
	private String  path;
	private long    maxAge;
	private boolean secure;
	private int     version;

	public CookieDTO() {
	}


	public CookieDTO(@NotNull final String accountId, @NotNull final HttpCookie cookie) {
		this.accountId = accountId;
		name = cookie.getName();
		value = cookie.getValue();
		domain = cookie.getDomain();
		discard = cookie.getDiscard();
		maxAge = cookie.getMaxAge();
		path = cookie.getPath();
		secure = cookie.getSecure();
		version = cookie.getVersion();
	}

	public CookieDTO(@NotNull final String accountId, @NotNull final String name, @NotNull final String value, @NotNull final String domain, final boolean discard, @NotNull final String path, final long maxAge, final boolean secure, final int version) {
		this.accountId = accountId;
		this.name = name;
		this.value = value;
		this.domain = domain;
		this.discard = discard;
		this.path = path;
		this.maxAge = maxAge;
		this.secure = secure;
		this.version = version;
	}

	public URI getURI() {
		return URI.create(domain);
	}

	public HttpCookie getCookie() {
		final HttpCookie cookie = new HttpCookie(name, value);
		cookie.setDiscard(discard);
		cookie.setDomain(domain);
		cookie.setPath(path);
		cookie.setMaxAge(maxAge);
		cookie.setSecure(secure);
		cookie.setVersion(version);
		return cookie;
	}

	@NotNull
	public String getName() {
		return name;
	}

	public void setName(@NotNull final String name) {
		this.name = name;
	}

	@NotNull
	public String getValue() {
		return value;
	}

	public void setValue(@NotNull final String value) {
		this.value = value;
	}

	@NotNull
	public String getDomain() {
		return domain;
	}

	public void setDomain(@NotNull final String domain) {
		this.domain = domain;
	}

	public boolean isDiscard() {
		return discard;
	}

	public void setDiscard(final boolean discard) {
		this.discard = discard;
	}

	@NotNull
	public String getPath() {
		return path;
	}

	public void setPath(@NotNull final String path) {
		this.path = path;
	}

	public long getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(final long maxAge) {
		this.maxAge = maxAge;
	}

	public boolean isSecure() {
		return secure;
	}

	public void setSecure(final boolean secure) {
		this.secure = secure;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(final int version) {
		this.version = version;
	}

	@Override
	public int hashCode() {
		int result = accountId.hashCode();
		result = 31 * result + name.hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CookieDTO)) {
			return false;
		}

		final CookieDTO cookieDTO = (CookieDTO) obj;
		return accountId.equals(cookieDTO.accountId) && name.equals(cookieDTO.name);
	}

	@Override
	@NonNls
	public String toString() {
		return "CookieDTO{" +
				"accountId='" + accountId + '\'' +
				", name='" + name + '\'' +
				", value='" + value + '\'' +
				", domain='" + domain + '\'' +
				", discard=" + discard +
				", path='" + path + '\'' +
				", maxAge=" + maxAge +
				", secure=" + secure +
				", version=" + version +
				'}';
	}

	@NotNull
	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(@NotNull final String accountId) {
		this.accountId = accountId;
	}
}
