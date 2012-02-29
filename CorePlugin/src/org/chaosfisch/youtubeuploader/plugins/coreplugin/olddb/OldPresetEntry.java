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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.olddb;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 08.02.12
 * Time: 19:44
 * To change this template use File | Settings | File Templates.
 *
 * @deprecated Removed in v 2.1
 */
@Deprecated
public class OldPresetEntry
{
	private int     number_modifier;
	private String  name;
	private String  description;
	private String  keywords;
	private String  autotitle_format;
	private String  default_dir;
	private String  category;
	private boolean isPlaylist;
	private boolean isAutotitle;
	//Permissions
	private boolean isRate;
	private boolean isCommentVote;
	private boolean isMobile;
	private boolean isEmbed;
	private int     comment;
	private int     videoResponse;
	private int     visibility;

	public int getNumber_modifier()
	{
		return this.number_modifier;
	}

	public void setNumber_modifier(final int number_modifier)
	{
		this.number_modifier = number_modifier;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(final String description)
	{
		this.description = description;
	}

	public String getKeywords()
	{
		return this.keywords;
	}

	public void setKeywords(final String keywords)
	{
		this.keywords = keywords;
	}

	public String getAutotitle_format()
	{
		return this.autotitle_format;
	}

	public void setAutotitle_format(final String autotitle_format)
	{
		this.autotitle_format = autotitle_format.replaceAll("%playlist%", "{playlist}").replaceAll("%number%", "{nummer}");
	}

	public String getDefault_dir()
	{
		return this.default_dir;
	}

	public void setDefault_dir(final String default_dir)
	{
		this.default_dir = default_dir;
	}

	public String getCategory()
	{
		return this.category;
	}

	public void setCategory(final String category)
	{
		this.category = category;
	}

	public boolean isPlaylist()
	{
		return this.isPlaylist;
	}

	public void setPlaylist(final boolean playlist)
	{
		this.isPlaylist = playlist;
	}

	public boolean isAutotitle()
	{
		return this.isAutotitle;
	}

	public void setAutotitle(final boolean autotitle)
	{
		this.isAutotitle = autotitle;
	}

	public boolean isRate()
	{
		return this.isRate;
	}

	public void setRate(final boolean rate)
	{
		this.isRate = rate;
	}

	public boolean isCommentVote()
	{
		return this.isCommentVote;
	}

	public void setCommentVote(final boolean commentVote)
	{
		this.isCommentVote = commentVote;
	}

	public boolean isMobile()
	{
		return this.isMobile;
	}

	public void setMobile(final boolean mobile)
	{
		this.isMobile = mobile;
	}

	public boolean isEmbed()
	{
		return this.isEmbed;
	}

	public void setEmbed(final boolean embed)
	{
		this.isEmbed = embed;
	}

	public int getComment()
	{
		return this.comment;
	}

	public void setComment(final int comment)
	{
		this.comment = comment;
	}

	public int getVideoResponse()
	{
		return this.videoResponse;
	}

	public void setVideoResponse(final int videoResponse)
	{
		this.videoResponse = videoResponse;
	}

	public int getVisibility()
	{
		return this.visibility;
	}

	public void setVisibility(final int visibility)
	{
		this.visibility = visibility;
	}
}

