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

import com.google.inject.Inject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import de.chaosfisch.serialization.IJsonSerializer;

import java.io.File;
import java.io.FileNotFoundException;

public class ThumbnailServiceImpl implements IThumbnailService {

	private final IJsonSerializer jsonSerializer;

	@Inject
	public ThumbnailServiceImpl(final IJsonSerializer jsonSerializer) {
		this.jsonSerializer = jsonSerializer;
	}

	@Override
	public Integer upload(final String content, final File thumbnail, final String videoid) throws FileNotFoundException, ThumbnailResponseException, ThumbnailJsonException {

		if (!thumbnail.exists()) {
			throw new FileNotFoundException(thumbnail.getName());
		}

		final String search = "yt.setAjaxToken(\"my_thumbnail_post\", \"";
		final String sessiontoken = content.substring(content.indexOf(search) + search.length(), content.indexOf('\"', content
				.indexOf(search) + search.length()));

		try {
			final HttpResponse<String> response = Unirest.post("http://www.youtube.com/my_thumbnail_post")
					.field("video_id", videoid)
					.field("is_ajax", "1")
					.field("session_token", sessiontoken)
					.field("imagefile", thumbnail)
					.asString();

			final String json = response.getBody();
			try {
				return parseResponse(json);
			} catch (final Exception e) {
				throw new ThumbnailJsonException(json, e);
			}
		} catch (final Exception e) {
			throw new ThumbnailResponseException(e);
		}
	}

	private Integer parseResponse(final String json) {
		return jsonSerializer.fromJSON(json, Thumbnail.class).version;
	}
}
