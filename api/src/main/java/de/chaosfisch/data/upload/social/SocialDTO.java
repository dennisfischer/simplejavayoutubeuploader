/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.data.upload.social;

public class SocialDTO {
	private String  uploadId;
	private String  message;
	private boolean facebook;
	private boolean twitter;
	private boolean gplus;

	public SocialDTO() {
	}

	public SocialDTO(final String uploadId, final String message, final boolean facebook, final boolean twitter, final boolean gplus) {
		this.uploadId = uploadId;
		this.message = message;
		this.facebook = facebook;
		this.twitter = twitter;
		this.gplus = gplus;
	}

	public String getUploadId() {
		return uploadId;
	}

	public void setUploadId(final String uploadId) {
		this.uploadId = uploadId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(final String message) {
		this.message = message;
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

	public boolean isGplus() {
		return gplus;
	}

	public void setGplus(final boolean gplus) {
		this.gplus = gplus;
	}
}
