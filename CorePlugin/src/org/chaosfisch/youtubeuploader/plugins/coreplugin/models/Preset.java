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

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 07.01.12
 * Time: 18:34
 * To change this template use File | Settings | File Templates.
 */
public class Preset implements IModel
{
	public boolean autotitle;
	public String  autotitleFormat;
	public String  category;
	public short   comment;
	public boolean commentvote;
	public String  defaultDir;
	public String  description;
	public boolean embed;
	public String  keywords;
	public boolean mobile;
	public String  name;
	public short   numberModifier;
	public short   videoresponse;
	public short   visibility;
	public boolean rate;
	public boolean monetize;
	public boolean monetizeOverlay;
	public boolean monetizeTrueview;
	public boolean monetizeProduct;
	public String  enddir;
	public short   license;

	public transient Integer  identity;
	public transient Account  account;
	public transient Playlist playlist;

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

	@Override public Integer getIdentity()
	{
		return identity;
	}

	@Override public String toString()
	{
		return name;
	}
}
