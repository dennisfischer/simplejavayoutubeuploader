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

import java.net.URL;
import java.util.List;

import org.chaosfisch.google.atom.media.MediaGroup;
import org.chaosfisch.google.atom.youtube.YoutubeAuthor;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("feed")
public class Feed {
	public YoutubeAuthor											author;
	public String													category;
	public String													generator;
	public String													id;
	public @XStreamAlias("openSearch:itemsPerPage") Integer			itemsPerPage;
	public @XStreamAlias("link") @XStreamImplicit List<Feedlink>	links;
	public URL														logo;
	public @XStreamAlias("media:group") MediaGroup					mediaGroup;
	public @XStreamAlias("yt:playlistId") String					playlistId;
	public @XStreamAlias("openSearch:startIndex") Integer			startIndex;
	public String													subtitle;
	public String													title;
	public @XStreamAlias("openSearch:totalResults") Integer			totalResults;
	public String													updated;
	public @XStreamAlias("entry") @XStreamImplicit List<VideoEntry>	videoEntries;
}
