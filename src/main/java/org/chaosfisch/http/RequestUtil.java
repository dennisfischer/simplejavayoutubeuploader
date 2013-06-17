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

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.chaosfisch.slf4j.Log;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;

public final class RequestUtil implements IRequestUtil {

	@Log
	private       Logger            logger;
	private final DefaultHttpClient httpClient;
	private final HttpContext context = new BasicHttpContext();

	public RequestUtil() {
		final PoolingClientConnectionManager cxMgr = new PoolingClientConnectionManager(SchemeRegistryFactory.createDefault());
		cxMgr.setMaxTotal(10);
		cxMgr.setDefaultMaxPerRoute(10);
		httpClient = new DefaultHttpClient(cxMgr);
		httpClient.setRedirectStrategy(new DefaultRedirectStrategy() {
			@Override
			public boolean isRedirected(final HttpRequest request, final HttpResponse response, final HttpContext context) {
				boolean isRedirect = false;
				try {
					isRedirect = super.isRedirected(request, response, context);
				} catch (final ProtocolException e) {
					e.printStackTrace();
				}
				if (!isRedirect) {
					final int responseCode = response.getStatusLine().getStatusCode();
					if (301 == responseCode || 302 == responseCode) {
						return true;
					}
				}
				return isRedirect;
			}

			@Override
			public URI getLocationURI(final HttpRequest request, final HttpResponse response, final HttpContext context) throws ProtocolException {

				final URI lastRedirectedUri = super.getLocationURI(request, response, context);
				try {
					logger.debug("Redirecting to: {}", lastRedirectedUri.toURL().toExternalForm());
				} catch (final MalformedURLException e) {
					logger.warn("Malfromed URL!", e);
				}
				return lastRedirectedUri;
			}
		});
	}

	@Override
	public HttpResponse execute(final HttpUriRequest request) throws IOException {
		return httpClient.execute(request, context);
	}

	@Override
	public void flow(final InputStream is, final OutputStream os, final byte[] buf) throws IOException {
		int numRead;
		while (!Thread.currentThread().isInterrupted() && 0 <= (numRead = is.read(buf))) {
			os.write(buf, 0, numRead);
		}
		os.flush();
	}

	@Override
	public void flow(final InputStream is, final OutputStream os, final byte[] buf, final int off, final int len) throws IOException {
		int numRead;
		while (!Thread.currentThread().isInterrupted() && 0 <= (numRead = is.read(buf, off, len))) {
			os.write(buf, 0, numRead);
		}
		os.flush();
	}

	@Override
	public int flowChunk(final InputStream is, final OutputStream os, final byte[] buf, final int off, final int len) throws IOException {
		final int numRead;
		if (0 <= (numRead = is.read(buf, off, len))) {
			os.write(buf, 0, numRead);
		}
		os.flush();
		return numRead;
	}

	@Override
	public HttpContext getContext() {
		return context;
	}
}
