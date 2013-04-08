package org.chaosfisch.youtubeuploader.db.data;

import org.chaosfisch.util.TextUtil;

public enum Visibility {
	PUBLIC("visibilitylist.public"), UNLISTED("visibilitylist.unlisted"), PRIVATE("visibilitylist.private"), SCHEDULED(
			"visibilitylist.scheduled");

	private String	i18n;

	private Visibility(final String i18n) {
		this.i18n = i18n;
	}

	@Override
	public String toString() {
		return TextUtil.getString(i18n);
	}

}
