/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.google.atom.youtube;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;

@XStreamAlias("yt:accessControl")
@XStreamConverter(value = ToAttributedValueConverter.class, strings = { "access" })
public class YoutubeAccessControl
{
	public String						access;
	public @XStreamAsAttribute String	action;
	public @XStreamAsAttribute String	permission;
	public @XStreamAsAttribute String	type;

	public YoutubeAccessControl()
	{

	}

	public YoutubeAccessControl(final String action, final String permission)
	{
		this(action, permission, null, null);
	}

	public YoutubeAccessControl(final String action, final String permission, final String type, final String access)
	{
		this.action = action;
		this.permission = permission;
		this.type = type;
		this.access = access;
	}
}
