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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("media:thumbnail")
public class MediaThumbnail {
	public @XStreamAlias("height")
	@XStreamAsAttribute
	String	height;
	public @XStreamAlias("yt:name")
	@XStreamAsAttribute
	String	name;
	public @XStreamAlias("time")
	@XStreamAsAttribute
	String	time;
	public @XStreamAlias("url")
	@XStreamAsAttribute
	String	url;
	public @XStreamAlias("width")
	@XStreamAsAttribute
	String	width;
}
