/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.google.youtube.thumbnail;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.inject.Inject;
import de.chaosfisch.google.YouTubeProvider;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.account.IAccountService;

import java.io.*;

public class ThumbnailServiceImpl implements IThumbnailService {

	@Inject
	IAccountService accountService;

	@Inject
	YouTubeProvider youTubeProvider;

	@Override
	public void upload(final File thumbnail, final String videoid, final Account account) throws FileNotFoundException, ThumbnailIOException {
		if (!thumbnail.exists()) {
			throw new FileNotFoundException(thumbnail.getName());
		}

		final YouTube youTube = youTubeProvider.setAccount(account).get();
		try (final BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(thumbnail))) {

			youTube.thumbnails()
					.set(videoid, new InputStreamContent("application/octet-stream", bufferedInputStream))
					.execute();
		} catch (final IOException e) {
			throw new ThumbnailIOException(e);
		}
	}
}
