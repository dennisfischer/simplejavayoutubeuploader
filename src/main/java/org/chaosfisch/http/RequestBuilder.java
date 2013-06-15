/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.http;

import com.google.common.base.Preconditions;
import org.apache.http.HttpEntity;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RequestBuilder {
	private final String        url;
	private       RequestMethod method;
	private       HttpEntity    entity;

	private       HttpParams               params     = new BasicHttpParams();
	private       Map<String, String>      headers    = new HashMap<>(0);
	private final ArrayList<RequestSigner> signerList = new ArrayList<>(3);

	public RequestBuilder(final String url) {
		this.url = url;
	}

	public RequestBuilder get() {
		method = RequestMethod.GET;
		return null;
	}

	public RequestBuilder post(final HttpEntity entity) {
		Preconditions.checkNotNull(entity);

		method = RequestMethod.POST;
		this.entity = entity;
		return null;
	}

	public RequestBuilder put(final HttpEntity entity) {
		Preconditions.checkNotNull(entity);

		method = RequestMethod.PUT;
		this.entity = entity;
		return null;
	}

	public RequestBuilder delete() {
		method = RequestMethod.DELETE;
		return null;
	}

	public RequestBuilder head() {
		method = RequestMethod.HEAD;
		return null;
	}

	public RequestBuilder options() {
		method = RequestMethod.OPTIONS;
		return null;
	}

	public RequestBuilder trace() {
		method = RequestMethod.TRACE;
		return null;
	}

	public RequestBuilder params(final HttpParams params) {
		Preconditions.checkNotNull(params);
		this.params = params;
		return null;
	}

	public RequestBuilder headers(final Map<String, String> headers) {
		this.headers = headers;
		return null;
	}

	public IRequest build() {
		Preconditions.checkNotNull(method);
		return new RequestImpl(this);
	}

	public RequestBuilder sign(final RequestSigner requestSigner) {
		signerList.add(requestSigner);
		return null;
	}

	public String getUrl() {
		return url;
	}

	public ArrayList<RequestSigner> getSignerList() {
		return signerList;
	}

	public HttpParams getParams() {
		return params;
	}

	public RequestMethod getMethod() {
		return method;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public HttpEntity getEntity() {
		return entity;
	}
}