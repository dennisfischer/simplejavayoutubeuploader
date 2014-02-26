/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.google.atom;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("entry")
public class VideoEntry {
	@XStreamAlias("yt:accessControl")
	@XStreamImplicit
	public final List<YoutubeAccessControl> accessControl = new ArrayList<>(10);
	@XStreamAlias("media:group")
	public final MediaGroup                 mediaGroup    = new MediaGroup();
	@XStreamAlias("app:control")
	@XStreamOmitField
	public Object appControl;
	@XStreamAlias("app:edited")
	@XStreamOmitField
	public Object appEdited;
	@XStreamOmitField
	public Object author;
	@XStreamAlias("category")
	@XStreamImplicit
	@XStreamOmitField
	public Object categories;
	@XStreamAlias("gd:comments")
	@XStreamOmitField
	public Object comments;
	@XStreamOmitField
	public Object content;
	@Deprecated
	@XStreamAlias("gd:rating")
	@XStreamOmitField
	public Object gdRating;
	@XStreamAlias("georss:where")
	@XStreamOmitField
	public Object geoRssWhere;
	@XStreamOmitField
	public Object id;
	@XStreamAlias("link")
	@XStreamImplicit
	@XStreamOmitField
	public Object links;
	@XStreamAlias("yt:location")
	@XStreamOmitField
	public Object location;
	@XStreamAlias("yt:countHint")
	@XStreamOmitField
	public Object playlistCountHint;

	@XStreamAlias("yt:playlistId")
	@XStreamOmitField
	public Object playlistId;

	@XStreamAlias("summary")
	@XStreamOmitField
	public Object playlistSummary;
	@XStreamOmitField
	public Object published;

	@XStreamAlias("yt:statistics")
	@XStreamOmitField
	public Object statistics;
	@XStreamOmitField
	public Object title;
	@XStreamOmitField
	public Object updated;

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
	@XStreamOmitField
	public Object ytAvailability;

	@XStreamAlias("yt:episode")
	@XStreamOmitField
	public Object ytEpisode;

	@XStreamAlias("yt:firstReleased")
	@XStreamOmitField
	public Object ytFirstReleased;

	@XStreamAlias("yt:hd")
	@XStreamOmitField
	public Object ytHD;

	@XStreamAlias("yt:noembed")
	@XStreamOmitField
	public Object ytNoEmbed;

	@XStreamAlias("yt:position")
	@XStreamOmitField
	public Object ytPosition;

	@XStreamAlias("yt:private")
	public Object ytPrivate;

	@XStreamAlias("yt:rating")
	@XStreamOmitField
	public Object ytRating;

	@XStreamAlias("yt:recorded")
	@XStreamOmitField
	public Object ytRecorded;
}
