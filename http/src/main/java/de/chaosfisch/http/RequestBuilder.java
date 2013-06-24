/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.http;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import de.chaosfisch.http.entity.Entity;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RequestBuilder {
	private final String         url;
	private final RequestFactory requestFactory;
	private       RequestMethod  method;
	private       Entity         entity;

	private       HttpParams               params     = new BasicHttpParams();
	private       Map<String, String>      headers    = new HashMap<>(0);
	private final ArrayList<RequestSigner> signerList = new ArrayList<>(3);

	@Inject
	public RequestBuilder(final RequestFactory requestFactory, @Assisted final String url) {
		this.requestFactory = requestFactory;
		this.url = url;
	}

	public RequestBuilder get() {
		method = RequestMethod.GET;
		return this;
	}

	public RequestBuilder post(final Entity entity) {
		Preconditions.checkNotNull(entity);

		method = RequestMethod.POST;
		this.entity = entity;
		return this;
	}

	public RequestBuilder put(final Entity entity) {
		Preconditions.checkNotNull(entity);

		method = RequestMethod.PUT;
		this.entity = entity;
		return this;
	}

	public RequestBuilder delete() {
		method = RequestMethod.DELETE;
		return this;
	}

	public RequestBuilder head() {
		method = RequestMethod.HEAD;
		return this;
	}

	public RequestBuilder options() {
		method = RequestMethod.OPTIONS;
		return this;
	}

	public RequestBuilder trace() {
		method = RequestMethod.TRACE;
		return this;
	}

	public RequestBuilder params(final HttpParams params) {
		Preconditions.checkNotNull(params);
		this.params = params;
		return this;
	}

	public RequestBuilder headers(final Map<String, String> headers) {
		this.headers = headers;
		return this;
	}

	public IRequest build() {
		Preconditions.checkNotNull(method);
		return requestFactory.create(this);
	}

	public RequestBuilder sign(final RequestSigner requestSigner) {
		signerList.add(requestSigner);
		return this;
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

	public Entity getEntity() {
		return entity;
	}
}