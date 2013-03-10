/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.services.youtube.thumbnail.impl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.chaosfisch.exceptions.SystemException;
import org.chaosfisch.io.http.RequestUtil;
import org.chaosfisch.youtubeuploader.models.Thumbnail;
import org.chaosfisch.youtubeuploader.services.youtube.thumbnail.spi.ThumbnailService;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class ThumbnailServiceImpl implements ThumbnailService {

	@Override
	public Integer upload(final String content, final String thumbnail, final String videoid) throws SystemException {

		final File thumbnailFile = new File(thumbnail);
		if (!thumbnailFile.exists()) {
			throw new SystemException(ThumbnailCode.FILE_NOT_FOUND).set("filename", thumbnailFile.getName());
		}

		final HttpPost thumbnailPost = new HttpPost("http://www.youtube.com/my_thumbnail_post");

		try {
			thumbnailPost.setEntity(buildEntity(content, videoid, thumbnailFile));
			final HttpResponse response = RequestUtil.execute(thumbnailPost);
			final String json = EntityUtils.toString(response.getEntity(), Charsets.UTF_8);

			try {
				return parseResponse(json);
			} catch (final JsonSyntaxException e) {
				throw SystemException.wrap(e, ThumbnailCode.UPLOAD_JSON).set("json", json);
			}
		} catch (final IOException e) {
			throw SystemException.wrap(e, ThumbnailCode.UPLOAD_RESPONSE);
		}
	}

	private MultipartEntity buildEntity(final String content, final String videoid, final File thumbnailFile)
			throws UnsupportedEncodingException {
		final MultipartEntity reqEntity = new MultipartEntity();

		reqEntity.addPart("video_id", new StringBody(videoid, Charsets.UTF_8));
		reqEntity.addPart("is_ajax", new StringBody("1", Charsets.UTF_8));

		final String search = "yt.setAjaxToken(\"my_thumbnail_post\", \"";
		final String sessiontoken = content.substring(
			content.indexOf(search) + search.length(),
			content.indexOf("\"", content.indexOf(search) + search.length()));
		reqEntity.addPart("session_token", new StringBody(sessiontoken, Charsets.UTF_8));
		reqEntity.addPart("imagefile", new FileBody(thumbnailFile));
		return reqEntity;
	}

	private Integer parseResponse(final String json) {
		final Gson gson = new Gson();
		final Thumbnail obj = gson.fromJson(json, Thumbnail.class);
		return obj.version;
	}
}
