package org.chaosfisch.youtubeuploader.db.data;

import org.chaosfisch.util.TextUtil;

public enum ClaimOption {
	MONETIZE("claimoptions.monetize"), TRACK("claimoptions.track"), BLOCK("claimoptions.block");

	private String	i18n;

	private ClaimOption(final String i18n) {
		this.i18n = i18n;
	}

	@Override
	public String toString() {
		return TextUtil.getString(i18n);
	}
}
