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

import de.chaosfisch.data.account.cookies.CookieDTO;
import de.chaosfisch.data.account.fields.FieldDTO;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AccountDTO {
	@NotNull
	private final List<FieldDTO>  fields;
	@NotNull
	private final List<CookieDTO> cookies;
	@NotNull
	private       String          youtubeId;
	@NotNull
	private       String          name;
	@NotNull
	private       String          email;
	@NotNull
	private       String          refreshToken;
	@NotNull
	private       String          type;

	public AccountDTO() {
		fields = new ArrayList<>(0);
		cookies = new ArrayList<>(0);
	}

	public AccountDTO(@NotNull final String youtubeId, @NotNull final String name, @NotNull final String email, @NotNull final String refreshToken, @NotNull final String type) {
		this(youtubeId, name, email, refreshToken, type, new ArrayList<>(0), new ArrayList<>(0));
	}

	public AccountDTO(@NotNull final String youtubeId, @NotNull final String name, @NotNull final String email, @NotNull final String refreshToken, @NotNull final String type, @NotNull final List<FieldDTO> fields, @NotNull final List<CookieDTO> cookies) {
		this.youtubeId = youtubeId;
		this.name = name;
		this.email = email;
		this.refreshToken = refreshToken;
		this.type = type;
		this.fields = fields;
		this.cookies = cookies;
	}

	@NotNull
	public String getYoutubeId() {
		return youtubeId;
	}

	public void setYoutubeId(@NotNull final String youtubeId) {
		this.youtubeId = youtubeId;
	}

	@NotNull
	public String getName() {
		return name;
	}

	public void setName(@NotNull final String name) {
		this.name = name;
	}

	@NotNull
	public String getEmail() {
		return email;
	}

	public void setEmail(@NotNull final String email) {
		this.email = email;
	}

	@NotNull
	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(@NotNull final String refreshToken) {
		this.refreshToken = refreshToken;
	}

	@NotNull
	public String getType() {
		return type;
	}

	public void setType(@NotNull final String type) {
		this.type = type;
	}

	@NotNull
	public List<FieldDTO> getFields() {
		return fields;
	}

	@NotNull
	public List<CookieDTO> getCookies() {
		return cookies;
	}

	@Override
	public int hashCode() {
		int result = youtubeId.hashCode();
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
				that.refreshToken) && type.equals(that.type) && youtubeId.equals(that.youtubeId);

	}

	@Override
	@NonNls
	public String toString() {
		return "AccountDTO{" +
				"youtubeId='" + youtubeId + '\'' +
				", name='" + name + '\'' +
				", email='" + email + '\'' +
				", refreshToken='" + refreshToken + '\'' +
				", type=" + type +
				", fields=" + fields +
				", cookies=" + cookies +
				'}';
	}
}
