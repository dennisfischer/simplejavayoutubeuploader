/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.util.io;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.params.HttpParams;

public class Request {
	public enum Method {
		GET, POST, PUT, DELETE, HEAD, OPTIONS, TRACE, CONNECT
	}
	
	private HttpUriRequest		httpRequest;
	private HttpURLConnection	urlConnection;
	
	public static class Builder {
		private final String		url;
		private HttpParams			params;
		private final Method		method;
		private Map<String, String>	headers;
		private HttpEntity			entity;
		
		public Builder(final String url, final Method method) {
			this.url = url;
			this.method = method;
		}
		
		public Builder params(final HttpParams params) {
			this.params = params;
			return this;
		}
		
		public Builder headers(final Map<String, String> headers) {
			this.headers = headers;
			return this;
		}
		
		public Builder entity(final HttpEntity entity) {
			this.entity = entity;
			return this;
		}
		
		public HttpUriRequest buildHttpUriRequest() {
			return new Request(this).getHttpUriRequest();
		}
		
		public HttpURLConnection buildHttpUrlConnection() throws IOException {
			return new Request(this, false).getUrlConnection();
		}
	}
	
	private Request(final Builder builder) {
		switch (builder.method) {
			case DELETE:
				httpRequest = new HttpDelete(builder.url);
			break;
			case HEAD:
				httpRequest = new HttpHead(builder.url);
			break;
			case OPTIONS:
				httpRequest = new HttpOptions(builder.url);
			break;
			case POST:
				httpRequest = new HttpPost(builder.url);
				((HttpEntityEnclosingRequestBase) httpRequest).setEntity(builder.entity);
			break;
			case PUT:
				httpRequest = new HttpPut(builder.url);
				((HttpEntityEnclosingRequestBase) httpRequest).setEntity(builder.entity);
			break;
			case TRACE:
				httpRequest = new HttpTrace(builder.url);
			break;
			default:
			case GET:
				httpRequest = new HttpGet(builder.url);
			break;
		}
		
		if (builder.headers != null) {
			for (final Entry<String, String> entry : builder.headers.entrySet()) {
				httpRequest.addHeader(entry.getKey(), entry.getValue());
			}
		}
		httpRequest.setParams(builder.params != null ? builder.params : httpRequest.getParams());
	}
	
	public Request(final Builder builder, final boolean b) throws IOException {
		final URL url = new URL(builder.url);
		urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setRequestMethod(builder.method.name());
		
		for (final Entry<String, String> entry : builder.headers.entrySet()) {
			urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
		}
	}
	
	private HttpUriRequest getHttpUriRequest() {
		return httpRequest;
	}
	
	private HttpURLConnection getUrlConnection() {
		return urlConnection;
	}
}
