package org.chaosfisch.youtubeuploader.db.data;

import org.chaosfisch.util.TextUtil;

public enum Comment {
	ALLOWED("commentlist.allowed"), MODERATED("commentlist.moderated"), DENIED("commentlist.denied"), FRIENDS_ONLY(
			"commentlist.friendsonly");

	private String	i18n;

	private Comment(final String i18n) {
		this.i18n = i18n;
	}

	@Override
	public String toString() {
		return TextUtil.getString(i18n);
	}
}
