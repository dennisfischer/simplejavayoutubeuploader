/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */
package org.chaosfisch.google.atom;

import org.chaosfisch.google.atom.youtube.YoutubeState;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("app:control")
public class AppControl {
	public @XStreamAlias("app:draft")
	String			draft;
	public @XStreamAlias("yt:state")
	YoutubeState	state;
}
