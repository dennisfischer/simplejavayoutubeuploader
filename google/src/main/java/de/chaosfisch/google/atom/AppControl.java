/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.atom;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import de.chaosfisch.google.atom.youtube.YoutubeState;

@XStreamAlias("app:control")
public class AppControl {

	@XStreamAlias("app:draft")
	public String draft;

	@XStreamAlias("yt:state")
	public YoutubeState state;
}
