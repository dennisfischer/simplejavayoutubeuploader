/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.util.http;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

public class Request {
	public enum Method {
		GET {
			@Override
			HttpGet get(final String url) {
				return new HttpGet(url);
			}
		},
		POST {
			@Override
			HttpPost get(final String url) {
				return new HttpPost(url);
			}
		},
		PUT {
			@Override
			HttpPut get(final String url) {
				return new HttpPut(url);
			}
		},
		DELETE {
			@Override
			HttpDelete get(final String url) {
				return new HttpDelete(url);
			}
		},
		HEAD {
			@Override
			HttpHead get(final String url) {
				return new HttpHead(url);
			}
		},
		OPTIONS {
			@Override
			HttpOptions get(final String url) {
				return new HttpOptions(url);
			}
		},
		TRACE {
			@Override
			HttpTrace get(final String url) {
				return new HttpTrace(url);
			}
		};

		abstract HttpRequestBase get(String url);
	}

	public static class Builder {
		private final String              url;
		private       HttpParams          params;
		private       Method              method;
		private       Map<String, String> headers;
		private       HttpEntity          entity;
		private       RequestSigner       requestSigner;
		private       String              authHeader;

		public Builder(final String url) {
			this.url = url;
		}

		public Builder get() {
			method = Method.GET;
			return this;
		}

		public Builder post(final HttpEntity entity) {
			method = Method.POST;
			this.entity = entity;
			return this;
		}

		@SuppressWarnings("SameParameterValue")
		public Builder put(final HttpEntity entity) {
			method = Method.PUT;
			this.entity = entity;
			return this;
		}

		public Builder delete() {
			method = Method.DELETE;
			return this;
		}

		public Builder head() {
			method = Method.HEAD;
			return this;
		}

		public Builder options() {
			method = Method.OPTIONS;
			return this;
		}

		public Builder trace() {
			method = Method.TRACE;
			return this;
		}

		public Builder params(final HttpParams params) {
			this.params = params;
			return this;
		}

		public Builder headers(final Map<String, String> headers) {
			this.headers = headers;
			return this;
		}

		public Request build() {
			return new Request(this);
		}

		public Builder sign(final RequestSigner requestSigner) {
			this.requestSigner = requestSigner;
			return this;
		}

		public Builder sign(final RequestSigner requestSigner, final String authHeader) {
			this.requestSigner = requestSigner;
			this.authHeader = authHeader;
			return this;
		}

	}

	private final HttpUriRequest httpRequest;

	private Request(final Builder builder) {
		httpRequest = builder.method.get(builder.url);

		if (builder.method == Method.POST || builder.method == Method.PUT) {
			((HttpEntityEnclosingRequestBase) httpRequest).setEntity(builder.entity);
		}

		if (builder.headers != null) {
			for (final Entry<String, String> entry : builder.headers.entrySet()) {
				httpRequest.addHeader(entry.getKey(), entry.getValue());
			}
		}
		httpRequest.setParams(builder.params != null ? builder.params : httpRequest.getParams());

		if (builder.requestSigner != null) {
			if (builder.authHeader != null) {
				builder.requestSigner.signWithAuthorization(httpRequest, builder.authHeader);
			} else {
				builder.requestSigner.sign(httpRequest);
			}
		}
	}

	public Response execute() throws IOException {
		return new Response(RequestUtil.execute(httpRequest));
	}
}
