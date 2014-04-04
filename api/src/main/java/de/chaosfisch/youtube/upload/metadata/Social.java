/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.upload.metadata;

import de.chaosfisch.data.upload.social.SocialDTO;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class Social {

	private final SimpleBooleanProperty facebook = new SimpleBooleanProperty();
	private final SimpleBooleanProperty gplus    = new SimpleBooleanProperty();
	private final SimpleStringProperty  message = new SimpleStringProperty();
	private final SimpleBooleanProperty twitter = new SimpleBooleanProperty();

	public Social(final SocialDTO socialDTO) {
		message.set(socialDTO.getMessage());
		facebook.set(socialDTO.isFacebook());
		twitter.set(socialDTO.isTwitter());
		gplus.set(socialDTO.isGplus());
	}

	public Social() {
	}

	public String getMessage() {
		return message.get();
	}

	public void setMessage(final String message) {
		this.message.set(message);
	}

	public SimpleStringProperty messageProperty() {
		return message;
	}

	public boolean getFacebook() {
		return facebook.get();
	}

	public void setFacebook(final boolean facebook) {
		this.facebook.set(facebook);
	}

	public SimpleBooleanProperty facebookProperty() {
		return facebook;
	}

	public boolean getTwitter() {
		return twitter.get();
	}

	public void setTwitter(final boolean twitter) {
		this.twitter.set(twitter);
	}

	public SimpleBooleanProperty twitterProperty() {
		return twitter;
	}

	public boolean getGplus() {
		return gplus.get();
	}

	public void setGplus(final boolean gplus) {
		this.gplus.set(gplus);
	}

	public SimpleBooleanProperty gplusProperty() {
		return gplus;
	}
}
