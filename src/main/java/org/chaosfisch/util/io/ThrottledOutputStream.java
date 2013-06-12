/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.util.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ThrottledOutputStream extends FilterOutputStream {
	private       long     bytes;
	private final long     start;
	private final Throttle throttle;

	public ThrottledOutputStream(final OutputStream out, final Throttle throttle) {
		super(out);
		this.throttle = throttle;
		bytes = 0;
		start = System.currentTimeMillis();
	}

	private final byte[] oneByte = new byte[1];

	/**
	 * Writes a byte. This method will block until the byte is actually
	 * written.
	 *
	 * @param b
	 * 		the byte to be written
	 *
	 * @throws IOException
	 * 		if an I/O error has occurred
	 */
	@Override
	public void write(final int b) throws IOException {
		oneByte[0] = (byte) b;
		write(oneByte, 0, 1);
	}

	/**
	 * Writes a subarray of bytes.
	 *
	 * @param b
	 * 		the data to be written
	 * @param off
	 * 		the start offset in the data
	 * @param len
	 * 		the number of bytes that are written
	 *
	 * @throws IOException
	 * 		if an I/O error has occurred
	 */
	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		// Check the throttle.
		bytes += len;

		final long elapsed = Math.max(System.currentTimeMillis() - start, 1);

		final long bps = bytes * 1000L / elapsed;
		if (0 != throttle.maxBps.get() && bps > throttle.maxBps.multiply(1000).get()) {

			// Oops, sending too fast.
			final long wakeElapsed = bytes * 1000L / throttle.maxBps.multiply(1000).get();
			try {
				Thread.sleep(wakeElapsed - elapsed);

			} catch (final InterruptedException e) { // $codepro.audit.disable
				// logExceptions
				Thread.currentThread().interrupt();
			}
		}

		// Write the bytes.
		out.write(b, off, len);
	}
}
