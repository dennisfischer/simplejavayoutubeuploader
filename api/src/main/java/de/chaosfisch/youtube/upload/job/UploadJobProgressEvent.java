/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.upload.job;

public class UploadJobProgressEvent {
	private long diffBytes;
	private long diffTime;
	private long time;
	private long totalBytesUploaded;

	public long getDiffBytes() {
		return diffBytes;
	}

	public long getDiffTime() {
		return diffTime;
	}

	public long getTotalBytesUploaded() {
		return totalBytesUploaded;
	}

	public long getTime() {
		return time;
	}

	public void setTime(final long diffTime) {
		this.diffTime = diffTime;
		time += diffTime;
	}

	public void setBytes(final long addBytes) {
		diffBytes = addBytes - totalBytesUploaded;
		totalBytesUploaded += diffBytes;
	}
}
