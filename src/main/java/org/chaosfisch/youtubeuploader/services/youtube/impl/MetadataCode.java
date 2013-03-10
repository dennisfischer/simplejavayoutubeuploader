package org.chaosfisch.youtubeuploader.services.youtube.impl;

import org.chaosfisch.exceptions.ErrorCode;

public enum MetadataCode implements ErrorCode {
	BAD_REQUEST(201), LOCATION_MISSING(202), REQUEST_IO_ERROR(203),

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
