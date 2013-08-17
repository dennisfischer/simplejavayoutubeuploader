/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.upload.metadata;

import java.io.Serializable;

public class Social implements Serializable {

	private static final long serialVersionUID = 1996880624554193297L;
	private int     id;
	private boolean facebook;
	private boolean twitter;
	private String  message;
	private int     version;

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public boolean isFacebook() {
		return facebook;
	}

	public void setFacebook(final boolean facebook) {
		this.facebook = facebook;
	}

	public boolean isTwitter() {
		return twitter;
	}

	public void setTwitter(final boolean twitter) {
		this.twitter = twitter;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Social)) {
			return false;
		}

		final Social social = (Social) obj;

		return id == social.id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(final int version) {
		this.version = version;
	}
}
