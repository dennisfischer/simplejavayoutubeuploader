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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.worker;

import com.google.inject.Inject;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.entities.QueueEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.Uploader;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 02.01.12
 * Time: 20:31
 * To change this template use File | Settings | File Templates.
 */

class XMLBlobBuilder
{
	private final QueueEntry queueEntry;

	@Inject
	public XMLBlobBuilder(final QueueEntry queueEntry)
	{
		this.queueEntry = queueEntry;
	}

	/**
	 * Uploads a new video to YouTube.
	 *
	 * @return The new created xml blob
	 */

	@SuppressWarnings({"HardCodedStringLiteral", "StringConcatenation"})
	public String buildXMLBlob()
	{

		return "<yt:accessControl action='embed' permission ='" + this.booleanToPermissionString(this.queueEntry.isEmbed()) +
				"' " +
				"/>" +
				"<yt:accessControl action='rate' permission='" + this.booleanToPermissionString(this.queueEntry.isRate()) + "'/>" + "<yt:accessControl action='syndicate' permission='" +
				this.booleanToPermissionString(this.queueEntry.isMobile()) + "'/>" +
				"<yt:accessControl " +
				"action='commentVote' permission='" +
				this.booleanToPermissionString(this.queueEntry.isCommentvote()) + "'/>" + "<yt:accessControl action='videoRespond' permission='" + this.intToPermissionString(this.queueEntry.getVideoresponse
				()) +
				"'/>" +
				"<yt:accessControl action='list' permission='" + this.booleanToPermissionString(!this.queueEntry.isUnlisted()) + "'/>";
	}

	/**
	 * Converts a boolean to a proper gdata.youtube xml element
	 * True:Allowed
	 * False:Denied
	 *
	 * @param value the param that should be converted
	 * @return the PermissionString identified by the given value
	 */
	private String booleanToPermissionString(final boolean value)
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
	private String intToPermissionString(final int value)
	{
		switch (value) {
			case 0:
				return Uploader.ALLOWED;
			case 1:
				return Uploader.MODERATED;
			case 2:
				return Uploader.DENIED;
		}
		return Uploader.ALLOWED;
	}
}
