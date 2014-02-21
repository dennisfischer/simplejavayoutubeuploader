/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.google.thumbnail;

import com.google.common.io.Files;
import de.chaosfisch.google.account.AccountModel;
import de.chaosfisch.google.account.IAccountService;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class YouTubeThumnbailService implements IThumbnailService {
	@Inject
	private final IAccountService accountService;

	public YouTubeThumnbailService(final IAccountService accountService) {
		this.accountService = accountService;
	}

	@Override
	public void upload(final File thumbnail, final String videoid, final AccountModel account) throws FileNotFoundException, ThumbnailIOException {
		if (!thumbnail.exists()) {
			throw new FileNotFoundException(thumbnail.getName());
		}

		try {
			final URL url = new URL("https://www.googleapis.com/upload/youtube/v3/thumbnails/set?videoId=" + videoid + "&uploadType=media");
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/octet-stream");
			connection.setRequestProperty("Authorization", accountService.getAuthentication(account).getHeader());

			final OutputStream outputStream = connection.getOutputStream();
			outputStream.write(Files.toByteArray(thumbnail));
			outputStream.flush();
			outputStream.close();
		} catch (final IOException e) {
			throw new ThumbnailIOException(e);
		}
	}
}
