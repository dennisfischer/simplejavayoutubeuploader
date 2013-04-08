package org.chaosfisch.youtubeuploader.db.data;

import org.chaosfisch.util.TextUtil;

public enum License {
	YOUTUBE("licenselist.youtube", "youtube"), CREATIVE_COMMONS("licenselist.cc", "cc");

	private String	i18n;
	private String	metaIdentifier;

	private License(final String i18n, final String metaIdentifier) {
		this.i18n = i18n;
		this.metaIdentifier = metaIdentifier;
	}

	@Override
	public String toString() {
		return TextUtil.getString(i18n);
	}

	public String getMetaIdentifier() {
		return metaIdentifier;
	}
}
