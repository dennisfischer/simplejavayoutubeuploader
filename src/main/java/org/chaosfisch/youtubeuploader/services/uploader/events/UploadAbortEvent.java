package org.chaosfisch.youtubeuploader.services.uploader.events;

import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Upload;

public class UploadAbortEvent {

	private final Upload	upload;

	public UploadAbortEvent(final Upload upload) {
		this.upload = upload;
	}

	public Upload getUpload() {
		return upload;
	}
}
