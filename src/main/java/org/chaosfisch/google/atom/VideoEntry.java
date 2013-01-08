/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.google.atom;

import java.util.ArrayList;
import java.util.List;

import org.chaosfisch.google.atom.gdata.GDataComments;
import org.chaosfisch.google.atom.gdata.GDataRating;
import org.chaosfisch.google.atom.media.MediaGroup;
import org.chaosfisch.google.atom.youtube.YoutubeAccessControl;
import org.chaosfisch.google.atom.youtube.YoutubeAuthor;
import org.chaosfisch.google.atom.youtube.YoutubeAvailability;
import org.chaosfisch.google.atom.youtube.YoutubeContent;
import org.chaosfisch.google.atom.youtube.YoutubeEpisode;
import org.chaosfisch.google.atom.youtube.YoutubeGeoRss;
import org.chaosfisch.google.atom.youtube.YoutubeRating;
import org.chaosfisch.google.atom.youtube.YoutubeStatistics;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("entry")
public class VideoEntry
{
	public @XStreamAlias("yt:accessControl") @XStreamImplicit List<YoutubeAccessControl>	accessControl	= new ArrayList<YoutubeAccessControl>();
	public @XStreamAlias("app:control") AppControl											appControl;
	public @XStreamAlias("app:edited") String												appEdited;
	public YoutubeAuthor																	author;
	public @XStreamAlias("category") @XStreamImplicit List<Category>						categories;
	public @XStreamAlias("gd:comments") GDataComments										comments;
	public YoutubeContent																	content;
	public @Deprecated @XStreamAlias("gd:rating") GDataRating								gdRating;
	public @XStreamAlias("georss:where") YoutubeGeoRss										geoRssWhere;
	public String																			id;
	public @XStreamAlias("link") @XStreamImplicit List<Feedlink>							links;
	public @XStreamAlias("yt:location") String												location;
	public @XStreamAlias("media:group") MediaGroup											mediaGroup		= new MediaGroup();
	public @XStreamAlias("yt:countHint") Integer											playlistCountHint;
	public @XStreamAlias("yt:playlistId") String											playlistId;
	public @XStreamAlias("summary") String													playlistSummary;
	public String																			published;
	public @XStreamAlias("yt:statistics") YoutubeStatistics									statistics;
	public String																			title;
	public String																			updated;
	@XStreamAlias("xmlns") @XStreamAsAttribute String										xmlns			= "http://www.w3.org/2005/Atom";
	@XStreamAlias("xmlns:media") @XStreamAsAttribute String									xmlnsMedia		= "http://search.yahoo.com/mrss/";
	@XStreamAlias("xmlns:yt") @XStreamAsAttribute String									xmlnsYt			= "http://gdata.youtube.com/schemas/2007";
	public @XStreamAlias("yt:availability") YoutubeAvailability								ytAvailability;
	public @XStreamAlias("yt:episode") YoutubeEpisode										ytEpisode;
	public @XStreamAlias("yt:firstReleased") String											ytFirstReleased;
	public @XStreamAlias("yt:hd") Object													ytHD;
	public @XStreamAlias("yt:noembed") Object												ytNoEmbed;
	public @XStreamAlias("yt:position") Integer												ytPosition;
	public @XStreamAlias("yt:private") Object												ytPrivate;
	public @XStreamAlias("yt:rating") YoutubeRating											ytRating;
	public @XStreamAlias("yt:recorded") String												ytRecorded;
}
