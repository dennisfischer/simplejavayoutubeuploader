/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.http.entity;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Map;

public class MultipartEntity implements Entity {

	private final org.apache.http.entity.mime.MultipartEntity entity;

	public MultipartEntity(final Map<String, Object> parameters, final Charset charset) {
		entity = new org.apache.http.entity.mime.MultipartEntity();

		try {

			for (final Map.Entry<String, Object> param : parameters.entrySet()) {

				final ContentBody content;

				if (param.getValue() instanceof File) {
					content = new FileBody((File) param.getValue());
				} else {
					content = new StringBody(param.getValue().toString(), charset);

				}
				entity.addPart(param.getKey(), content);
			}

		} catch (UnsupportedEncodingException ignored) {
		}
	}

	@Override
	public HttpEntity get() {
		return entity;
	}
}
