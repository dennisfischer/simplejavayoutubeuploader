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
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("media:thumbnail")
class MediaThumbnail {

	@XStreamAlias("height")
	@XStreamAsAttribute
	public String height;

	@XStreamAlias("yt:name")
	@XStreamAsAttribute
	public String name;

	@XStreamAlias("time")
	@XStreamAsAttribute
	public String time;

	@XStreamAlias("url")
	@XStreamAsAttribute
	public String url;

	@XStreamAlias("width")
	@XStreamAsAttribute
	public String width;
}
