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

@XStreamAlias("media:content")
public class MediaContent
{
	public @XStreamAsAttribute Integer								duration;
	public @XStreamAsAttribute @XStreamAlias("yt:format") String	format;
	public @XStreamAsAttribute Boolean								isDefault;
	public @XStreamAsAttribute @XStreamAlias("yt:name") String		name;
	public @XStreamAsAttribute String								type;
	public @XStreamAsAttribute String								url;
}
