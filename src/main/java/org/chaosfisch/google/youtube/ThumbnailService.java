/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.google.youtube;

import org.chaosfisch.exceptions.SystemException;

import java.io.File;

public interface ThumbnailService {
	/**
	 * Uploads a thumbnail via the YouTube webpage. This may fail!
	 *
	 * @param content
	 * 		the HTML content of the video edit page
	 * @param thumbnail
	 * 		the thumbnail file
	 * @param videoid
	 * 		the matching videoid
	 *
	 * @return Integer containing the "thumbnail_id"
	 *
	 * @throws SystemException
	 * 		if thumbnail upload fails. This may be due to failure during request or incorrect response.
	 */
	Integer upload(String content, File thumbnail, String videoid) throws SystemException;
}
