/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.playlist;

import de.chaosfisch.exceptions.ErrorCode;

public enum PlaylistCode implements ErrorCode {
	SYNCH_IO_ERROR(501), SYNCH_UNEXPECTED_RESPONSE_CODE(502), ADD_PLAYLIST_IO_ERROR(503), ADD_VIDEO_IO_ERROR(504), ADD_PLAYLIST_UNEXPECTED_RESPONSE_CODE(505),;

	private final int number;

	PlaylistCode(final int number) {
		this.number = number;
	}

	@Override
	public int getNumber() {
		return number;
	}

}
