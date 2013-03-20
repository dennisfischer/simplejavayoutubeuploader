package org.chaosfisch.youtubeuploader.services.impl;

import org.chaosfisch.exceptions.ErrorCode;

public enum ThumbnailCode implements ErrorCode {
	FILE_NOT_FOUND(101), UPLOAD_RESPONSE(102), UPLOAD_JSON(103);
	private final int	number;

	private ThumbnailCode(final int number) {
		this.number = number;
	}

	@Override
	public int getNumber() {
		return number;
	}
}
