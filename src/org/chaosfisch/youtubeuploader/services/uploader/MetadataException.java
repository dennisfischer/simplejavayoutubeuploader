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
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 01.08.12
 * Time: 15:57
 * To change this template use File | Settings | File Templates.
 */
public class MetadataException extends Exception
{

	private static final long serialVersionUID = 1453709249687249732L;

	public MetadataException(final String message)
	{
		super(message);
	}

	public MetadataException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}
