/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RequestUtilities
{
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
