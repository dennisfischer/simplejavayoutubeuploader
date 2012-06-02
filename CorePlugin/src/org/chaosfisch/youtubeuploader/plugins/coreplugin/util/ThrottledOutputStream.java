/*
 * Copyright (c) 2012, Dennis Fischer
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.chaosfisch.youtubeuploader.plugins.coreplugin.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ThrottledOutputStream extends FilterOutputStream
{
	private final long maxBps;
	private       long bytes;
	private final long start;

	/// Constructor.
	public ThrottledOutputStream(final OutputStream out, final long maxBps)
	{
		super(out);
		this.maxBps = maxBps;
		this.bytes = 0;
		this.start = System.currentTimeMillis();
	}

	private final byte[] oneByte = new byte[1];

	/// Writes a byte.  This method will block until the byte is actually
	// written.
	// @param b the byte to be written
	// @exception IOException if an I/O error has occurred
	public void write(final int b) throws IOException
	{
		this.oneByte[0] = (byte) b;
		this.write(this.oneByte, 0, 1);
	}

	/// Writes a subarray of bytes.
	// @param b the data to be written
	// @param off the start offset in the data
	// @param len the number of bytes that are written
	// @exception IOException if an I/O error has occurred
	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException
	{
		// Check the throttle.
		this.bytes += len;
		final long elapsed = Math.max(System.currentTimeMillis() - this.start, 1);

		final long bps = (this.bytes * 1000L) / elapsed;
		if ((this.maxBps != 0) && (bps > this.maxBps)) {
			// Oops, sending too fast.
			final long wakeElapsed = (this.bytes * 1000L) / this.maxBps;
			try {
				Thread.sleep(wakeElapsed - elapsed);
			} catch (InterruptedException ignored) {
			}
		}

		// Write the bytes.
		this.out.write(b, off, len);
	}
}
