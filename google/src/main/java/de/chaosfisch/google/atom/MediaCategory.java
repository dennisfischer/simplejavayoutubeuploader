/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.google.atom;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;

@SuppressWarnings("FieldCanBeLocal")
@XStreamAlias("media:category")
@XStreamConverter(value = ToAttributedValueConverter.class, strings = {"category"})
public class MediaCategory {
	private final   String category;
	@XStreamAsAttribute
	protected final String label;
	@XStreamAsAttribute
	protected final String scheme;
	@XStreamAsAttribute
	protected final String term;

	public MediaCategory(final String term, final String label, final String scheme) {
		this.term = term;
		this.label = label;
		this.scheme = scheme;
		category = term;
	}
}
