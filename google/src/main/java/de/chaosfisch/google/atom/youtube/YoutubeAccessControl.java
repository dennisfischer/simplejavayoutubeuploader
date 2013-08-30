/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.atom.youtube;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;

@XStreamAlias("yt:accessControl")
@XStreamConverter(value = ToAttributedValueConverter.class, strings = {"access"})
public class YoutubeAccessControl {
	private String access;

	@XStreamAsAttribute
	private String action;

	@XStreamAsAttribute
	private String permission;

	@XStreamAsAttribute
	private String type;

	public YoutubeAccessControl() {

	}

	public YoutubeAccessControl(final String action, final String permission) {
		this(action, permission, null, null);
	}

	private YoutubeAccessControl(final String action, final String permission, final String type, final String access) {
		this.action = action;
		this.permission = permission;
		this.type = type;
		this.access = access;
	}
}
