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
import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.chaosfisch.util.io.RequestUtil;

import com.google.gson.Gson;

public class ThumbnailServiceImpl {
	
	public Integer upload(final String content, String thumbnail, String videoid)
			throws ThumbnailException {
		
		final File thumbnailFile = new File(thumbnail);
		if (!thumbnailFile.exists()) {
			throw new ThumbnailException(
					"Datei nicht vorhanden f�r Thumbnail "
							+ thumbnailFile.getName());
		}
		
		final HttpPost thumbnailPost = new HttpPost(
				"http://www.youtube.com/my_thumbnail_post");
		
		try {
			thumbnailPost.setEntity(buildEntity(content, videoid, thumbnailFile));
			final HttpResponse response = RequestUtil.execute(thumbnailPost);
			return parseResponse(response);
		} catch (ParseException | IOException | NumberFormatException e) {
			throw new ThumbnailException("Thumbnail couldn't be uploaded.", e);
		}
		
	}
	
	private MultipartEntity buildEntity(final String content, String videoid, final File thumbnailFile) throws UnsupportedEncodingException {
		final MultipartEntity reqEntity = new MultipartEntity();
		
		reqEntity.addPart("video_id",
				new StringBody(videoid, Charset.forName("UTF-8")));
		reqEntity.addPart("is_ajax", new StringBody("1", Charset.forName("UTF-8")));
		
		final String search = "yt.setAjaxToken(\"my_thumbnail_post\", \"";
		final String sessiontoken = content.substring(content.indexOf(search)
				+ search.length(), content.indexOf("\"",
				content.indexOf(search) + search.length()));
		reqEntity.addPart("session_token", new StringBody(sessiontoken, Charset.forName("UTF-8")));
		
		reqEntity.addPart("imagefile", new FileBody(thumbnailFile));
		return reqEntity;
	}
	
	private Integer parseResponse(final HttpResponse response) throws IOException {
		Gson gson = new Gson();
		Thumbnail obj = gson.fromJson(EntityUtils.toString(response.getEntity(),
				Charset.forName("UTF-8")), Thumbnail.class);
		
		return obj.version;
	}
}
