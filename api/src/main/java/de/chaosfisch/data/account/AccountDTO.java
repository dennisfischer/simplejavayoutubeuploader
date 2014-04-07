/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.data.account;

import java.util.ArrayList;
import java.util.List;

public class AccountDTO {

	private String accountId;

	private List<FieldDTO>  fields;
	private List<CookieDTO> cookies;
	private String          name;
	private String          email;

	private String refreshToken;

	private String type;

	public AccountDTO() {
		fields = new ArrayList<>(0);
		cookies = new ArrayList<>(0);
	}

	public AccountDTO(final String accountId, final String name, final String email, final String refreshToken, final String type) {
		this(accountId, name, email, refreshToken, type, new ArrayList<>(0), new ArrayList<>(0));
	}

	public AccountDTO(final String accountId, final String name, final String email, final String refreshToken, final String type, final List<FieldDTO> fields,
					  final List<CookieDTO> cookies) {
		this.accountId = accountId;
		this.name = name;
		this.email = email;
		this.refreshToken = refreshToken;
		this.type = type;
		this.fields = fields;
		this.cookies = cookies;
	}


	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(final String accountId) {
		this.accountId = accountId;
	}


	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}


	public String getEmail() {
		return email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}


	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(final String refreshToken) {
		this.refreshToken = refreshToken;
	}


	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}


	public List<FieldDTO> getFields() {
		return fields;
	}


	public List<CookieDTO> getCookies() {
		return cookies;
	}


	@Override
	public int hashCode() {
		int result = accountId.hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + email.hashCode();
		result = 31 * result + refreshToken.hashCode();
		result = 31 * result + type.hashCode();
		result = 31 * result + fields.hashCode();
		result = 31 * result + cookies.hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof AccountDTO)) {
			return false;
		}

		final AccountDTO that = (AccountDTO) obj;

		return cookies.equals(that.cookies) && email.equals(that.email) && fields.equals(that.fields) && name.equals(that.name) && refreshToken.equals(
				that.refreshToken) && type.equals(that.type) && accountId.equals(that.accountId);

	}

	@Override
	public String toString() {
		return "AccountDTO{" +
				"accountId='" + accountId + '\'' +
				", name='" + name + '\'' +
				", email='" + email + '\'' +
				", refreshToken='" + refreshToken + '\'' +
				", type=" + type +
				", fields=" + fields +
				", cookies=" + cookies +
				'}';
	}

	public void setFields(final List<FieldDTO> fields) {
		this.fields = fields;
	}

	public void setCookies(final List<CookieDTO> cookies) {
		this.cookies = cookies;
	}
}
