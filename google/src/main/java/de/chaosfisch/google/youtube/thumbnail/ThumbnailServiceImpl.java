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

import com.google.common.base.Charsets;
import com.google.inject.Inject;
import de.chaosfisch.http.HttpIOException;
import de.chaosfisch.http.IRequest;
import de.chaosfisch.http.IResponse;
import de.chaosfisch.http.RequestBuilderFactory;
import de.chaosfisch.http.entity.Entity;
import de.chaosfisch.http.entity.EntityBuilder;
import de.chaosfisch.serialization.IJsonSerializer;

import java.io.File;
import java.io.FileNotFoundException;

public class ThumbnailServiceImpl implements IThumbnailService {

	private final RequestBuilderFactory requestBuilderFactory;
	private final IJsonSerializer       jsonSerializer;

	@Inject
	public ThumbnailServiceImpl(final RequestBuilderFactory requestBuilderFactory, final IJsonSerializer jsonSerializer) {
		this.requestBuilderFactory = requestBuilderFactory;
		this.jsonSerializer = jsonSerializer;
	}

	@Override
	public Integer upload(final String content, final File thumbnail, final String videoid) throws FileNotFoundException, ThumbnailResponseException, ThumbnailJsonException {

		if (!thumbnail.exists()) {
			throw new FileNotFoundException(thumbnail.getName());
		}

		final IRequest thumbnailPost = requestBuilderFactory.create("http://www.youtube.com/my_thumbnail_post")
				.post(buildEntity(content, videoid, thumbnail))
				.build();

		try (IResponse response = thumbnailPost.execute()) {
			final String json = response.getContent();
			try {
				return parseResponse(json);
			} catch (final Exception e) {
				throw new ThumbnailJsonException(json, e);
			}
		} catch (final HttpIOException e) {
			throw new ThumbnailResponseException(e);
		}
	}

	private Entity buildEntity(final String content, final String videoid, final File thumbnail) {

		final String search = "yt.setAjaxToken(\"my_thumbnail_post\", \"";
		final String sessiontoken = content.substring(content.indexOf(search) + search.length(), content.indexOf('\"', content
				.indexOf(search) + search.length()));

		return new EntityBuilder().multipart()
				.charset(Charsets.UTF_8)
				.add("video_id", videoid)
				.add("is_ajax", "1")
				.add("session_token", sessiontoken)
				.add("imagefile", thumbnail)
				.build();
	}

	private Integer parseResponse(final String json) {
		return jsonSerializer.fromJSON(json, Thumbnail.class).version;
	}
}
