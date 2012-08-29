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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.models;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 07.01.12
 * Time: 16:49
 * To change this template use File | Settings | File Templates.
 */
public class Queue implements IModel
{
	public String   category;
	public String   description;
	public String   file;
	public String   keywords;
	public String   mimetype;
	public String   status;
	public String   title;
	public String   uploadurl;
	public String   videoId;
	public boolean  archived;
	public boolean  commentvote;
	public boolean  inprogress;
	public boolean  locked;
	public boolean  mobile;
	public boolean  privatefile;
	public boolean  rate;
	public boolean  unlisted;
	public boolean  embed;
	public boolean  failed;
	public short    comment;
	public short    videoresponse;
	public int      progress;
	public int      sequence;
	public Date     started;
	public Date     eta;
	public Account  account;
	public Playlist playlist;
	public boolean  monetize;
	public boolean  monetizeOverlay;
	public boolean  monetizeTrueview;
	public boolean  monetizeProduct;
	public String   enddir;
	public short    license;
	public Date     release;

	public boolean claim;
	public short   claimtype;
	public short   claimpolicy;
	public boolean partnerOverlay;
	public boolean partnerTrueview;
	public boolean partnerProduct;
	public boolean partnerInstream;
	public String  asset;

	public String webTitle;
	public String webDescription;
	public String webID;
	public String webNotes;

	public String tvTMSID;
	public String tvISAN;
	public String tvEIDR;
	public String showTitle;
	public String episodeTitle;
	public String seasonNb;
	public String episodeNb;
	public String tvID;
	public String tvNotes;

	public String movieTitle;
	public String movieDescription;
	public String movieTMSID;
	public String movieISAN;
	public String movieEIDR;
	public String movieID;
	public String movieNotes;

	public int     number;
	public boolean thumbnail;
	public String  thumbnailimage;
	public int     thumbnailId;

	public transient Integer identity;

	@Override public Integer getIdentity()
	{
		return identity;
	}

	@Override public String toString()
	{
		return title;
	}
}
