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

package org.chaosfisch.youtubeuploader.db;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 07.01.12
 * Time: 18:34
 * To change this template use File | Settings | File Templates.
 */
public class PresetEntry
{
	private           boolean autotitle;
	private           String  autotitleFormat;
	private           String  category;
	private           short   comment;
	private           boolean commentvote;
	private           String  defaultDir;
	private           String  description;
	private           boolean embed;
	private           String  keywords;
	private           boolean mobile;
	private           String  name;
	private           short   numberModifier;
	private           short   videoresponse;
	private           short   visibility;
	private transient int     identity;
	private           boolean rate;

	public boolean isRate()
	{
		return this.rate;
	}

	public void setRate(final boolean rate)
	{
		this.rate = rate;
	}

	public int getIdentity()
	{
		return this.identity;
	}

	public void setIdentity(final int identity)
	{
		this.identity = identity;
	}

	public short getVisibility()
	{
		return this.visibility;
	}

	public void setVisibility(final short visibility)
	{
		this.visibility = visibility;
	}

	public short getVideoresponse()
	{
		return this.videoresponse;
	}

	public void setVideoresponse(final short videoresponse)
	{
		this.videoresponse = videoresponse;
	}

	public short getNumberModifier()
	{
		return this.numberModifier;
	}

	public void setNumberModifier(final short numberModifier)
	{
		this.numberModifier = numberModifier;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public boolean isMobile()
	{
		return this.mobile;
	}

	public void setMobile(final boolean mobile)
	{
		this.mobile = mobile;
	}

	public String getKeywords()
	{
		return this.keywords;
	}

	public void setKeywords(final String keywords)
	{
		this.keywords = keywords;
	}

	public boolean isEmbed()
	{
		return this.embed;
	}

	public void setEmbed(final boolean embed)
	{
		this.embed = embed;
	}

	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(final String description)
	{
		this.description = description;
	}

	public String getDefaultDir()
	{
		return this.defaultDir;
	}

	public void setDefaultDir(final String defaultDir)
	{
		this.defaultDir = defaultDir;
	}

	public boolean isCommentvote()
	{
		return this.commentvote;
	}

	public void setCommentvote(final boolean commentvote)
	{
		this.commentvote = commentvote;
	}

	public short getComment()
	{
		return this.comment;
	}

	public void setComment(final short comment)
	{
		this.comment = comment;
	}

	public String getCategory()
	{
		return this.category;
	}

	public void setCategory(final String category)
	{
		this.category = category;
	}

	public String getAutotitleFormat()
	{
		return this.autotitleFormat;
	}

	public void setAutotitleFormat(final String autotitleFormat)
	{
		this.autotitleFormat = autotitleFormat;
	}

	public boolean isAutotitle()
	{
		return this.autotitle;
	}

	public void setAutotitle(final boolean autotitle)
	{
		this.autotitle = autotitle;
	}

	private transient Collection<DirectoryEntry> directories;

	public Collection<DirectoryEntry> getDirectories()
	{
		return this.directories;
	}

	public void setDirectories(final Collection<DirectoryEntry> directories)
	{
		this.directories = directories;
	}

	public String toString()
	{
		return this.getName();
	}

	private PlaylistEntry playlist;

	public PlaylistEntry getPlaylist()
	{
		return this.playlist;
	}

	public void setPlaylist(final PlaylistEntry playlist)
	{
		this.playlist = playlist;
	}
}
