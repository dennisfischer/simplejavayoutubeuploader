/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader.services.uploader;

public final class PermissionStringConverter {
	private static final String ALLOWED   = "allowed";
	private static final String DENIED    = "denied";
	private static final String MODERATED = "moderated";

	/**
	 * Converts a boolean to a proper gdata.youtube xml element True:Allowed
	 * False:Denied
	 *
	 * @param value
	 * 		the param that should be converted
	 *
	 * @return the PermissionString identified by the given value
	 */
	public static String convertBoolean(final boolean value) {
		return value ? ALLOWED : DENIED;
	}

	/**
	 * Converts a integer to a proper gdata.youtube xml element 1:Allowed
	 * 2:Moderated 3:Denied
	 *
	 * @param value
	 * 		the param that should be converted
	 *
	 * @return the PermissionString identified by the given value
	 */
	public static String convertInteger(final int value) {
		switch (value) {
			case 0:
				return ALLOWED;
			case 1:
			case 3:
				return MODERATED;
			case 2:
				return DENIED;
		}
		return ALLOWED;
	}
}
