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

import de.chaosfisch.google.account.Account;

import java.io.File;
import java.io.FileNotFoundException;

public interface IThumbnailService {
	/**
	 * Uploads a thumbnail via the YouTube webpage. This may fail!
	 *
	 * @param thumbnail
	 * 		the thumbnail file
	 * @param videoid
	 * 		the matching videoid
	 * @param account
	 * 		the matching videoid
	 */
	void upload(File thumbnail, String videoid, Account account) throws FileNotFoundException, ThumbnailIOException;
}
