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

package org.chaosfisch.youtubeuploader.services.uploader;


/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 02.01.12
 * Time: 20:31
 * To change this template use File | Settings | File Templates.
 */

class PermissionStringConverter
{
	/**
	 * Converts a boolean to a proper gdata.youtube xml element
	 * True:Allowed
	 * False:Denied
	 *
	 * @param value the param that should be converted
	 * @return the PermissionString identified by the given value
	 */
	public static String convertBoolean(final boolean value)
	{
		if (value) {
			return Uploader.ALLOWED;
		}
		return Uploader.DENIED;
	}

	/**
	 * Converts a integer to a proper gdata.youtube xml element
	 * 1:Allowed
	 * 2:Moderated
	 * 3:Denied
	 *
	 * @param value the param that should be converted
	 * @return the PermissionString identified by the given value
	 */
	public static String convertInteger(final int value)
	{
		switch (value) {
			case 0:
				return Uploader.ALLOWED;
			case 1:
			case 3:
				return Uploader.MODERATED;
			case 2:
				return Uploader.DENIED;
		}
		return Uploader.ALLOWED;
	}
}
