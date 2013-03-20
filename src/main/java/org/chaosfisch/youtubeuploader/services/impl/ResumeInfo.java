/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.services.impl;

public class ResumeInfo {

	public final Long	nextByteToUpload;
	public final String	videoId;

	ResumeInfo(final long nextByteToUpload) {
		this.nextByteToUpload = nextByteToUpload;
		videoId = null;
	}

	ResumeInfo(final String videoId) {
		this.videoId = videoId;
		nextByteToUpload = null;
	}
}
