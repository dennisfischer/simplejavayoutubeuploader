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
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UrlEncodedEntity implements Entity {

	private final UrlEncodedFormEntity entity;

	public UrlEncodedEntity(final Map<String, Object> parameters, final Charset charset) {
		final List<BasicNameValuePair> nameValuePairs = new ArrayList<>(parameters.size());
		for (final Map.Entry<String, Object> param : parameters.entrySet()) {
			nameValuePairs.add(new BasicNameValuePair(param.getKey(), param.getValue().toString()));
		}
		entity = new UrlEncodedFormEntity(nameValuePairs, charset);
	}

	@Override
	public HttpEntity get() {
		return entity;
	}
}
