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

package org.chaosfisch.util;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 09.01.12
 * Time: 16:49
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings({"CallToStringEquals", "DuplicateStringLiteralInspection", "HardCodedStringLiteral"})
public class Mimetype
{

	public static final  String[] EXTENSIONS = {"mkv", "mp4", "mpeg", "avi", "flv", "mov", "wmv", "f4v", "m4v", "mk3d"};
	private static final String[] MIMETYPES  = {"video/mkv", "video/mp4", "video/mpeg", "video/avi", "video/x-flv", "video/quicktime", "video/xs-ms-wmv", "video/x-flv", "video/mp4", "video/mkv"};

	public static String getMimetypeByExtension(final String extension)
	{
		int i = 0;
		for (final String ext : Mimetype.EXTENSIONS) {
			if (ext.equals(extension)) {
				return Mimetype.MIMETYPES[i];
			}
			i++;
		}
		return "application/octet-stream";
	}

	public static String getMimetypeByFile(final String file)
	{
		final String extension = new String(file.substring(file.lastIndexOf(".") + 1, file.length()));
		int i = 0;
		for (final String ext : Mimetype.EXTENSIONS) {
			if (ext.equals(extension)) {
				return Mimetype.MIMETYPES[i];
			}
			i++;
		}
		return "application/octet-stream";
	}
}
