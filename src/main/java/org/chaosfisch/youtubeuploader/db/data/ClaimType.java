package org.chaosfisch.youtubeuploader.db.data;

import org.chaosfisch.util.TextUtil;

public enum ClaimType {
	AUDIO_VISUAL("claimtype.audiovisual"), VISUAL("claimtype.visual"), AUDIO("claimtype.audio");

	private String	i18n;

	private ClaimType(final String i18n) {
		this.i18n = i18n;
	}

	@Override
	public String toString() {
		return TextUtil.getString(i18n);
	}
}
