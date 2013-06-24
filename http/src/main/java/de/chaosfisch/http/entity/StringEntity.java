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

import java.nio.charset.Charset;

public class StringEntity implements Entity {
	private final org.apache.http.entity.StringEntity entity;

	public StringEntity(final String value, final Charset charset) {
		entity = new org.apache.http.entity.StringEntity(value, charset);
	}

	@Override
	public HttpEntity get() {
		return entity;
	}
}
