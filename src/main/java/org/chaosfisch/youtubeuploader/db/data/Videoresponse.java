package org.chaosfisch.youtubeuploader.db.data;

import org.chaosfisch.util.TextUtil;

public enum Videoresponse {
	ALLOWED("videoresponselist.allowed"), MODERATED("videoresponselist.moderated"), DENIED("videoresponselist.denied");

	private String	i18n;

	private Videoresponse(final String i18n) {
		this.i18n = i18n;
	}

	@Override
	public String toString() {
		return TextUtil.getString(i18n);
	}
}
