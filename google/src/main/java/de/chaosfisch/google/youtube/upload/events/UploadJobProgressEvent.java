/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.upload.events;

import de.chaosfisch.google.youtube.upload.Upload;

public class UploadJobProgressEvent {
	private long diffBytes;
	private long diffTime;
	private final long fileSize;
	private Upload upload;
	private long time;
	private long totalBytesUploaded;
	public boolean failed;
	public boolean done;

	public UploadJobProgressEvent(final Upload upload, final long fileSize) {
		this.upload = upload;
		this.fileSize = fileSize;
	}

	public long getDiffBytes() {
		return diffBytes;
	}

	public long getDiffTime() {
		return diffTime;
	}

	public long getFileSize() {
		return fileSize;
	}

	public Upload getUpload() {
		return upload;
	}

	public long getTotalBytesUploaded() {
		return totalBytesUploaded;
	}

	public long getTime() {
		return time;
	}

	public void setBytes(final long addBytes) {
		diffBytes = addBytes - totalBytesUploaded;
		totalBytesUploaded += diffBytes;
	}

	public void setTime(final long diffTime) {
		this.diffTime = diffTime;
		time += diffTime;
	}

	public void setUpload(final Upload upload) {
		this.upload = upload;
	}
}
