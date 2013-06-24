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

	private Integer id;
	private Boolean facebook;
	private Boolean twitter;
	private String  message;

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public Boolean getFacebook() {
		return facebook;
	}

	public void setFacebook(final Boolean facebook) {
		this.facebook = facebook;
	}

	public Boolean getTwitter() {
		return twitter;
	}

	public void setTwitter(final Boolean twitter) {
		this.twitter = twitter;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}
}
