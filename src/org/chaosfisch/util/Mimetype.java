/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.util;

public class Mimetype
{

	/**
	 * Default known media file extensions
	 */
	public static final String[]	EXTENSIONS	= { "mkv", "mp4", "mpeg", "avi", "flv", "mov", "wmv", "f4v", "m4v", "mk3d" };

	/**
	 * Default know media file mimetypes to know EXTENSIONS
	 */
	private static final String[]	MIMETYPES	= { "video/mkv", "video/mp4", "video/mpeg", "video/avi", "video/x-flv", "video/quicktime",
			"video/xs-ms-wmv", "video/x-flv", "video/mp4", "video/mkv" };

	/**
	 * Gets the mimetype by the given extension
	 * 
	 * @param extension
	 *            the extension to search
	 * @return extension mimetype | application/octet-stream if not found
	 */
	public static String getMimetypeByExtension(final String extension)
	{
		int i = 0;
		for (final String ext : Mimetype.EXTENSIONS)
		{
			if (ext.equals(extension)) { return Mimetype.MIMETYPES[i]; }
			i++;
		}
		return "application/octet-stream";
	}

	/**
	 * Gets the mimetype by the given file
	 * 
	 * @param file
	 *            the file to search
	 * @return file mimetype | application/octet-stream if not found
	 */
	public static String getMimetypeByFile(final String file)
	{
		final String extension = new String(file.substring(file.lastIndexOf(".") + 1, file.length()));
		return getMimetypeByExtension(extension);
	}
}
