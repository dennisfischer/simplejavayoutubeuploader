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

package org.chaosfisch.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ThrottledOutputStream extends FilterOutputStream
{
	private final long	maxBps;
	private final long	start;
	private long		bytes;

	/**
	 * Constructs a new ThrotteldOutputStream
	 * 
	 * @param out
	 *            the output stream to be managed
	 * @param maxBps
	 *            the max bytes per second that can be written
	 */
	public ThrottledOutputStream(final OutputStream out, final long maxBps)
	{
		super(out);
		this.maxBps = maxBps;
		bytes = 0;
		start = System.currentTimeMillis();
	}

	private final byte[]	oneByte	= new byte[1];

	/**
	 * Writes a byte. This method will block until the byte is actually written
	 * 
	 * @param b
	 *            the byte to be written
	 * @throws IOException
	 *             if an I/O error has occured
	 */
	@Override
	public void write(final int b) throws IOException
	{
		oneByte[0] = (byte) b;
		write(oneByte, 0, 1);
	}

	/**
	 * Writes an array of bytes.
	 * 
	 * @param b
	 *            the data to be written
	 * @param off
	 *            the start offset in the data
	 * @param len
	 *            the number of bytes that are written
	 * @throws IOException
	 *             if an I/O error has occurred
	 */
	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException
	{
		// Check the throttle.
		bytes += len;
		final long elapsed = Math.max(System.currentTimeMillis() - start, 1);

		final long bps = (bytes * 1000L) / elapsed;
		if ((maxBps != 0) && (bps > maxBps))
		{
			// Oops, sending too fast.
			final long wakeElapsed = (bytes * 1000L) / maxBps;
			try
			{
				Thread.sleep(wakeElapsed - elapsed);
			} catch (InterruptedException ignored)
			{
				throw new RuntimeException("This shouldn't happen");
			}
		}

		// Write the bytes.
		out.write(b, off, len);
	}
}
