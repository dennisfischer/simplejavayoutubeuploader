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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class InputStreams {
	/**
	 * Converts any InputStream to a string
	 * 
	 * @param inputstream
	 *            the InputStream
	 * @return the read string
	 */
	public static String toString(final InputStream inputstream) {
		String line = "";
		final StringBuilder total = new StringBuilder();

		// Wrap a BufferedReader around the InputStream
		final BufferedReader rd = new BufferedReader(new InputStreamReader(inputstream));

		// Read response until the end
		try {
			while ((line = rd.readLine()) != null) {
				total.append(line);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}

		// Return full string
		return total.toString();
	}

}
