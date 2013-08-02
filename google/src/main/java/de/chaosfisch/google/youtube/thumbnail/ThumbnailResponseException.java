/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.thumbnail;

import de.chaosfisch.http.HttpIOException;

public class ThumbnailResponseException extends Exception {
	public ThumbnailResponseException(final HttpIOException e) {
		super(e);
	}
}
