package org.chaosfisch.youtubeuploader.models;

import org.chaosfisch.youtubeuploader.I18nHelper;

public enum AccountsType {

	YOUTUBE("enum.youtube"), FACEBOOK("enum.facebook"), TWITTER("enum.twitter"), GOOGLEPLUS("enum.googleplus");

	private final String	name;

	AccountsType(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return I18nHelper.message(name);
	}
}
