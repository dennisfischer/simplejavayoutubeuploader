/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.google.atom.media;

import java.util.List;

import org.chaosfisch.google.atom.youtube.YoutubeDuration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("media:group")
public class MediaGroup {
	public @XStreamAlias("yt:aspectRatio")
	String					aspectRatio;
	public @XStreamAlias("media:category")
	@XStreamImplicit
	List<MediaCategory>		category;
	public @XStreamAlias("media:content")
	@XStreamImplicit
	List<MediaContent>		content;
	public @XStreamAlias("media:credit")
	@XStreamImplicit
	List<MediaCredit>		credit;
	public @XStreamAlias("media:description")
	String					description;
	public @XStreamAlias("yt:duration")
	YoutubeDuration			duration;
	public @XStreamAlias("media:keywords")
	String					keywords;
	public @XStreamAlias("media:license")
	String					license;
	public @XStreamAlias("media:player")
	MediaPlayer				player;
	public @XStreamAlias("media:rating")
	MediaRating				rating;
	public @XStreamAlias("media:restriction")
	MediaRestriction		restriction;
	public @XStreamAlias("media:thumbnail")
	@XStreamImplicit
	List<MediaThumbnail>	thumbnails;
	public @XStreamAlias("media:title")
	String					title;
	public @XStreamAlias("yt:uploaded")
	String					uploaded;
	public @XStreamAlias("yt:uploaderId")
	String					uploaderId;
	public @XStreamAlias("yt:videoid")
	String					videoID;
	public @XStreamAlias("yt:private")
	Object					ytPrivate;
}
