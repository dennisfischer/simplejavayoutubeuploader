/*
 * Copyright (c) 2014 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.thumbnail;

import com.google.common.io.Files;
import com.google.inject.Inject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import de.chaosfisch.google.account.Account;
import de.chaosfisch.google.account.IAccountService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ThumbnailServiceImpl implements IThumbnailService {

	@Inject
	IAccountService accountService;

	@Override
	public void upload(final File thumbnail, final String videoid, final Account account) throws FileNotFoundException, ThumbnailIOException {
		if (!thumbnail.exists()) {
			throw new FileNotFoundException(thumbnail.getName());
		}

		try {
			final HttpResponse<String> response = Unirest.post("https://www.googleapis.com/upload/youtube/v3/thumbnails/set?videoId=" + videoid + "&uploadType=resumable")
					.header("Authorization", accountService.getAuthentication(account).getHeader())
					.header("Content-Type", "application/octet-stream")
					.asString();

			final URL url = new URL(response.getHeaders().get("location"));
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "image/png");
			connection.setRequestProperty("Authorization", accountService.getAuthentication(account).getHeader());

			final OutputStream outputStream = connection.getOutputStream();
			outputStream.write(Files.toByteArray(thumbnail));
			outputStream.flush();
			outputStream.close();
			System.out.println(connection.getResponseMessage());
			System.out.println(connection.getResponseCode());

		} catch (final UnirestException | IOException e) {
			throw new ThumbnailIOException(e);
		}
	}
}
