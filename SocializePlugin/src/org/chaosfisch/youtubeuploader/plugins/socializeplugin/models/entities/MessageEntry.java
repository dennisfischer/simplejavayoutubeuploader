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

package org.chaosfisch.youtubeuploader.plugins.socializeplugin.models.entities;

import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.entities.IEntry;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 14.04.12
 * Time: 21:19
 * To change this template use File | Settings | File Templates.
 */
public class MessageEntry implements IEntry
{
	private transient int     identity;
	private           String  message;
	private           int     uploadID;
	private           boolean facebook;
	private           boolean twitter;
	private           boolean youtube;
	private           boolean googlePlus;

	public boolean isGooglePlus()
	{
		return this.googlePlus;
	}

	public void setGooglePlus(final boolean googlePlus)
	{
		this.googlePlus = googlePlus;
	}

	public boolean isYoutube()
	{
		return this.youtube;
	}

	public void setYoutube(final boolean youtube)
	{
		this.youtube = youtube;
	}

	public boolean isTwitter()
	{
		return this.twitter;
	}

	public void setTwitter(final boolean twitter)
	{
		this.twitter = twitter;
	}

	public boolean isFacebook()
	{
		return this.facebook;
	}

	public void setFacebook(final boolean facebook)
	{
		this.facebook = facebook;
	}

	public int getUploadID()
	{
		return this.uploadID;
	}

	public void setUploadID(final int uploadID)
	{
		this.uploadID = uploadID;
	}

	public String getMessage()
	{
		return this.message;
	}

	public void setMessage(final String message)
	{
		this.message = message;
	}

	@Override public int getIdentity()
	{
		return this.identity;
	}

	public void setIdentity(final int identity)
	{
		this.identity = identity;
	}
}
