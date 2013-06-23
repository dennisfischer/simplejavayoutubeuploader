/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.google.youtube.impl;

import com.google.common.base.Charsets;
import com.google.inject.Inject;
import de.chaosfisch.exceptions.SystemException;
import de.chaosfisch.http.HttpIOException;
import de.chaosfisch.http.IRequest;
import de.chaosfisch.http.IResponse;
import de.chaosfisch.http.RequestBuilderFactory;
import de.chaosfisch.serialization.IJsonSerializer;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.chaosfisch.google.youtube.Thumbnail;
import org.chaosfisch.google.youtube.ThumbnailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;

public class ThumbnailServiceImpl implements ThumbnailService {

	private static final Logger logger = LoggerFactory.getLogger(ThumbnailServiceImpl.class);

	private final RequestBuilderFactory requestBuilderFactory;
	private final IJsonSerializer       jsonSerializer;

	@Inject
	public ThumbnailServiceImpl(final RequestBuilderFactory requestBuilderFactory, final IJsonSerializer jsonSerializer) {
		this.requestBuilderFactory = requestBuilderFactory;
		this.jsonSerializer = jsonSerializer;
	}

	@Override
	public Integer upload(final String content, final File thumbnail, final String videoid) throws SystemException {

		if (!thumbnail.exists()) {
			throw new SystemException(ThumbnailCode.FILE_NOT_FOUND).set("filename", thumbnail.getName());
		}

		final IRequest thumbnailPost = requestBuilderFactory.create("http://www.youtube.com/my_thumbnail_post")
				.post(buildEntity(content, videoid, thumbnail))
				.build();

		try (IResponse response = thumbnailPost.execute()) {
			final String json = response.getContent();
			try {
				return parseResponse(json);
			} catch (final Exception e) {
				throw new SystemException(e, ThumbnailCode.UPLOAD_JSON).set("json", json);
			}
		} catch (final HttpIOException e) {
			throw new SystemException(e, ThumbnailCode.UPLOAD_RESPONSE);
		}
	}

	private MultipartEntity buildEntity(final String content, final String videoid, final File thumbnailFile) {
		final MultipartEntity reqEntity = new MultipartEntity();

		try {
			reqEntity.addPart("video_id", new StringBody(videoid, Charsets.UTF_8));
			reqEntity.addPart("is_ajax", new StringBody("1", Charsets.UTF_8));

			final String search = "yt.setAjaxToken(\"my_thumbnail_post\", \"";
			final String sessiontoken = content.substring(content.indexOf(search) + search.length(), content.indexOf('\"', content
					.indexOf(search) + search.length()));
			reqEntity.addPart("session_token", new StringBody(sessiontoken, Charsets.UTF_8));
			reqEntity.addPart("imagefile", new FileBody(thumbnailFile));
		} catch (UnsupportedEncodingException e) {
			logger.warn("Unsupported charset", e);
		}
		return reqEntity;
	}

	private Integer parseResponse(final String json) {
		return jsonSerializer.fromJSON(json, Thumbnail.class).version;
	}
}
