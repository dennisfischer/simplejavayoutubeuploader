/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.atom.media;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import de.chaosfisch.google.atom.Category;
import de.chaosfisch.google.atom.youtube.YoutubeDuration;

import java.util.List;

@XStreamAlias("media:group")
public class MediaGroup {

	@XStreamAlias("yt:aspectRatio")
	public String aspectRatio;

	@XStreamAlias("media:category")
	@XStreamImplicit
	public List<Category> category;

	@XStreamAlias("media:content")
	@XStreamImplicit
	public List<MediaContent> content;

	@XStreamAlias("media:credit")
	@XStreamImplicit
	public List<MediaCredit> credit;

	@XStreamAlias("media:description")
	public String description;

	@XStreamAlias("yt:duration")
	public YoutubeDuration duration;

	@XStreamAlias("media:keywords")
	public String keywords;

	@XStreamAlias("media:license")
	public String license;

	@XStreamAlias("media:player")
	public MediaPlayer player;

	@XStreamAlias("media:rating")
	public MediaRating rating;

	@XStreamAlias("media:restriction")
	public MediaRestriction restriction;

	@XStreamAlias("media:thumbnail")
	@XStreamImplicit
	public List<MediaThumbnail> thumbnails;

	@XStreamAlias("media:title")
	public String title;

	@XStreamAlias("yt:uploaded")
	public String uploaded;

	@XStreamAlias("yt:uploaderId")
	public String uploaderId;

	@XStreamAlias("yt:videoid")
	public String videoID;

	@XStreamAlias("yt:private")
	public Object ytPrivate;
}
