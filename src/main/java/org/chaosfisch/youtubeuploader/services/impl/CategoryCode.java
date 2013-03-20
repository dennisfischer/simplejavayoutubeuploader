package org.chaosfisch.youtubeuploader.services.impl;

import org.chaosfisch.exceptions.ErrorCode;

public enum CategoryCode implements ErrorCode {
	UNEXPECTED_RESPONSE_CODE(601), LOAD_IO_ERROR(602), CATGORIES_EMPTY(603);
	private final int	number;

	private CategoryCode(final int number) {
		this.number = number;
	}

	@Override
	public int getNumber() {
		return number;
	}
}
