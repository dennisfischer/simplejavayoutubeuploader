/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.upload;

public abstract class AbstractUploadService implements IUploadService {

	private final Uploader uploader;

	protected AbstractUploadService(final Uploader uploader) {
		this.uploader = uploader;
	}

	@Override
	public void startUploading() {
		uploader.run();
	}

	@Override
	public void stopUploading() {
		uploader.shutdown();
	}

	@Override
	public void startStarttimeCheck() {
		uploader.runStarttimeChecker();
	}

	@Override
	public void stopStarttimeCheck() {
		uploader.stopStarttimeChecker();
	}

	@Override
	public void abort(final UploadModel upload) {
		uploader.abort(upload);
	}
}
