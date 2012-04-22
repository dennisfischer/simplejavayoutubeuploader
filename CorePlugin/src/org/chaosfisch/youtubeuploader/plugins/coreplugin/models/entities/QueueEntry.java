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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.models.entities;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 07.01.12
 * Time: 16:49
 * To change this template use File | Settings | File Templates.
 */
public class QueueEntry implements IEntry
{
	private transient int     identity;
	private           boolean archived;
	private           String  category;
	private           short   comment;
	private           boolean commentvote;
	private           String  description;
	private           boolean embed;
	private           boolean failed;
	private           String  file;
	private           String  keywords;
	private           String  mimetype;
	private           boolean mobile;
	private           boolean privatefile;
	private           boolean rate;
	private           String  title;
	private           boolean unlisted;
	private           String  uploadurl;
	private           short   videoresponse;
	private           int     sequence;
	private           Date    started;
	private           Date    eta;
	private           int     progress;
	private           String  status;
	private           boolean inprogress;
	private           boolean locked;
	private           String  videoId;

	public String getVideoId()
	{
		return this.videoId;
	}

	public void setVideoId(final String videoId)
	{
		this.videoId = videoId;
	}

	public boolean isLocked()
	{
		return this.locked;
	}

	public void setLocked(final boolean locked)
	{
		this.locked = locked;
	}

	public boolean isInprogress()
	{
		return this.inprogress;
	}

	public void setInprogress(final boolean inprogress)
	{
		this.inprogress = inprogress;
	}

	public Date getStarted()
	{
		return this.started;
	}

	public void setStarted(final Date started)
	{
		this.started = started;
	}

	public int getSequence()
	{
		return this.sequence;
	}

	public void setSequence(final int sequence)
	{
		this.sequence = sequence;
	}

	public short getVideoresponse()
	{
		return this.videoresponse;
	}

	public void setVideoresponse(final short videoresponse)
	{
		this.videoresponse = videoresponse;
	}

	public String getUploadurl()
	{
		return this.uploadurl;
	}

	public void setUploadurl(final String uploadurl)
	{
		this.uploadurl = uploadurl;
	}

	public Date getEta()
	{
		return this.eta;
	}

	public void setEta(final Date eta)
	{
		this.eta = eta;
	}

	public int getProgress()
	{
		return this.progress;
	}

	public void setProgress(final int progress)
	{
		this.progress = progress;
	}

	public String getStatus()
	{
		return this.status;
	}

	public void setStatus(final String status)
	{
		this.status = status;
	}

	public boolean isUnlisted()
	{
		return this.unlisted;
	}

	public void setUnlisted(final boolean unlisted)
	{
		this.unlisted = unlisted;
	}

	public String getTitle()
	{
		return this.title;
	}

	public void setTitle(final String title)
	{
		this.title = title;
	}

	public boolean isRate()
	{
		return this.rate;
	}

	public void setRate(final boolean rate)
	{
		this.rate = rate;
	}

	public boolean isPrivatefile()
	{
		return this.privatefile;
	}

	public void setPrivatefile(final boolean privatefile)
	{
		this.privatefile = privatefile;
	}

	public boolean isMobile()
	{
		return this.mobile;
	}

	public void setMobile(final boolean mobile)
	{
		this.mobile = mobile;
	}

	public String getMimetype()
	{
		return this.mimetype;
	}

	public void setMimetype(final String mimetype)
	{
		this.mimetype = mimetype;
	}

	public String getKeywords()
	{
		return this.keywords;
	}

	public void setKeywords(final String keywords)
	{
		this.keywords = keywords;
	}

	public String getFile()
	{
		return this.file;
	}

	public void setFile(final String file)
	{
		this.file = file;
	}

	public boolean isFailed()
	{
		return this.failed;
	}

	public void setFailed(final boolean failed)
	{
		this.failed = failed;
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

	public int getIdentity()
	{
		return this.identity;
	}

	public void setIdentity(final int identity)
	{
		this.identity = identity;
	}

	public boolean isArchived()
	{
		return this.archived;
	}

	public void setArchived(final boolean archived)
	{
		this.archived = archived;
	}

	private AccountEntry account;

	public AccountEntry getAccount()
	{
		return this.account;
	}

	public void setAccount(final AccountEntry account)
	{
		this.account = account;
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
