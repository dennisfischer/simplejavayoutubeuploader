/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.thumbnail;

import de.chaosfisch.youtube.account.AccountModel;

import java.io.File;

public interface IThumbnailService {
	/**
	 * Uploads a thumbnail via the YouTube webpage. This may fail!
	 *
	 * @param thumbnail    the thumbnail file
	 * @param videoid      the matching videoid
	 * @param accountModel the matching videoid
	 */
	void upload(File thumbnail, String videoid, AccountModel accountModel) throws ThumbnailIOException;
}
