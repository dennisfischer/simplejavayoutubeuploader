/*
 * Copyright (c) 2014 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.thumbnail;

public class ThumbnailIOException extends Exception {
	private static final long serialVersionUID = -2891454084663818371L;

	public ThumbnailIOException(final Exception e) {
		super(e);
	}
}
