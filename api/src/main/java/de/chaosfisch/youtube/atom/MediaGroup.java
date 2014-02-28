/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.atom;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import java.util.List;

@XStreamAlias("media:group")
public class MediaGroup {

	@XStreamAlias("yt:aspectRatio")
	@XStreamOmitField
	public Object aspectRatio;

	@XStreamAlias("media:category")
	@XStreamImplicit
	public List<MediaCategory> category;

	@XStreamAlias("media:content")
	@XStreamImplicit
	@XStreamOmitField
	public Object content;

	@XStreamAlias("media:credit")
	@XStreamImplicit
	@XStreamOmitField
	public Object credit;

	@XStreamAlias("media:description")
	public String description;

	@XStreamAlias("yt:duration")
	@XStreamOmitField
	public Object duration;

	@XStreamAlias("media:keywords")
	public String keywords;

	@XStreamAlias("media:license")
	public String license;

	@XStreamAlias("media:player")
	@XStreamOmitField
	public Object player;

	@XStreamAlias("media:rating")
	@XStreamOmitField
	public Object rating;

	@XStreamAlias("media:restriction")
	@XStreamOmitField
	public Object restriction;

	@XStreamAlias("media:thumbnail")
	@XStreamImplicit
	@XStreamOmitField
	public Object thumbnails;

	@XStreamAlias("media:title")
	public String title;

	@XStreamAlias("yt:uploaded")
	@XStreamOmitField
	public Object uploaded;

	@XStreamAlias("yt:uploaderId")
	@XStreamOmitField
	public Object uploaderId;

	@XStreamAlias("yt:videoid")
	public String videoID;

	@XStreamAlias("yt:private")
	public Object ytPrivate;
}
