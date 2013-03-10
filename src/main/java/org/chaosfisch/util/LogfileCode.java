package org.chaosfisch.util;

import org.chaosfisch.exceptions.ErrorCode;

public enum LogfileCode implements ErrorCode {
	IO_ERROR(701);

	private final int	number;

	private LogfileCode(final int number) {
		this.number = number;
	}

	@Override
	public int getNumber() {
		return number;
	}

}
