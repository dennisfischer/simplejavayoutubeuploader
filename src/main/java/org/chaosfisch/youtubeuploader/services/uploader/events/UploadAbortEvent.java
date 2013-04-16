/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader.services.uploader.events;

import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;

public class UploadAbortEvent {

	private final Upload upload;

	public UploadAbortEvent(final Upload upload) {
		this.upload = upload;
	}

	public Upload getUpload() {
		return upload;
	}
}
