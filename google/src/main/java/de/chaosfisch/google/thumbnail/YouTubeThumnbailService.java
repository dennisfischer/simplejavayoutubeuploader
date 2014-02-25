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

import com.google.api.client.http.InputStreamContent;
import de.chaosfisch.google.account.AccountModel;
import de.chaosfisch.google.auth.GoogleAuthProvider;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;


public class YouTubeThumnbailService implements IThumbnailService {
	private final GoogleAuthProvider googleAuthProvider;

	@Inject
	public YouTubeThumnbailService(final GoogleAuthProvider googleAuthProvider) {
		this.googleAuthProvider = googleAuthProvider;
	}

	@Override
	public void upload(final File thumbnail, final String videoid, final AccountModel accountModel) throws FileNotFoundException, ThumbnailIOException {
		if (!thumbnail.exists()) {
			throw new FileNotFoundException(thumbnail.getName());
		}

		try (final InputStream is = Files.newInputStream(thumbnail.toPath())) {
			googleAuthProvider.getYouTubeService(accountModel)
					.thumbnails()
					.set(videoid, new InputStreamContent("application/octet-stream", is));
		} catch (final IOException e) {
			throw new ThumbnailIOException(e);
		}
	}
}
