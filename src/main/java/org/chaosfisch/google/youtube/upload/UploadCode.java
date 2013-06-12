/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.google.youtube.upload;

import org.chaosfisch.exceptions.ErrorCode;

public enum UploadCode implements ErrorCode {
	MAX_RETRIES_REACHED(1), UPLOAD_REPONSE_200(2), FILE_IO_ERROR(3), UPLOAD_RESPONSE_UNKNOWN(4), FILE_NOT_FOUND(5), LOGFILE_IO_ERROR(6), UPDATE_METADATA_IO_ERROR(7), PLAYLIST_IO_ERROR(8), USER_ABORT(9), UNKNOWN_ERROR(10);

	private final int number;

	UploadCode(final int number) {
		this.number = number;
	}

	@Override
	public int getNumber() {
		return number;
	}
}
