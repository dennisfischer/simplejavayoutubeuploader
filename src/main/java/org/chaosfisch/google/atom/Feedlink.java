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

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("gd:feedLink")
public class Feedlink {
	public
	@XStreamAlias("gd:countHint")
	@XStreamAsAttribute
	Integer countHint;
	public
	@XStreamAlias("yt:hasEntries")
	@XStreamAsAttribute
	Boolean hasEntries;
	public
	@XStreamAsAttribute
	String  href;
	public
	@XStreamAsAttribute
	String  rel;
	public
	@XStreamAsAttribute
	String  type;
}
