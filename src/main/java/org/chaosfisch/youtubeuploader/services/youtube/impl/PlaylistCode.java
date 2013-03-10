package org.chaosfisch.youtubeuploader.services.youtube.impl;

import org.chaosfisch.exceptions.ErrorCode;

public enum PlaylistCode implements ErrorCode {
	SYNCH_IO_ERROR(501), SYNCH_UNEXPECTED_RESPONSE_CODE(502), ADD_PLAYLIST_IO_ERROR(503), ADD_VIDEO_IO_ERROR(504), ADD_PLAYLIST_UNEXPECTED_RESPONSE_CODE(
			505),

	;

	private final int	number;

	private PlaylistCode(final int number) {
		this.number = number;
	}

	@Override
	public int getNumber() {
		return number;
	}

}
