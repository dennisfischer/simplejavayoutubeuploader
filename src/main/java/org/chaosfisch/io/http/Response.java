/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.io.http;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.util.EntityUtils;
import org.chaosfisch.exceptions.SystemException;

import com.google.common.base.Charsets;

public class Response implements AutoCloseable {

	private final HttpResponse	response;

	public Response(final HttpResponse response) {
		this.response = response;
	}

	@Override
	public void close() {
		if (response != null && response.getEntity() != null) {
			EntityUtils.consumeQuietly(response.getEntity());
		}
	}

	public HttpEntity getEntity() {
		return response.getEntity();
	}

	public int getStatusCode() {
		return response.getStatusLine()
			.getStatusCode();
	}

	public HttpResponse getRaw() {
		return response;
	}

	public String getContent() throws SystemException {
		try {
			return EntityUtils.toString(getEntity(), Charsets.UTF_8);
		} catch (ParseException | IOException e) {
			throw SystemException.wrap(e, HttpCode.IO_ERROR);
		}
	}

	public String getCurrentUrl() {
		final HttpUriRequest currentReq = (HttpUriRequest) RequestUtil.context.getAttribute(ExecutionContext.HTTP_REQUEST);
		final HttpHost currentHost = (HttpHost) RequestUtil.context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
		return currentReq.getURI()
			.isAbsolute() ? currentReq.getURI()
			.toString() : currentHost.toURI() + currentReq.getURI();
	}
}
