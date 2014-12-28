/*
 * Copyright (c) 2014 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.atom.gdata;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("gd:rating")
public class GDataRating {

	@XStreamAsAttribute
	public Double average;

	@XStreamAsAttribute
	public Integer max;

	@XStreamAsAttribute
	public Integer min;

	@XStreamAsAttribute
	public Integer numRaters;

	@XStreamAsAttribute
	public Integer value;
}
