package org.chaosfisch.io.http;

import org.chaosfisch.exceptions.ErrorCode;

public enum HttpCode implements ErrorCode {
	IO_ERROR(801);
	private final int	number;

	private HttpCode(final int number) {
		this.number = number;
	}

	@Override
	public int getNumber() {
		return number;
	}
}
