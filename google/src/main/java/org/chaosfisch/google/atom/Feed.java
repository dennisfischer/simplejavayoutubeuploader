/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.google.atom;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.chaosfisch.google.atom.media.MediaGroup;
import org.chaosfisch.google.atom.youtube.YoutubeAuthor;

import java.net.URL;
import java.util.List;

@XStreamAlias("feed")
public class Feed {
	public YoutubeAuthor author;
	public String        category;
	public String        generator;
	public String        id;

	@XStreamAlias("openSearch:itemsPerPage")
	public Integer itemsPerPage;

	@XStreamAlias("link")
	@XStreamImplicit
	public List<Feedlink> links;
	public URL            logo;

	@XStreamAlias("media:group")
	public MediaGroup mediaGroup;
	@XStreamAlias("yt:playlistId")
	public String     playlistId;

	@XStreamAlias("openSearch:startIndex")
	public Integer startIndex;
	public String  subtitle;
	public String  title;

	@XStreamAlias("openSearch:totalResults")
	public Integer totalResults;
	public String  updated;

	@XStreamAlias("entry")
	@XStreamImplicit
	public List<VideoEntry> videoEntries;
}
