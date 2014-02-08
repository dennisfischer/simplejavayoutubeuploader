/*
 * Copyright (c) 2014 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.playlist;

public class PlaylistIOException extends Exception {
	private static final long serialVersionUID = -6202643396909658030L;

	public PlaylistIOException(final Exception e) {
		super(e);
	}
}
