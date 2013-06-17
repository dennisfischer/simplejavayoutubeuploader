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

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IRequestUtil {

	/**
	 * Executes a HttpUriRequest and returns the HttpResponse
	 *
	 * @param request
	 * 		the HttpUriRequest to execute
	 *
	 * @return HttpResponse received
	 *
	 * @throws IOException
	 * 		if I/O error
	 */
	HttpResponse execute(HttpUriRequest request) throws IOException;

	/**
	 * Read input from input stream and write it to output stream until there is
	 * no more input from input stream.
	 *
	 * @param is
	 * 		input stream the input stream to read from.
	 * @param os
	 * 		output stream the output stream to write to.
	 * @param buf
	 * 		the byte array to use as a buffer
	 */
	void flow(InputStream is, OutputStream os, byte[] buf) throws IOException;

	/**
	 * Read input from input stream and write it to output stream until there is
	 * no more input from input stream.
	 *
	 * @param is
	 * 		input stream the input stream to read from.
	 * @param os
	 * 		output stream the output stream to write to.
	 * @param buf
	 * 		the byte array to use as a buffer
	 * @param off
	 * 		the offset to start reading
	 * @param len
	 * 		the length of bytes to read
	 */
	void flow(InputStream is, OutputStream os, byte[] buf, int off, int len) throws IOException;

	/**
	 * Read input from input stream and write it to output stream until there is
	 * no more input from input stream.
	 *
	 * @param is
	 * 		input stream the input stream to read from.
	 * @param os
	 * 		output stream the output stream to write to.
	 * @param buf
	 * 		the byte array to use as a buffer
	 */
	int flowChunk(InputStream is, OutputStream os, byte[] buf, int off, int len) throws IOException;

	/** @return HttpContext - the used driver */
	HttpContext getContext();
}
