/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.atom;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import de.chaosfisch.google.atom.gdata.GDataComments;
import de.chaosfisch.google.atom.gdata.GDataRating;
import de.chaosfisch.google.atom.media.MediaGroup;
import de.chaosfisch.google.atom.youtube.*;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("entry")
public class VideoEntry {
	@XStreamAlias("yt:accessControl")
	@XStreamImplicit
	public final List<YoutubeAccessControl> accessControl = new ArrayList<>(10);

	@XStreamAlias("app:control")
	public AppControl appControl;

	@XStreamAlias("app:edited")
	public String        appEdited;
	public YoutubeAuthor author;

	@XStreamAlias("category")
	@XStreamImplicit
	public List<Category> categories;

	@XStreamAlias("gd:comments")
	public GDataComments  comments;
	public YoutubeContent content;

	@Deprecated
	@XStreamAlias("gd:rating")
	public GDataRating gdRating;

	@XStreamAlias("georss:where")
	public YoutubeGeoRss geoRssWhere;
	public String        id;

	@XStreamAlias("link")
	@XStreamImplicit
	public List<Feedlink> links;

	@XStreamAlias("yt:location")
	public String location;

	@XStreamAlias("media:group")
	public MediaGroup mediaGroup = new MediaGroup();

	@XStreamAlias("yt:countHint")
	public Integer playlistCountHint;

	@XStreamAlias("yt:playlistId")
	public String playlistId;

	@XStreamAlias("summary")
	public String playlistSummary;
	public String published;

	@XStreamAlias("yt:statistics")
	public YoutubeStatistics statistics;
	public String            title;
	public String            updated;

	@XStreamAlias("xmlns")
	@XStreamAsAttribute
	public String xmlns = "http://www.w3.org/2005/Atom";

	@XStreamAlias("xmlns:media")
	@XStreamAsAttribute
	public String xmlnsMedia = "http://search.yahoo.com/mrss/";

	@XStreamAlias("xmlns:yt")
	@XStreamAsAttribute
	public String xmlnsYt = "http://gdata.youtube.com/schemas/2007";

	@XStreamAlias("yt:availability")
	public YoutubeAvailability ytAvailability;

	@XStreamAlias("yt:episode")
	public YoutubeEpisode ytEpisode;

	@XStreamAlias("yt:firstReleased")
	public String ytFirstReleased;

	@XStreamAlias("yt:hd")
	public Object ytHD;

	@XStreamAlias("yt:noembed")
	public Object ytNoEmbed;

	@XStreamAlias("yt:position")
	public Integer ytPosition;

	@XStreamAlias("yt:private")
	public Object ytPrivate;

	@XStreamAlias("yt:rating")
	public YoutubeRating ytRating;

	@XStreamAlias("yt:recorded")
	public String ytRecorded;
}
