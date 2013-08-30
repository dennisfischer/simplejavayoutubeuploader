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
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;

@XStreamAlias("media:credit")
@XStreamConverter(value = ToAttributedValueConverter.class, strings = {"credit"})
class MediaCredit {
	public String credit;

	@XStreamAsAttribute
	public String role;

	@XStreamAsAttribute
	public String scheme;

	@XStreamAsAttribute
	@XStreamAlias("yt:display")
	public String ytDisplay;

	@XStreamAsAttribute
	@XStreamAlias("yt:type")
	public String ytType;
}
