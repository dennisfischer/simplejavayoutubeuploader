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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestUtil
{
	
	static DefaultHttpClient httpClient;
	static {
		httpClient = new DefaultHttpClient();
		httpClient.setRedirectStrategy(new DefaultRedirectStrategy() {
			@Override
			public boolean isRedirected(final HttpRequest request,
					final HttpResponse response, final HttpContext context) {
				boolean isRedirect = false;
				try {
					isRedirect = super.isRedirected(request, response, context);
				} catch (final ProtocolException e) {
					e.printStackTrace();
				}
				if (!isRedirect) {
					final int responseCode = response.getStatusLine()
							.getStatusCode();
					if ((responseCode == 301) || (responseCode == 302)) {
						return true;
					}
				}
				return isRedirect;
			}
			
			@Override
			public URI getLocationURI(final HttpRequest request,
					final HttpResponse response, final HttpContext context)
					throws ProtocolException {
				
				final URI lastRedirectedUri = super.getLocationURI(request, response,
						context);
				final Logger logger = LoggerFactory.getLogger(RequestUtil.class);
				try {
					logger.info("Redirecting to: {}", lastRedirectedUri.toURL().toExternalForm());
				} catch (final MalformedURLException e) {
					e.printStackTrace();
				}
				return lastRedirectedUri;
			}
		});
	}
	
	public static HttpResponse execute(final HttpUriRequest request) throws ClientProtocolException, IOException
	{
		return httpClient.execute(request);
	}
	
	/**
	 * Read input from input stream and write it to output stream until there is
	 * no more input from input stream.
	 * 
	 * @param is
	 *            input stream the input stream to read from.
	 * @param os
	 *            output stream the output stream to write to.
	 * @param buf
	 *            the byte array to use as a buffer
	 */
	public static void flow(final InputStream is, final OutputStream os, final byte[] buf) throws IOException
	{
		int numRead;
		while (!Thread.currentThread().isInterrupted() && ((numRead = is.read(buf)) >= 0))
		{
			os.write(buf, 0, numRead);
		}
		os.flush();
	}
	
	/**
	 * Read input from input stream and write it to output stream until there is
	 * no more input from input stream.
	 * 
	 * @param is
	 *            input stream the input stream to read from.
	 * @param os
	 *            output stream the output stream to write to.
	 * @param buf
	 *            the byte array to use as a buffer
	 * @param off
	 *            the offset to start reading
	 * @param len
	 *            the length of bytes to read
	 */
	public static void flow(final InputStream is, final OutputStream os, final byte[] buf, final int off, final int len) throws IOException
	{
		int numRead;
		while (!Thread.currentThread().isInterrupted() && ((numRead = is.read(buf, off, len)) >= 0))
		{
			os.write(buf, 0, numRead);
		}
		os.flush();
	}
	
	/**
	 * Read input from input stream and write it to output stream until there is
	 * no more input from input stream.
	 * 
	 * @param is
	 *            input stream the input stream to read from.
	 * @param os
	 *            output stream the output stream to write to.
	 * @param buf
	 *            the byte array to use as a buffer
	 */
	public static int flowChunk(final InputStream is, final OutputStream os, final byte[] buf, final int off, final int len) throws IOException
	{
		final int numRead;
		if ((numRead = is.read(buf, off, len)) >= 0)
		{
			os.write(buf, 0, numRead);
		}
		os.flush();
		return numRead;
	}
}
