package org.chaosfisch.google.auth;

import org.chaosfisch.exceptions.ErrorCode;

public enum AuthCode implements ErrorCode {
	AUTH_IO_ERROR(301), RESPONSE_NOT_200(302),

	;

	private final int	number;

	private AuthCode(final int number) {
		this.number = number;
	}

	@Override
	public int getNumber() {
		return number;
	}
}
