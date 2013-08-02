/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.thumbnail;

import java.io.File;
import java.io.FileNotFoundException;

public interface IThumbnailService {
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
	 */
	Integer upload(String content, File thumbnail, String videoid) throws FileNotFoundException, ThumbnailResponseException, ThumbnailJsonException;
}
