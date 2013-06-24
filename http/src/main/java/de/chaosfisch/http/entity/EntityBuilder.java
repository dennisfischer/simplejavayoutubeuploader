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

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class EntityBuilder {

	private final Map<String, Object> parameters = new HashMap<>();
	private Charset charset;
	private boolean multipart;
	private boolean stringEntity;
	private String  value;

	public EntityBuilder multipart() {
		multipart = true;
		return this;
	}

	public EntityBuilder charset(final Charset charset) {
		this.charset = charset;
		return this;
	}

	public EntityBuilder add(final String key, final Object value) {
		parameters.put(key, value);
		return this;
	}

	public EntityBuilder addAll(final Map<String, Object> params) {
		this.parameters.putAll(params);
		return this;
	}

	public Entity build() {
		if (stringEntity) {
			return new StringEntity(value, charset);
		} else if (multipart) {
			return new MultipartEntity(parameters, charset);
		}
		return new UrlEncodedEntity(parameters, charset);
	}

	public EntityBuilder stringEntity(final String value) {
		stringEntity = true;
		this.value = value;
		return this;
	}
}
