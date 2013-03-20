package org.chaosfisch.youtubeuploader.services.impl;

import org.chaosfisch.exceptions.ErrorCode;

public enum MetadataCode implements ErrorCode {
	BAD_REQUEST(201), LOCATION_MISSING(202), REQUEST_IO_ERROR(203), DEAD_END(204)

	;

	private final int	number;

	private MetadataCode(final int number) {
		this.number = number;
	}

	@Override
	public int getNumber() {
		return number;
	}

}
