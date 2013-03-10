package org.chaosfisch.youtubeuploader.services.youtube.impl;

import org.chaosfisch.exceptions.ErrorCode;

public enum ResumeCode implements ErrorCode {
	IO_ERROR(401), UNEXPECTED_RESPONSE_CODE(402);
	private final int	number;

	private ResumeCode(final int number) {
		this.number = number;
	}

	@Override
	public int getNumber() {
		return number;
	}
}
