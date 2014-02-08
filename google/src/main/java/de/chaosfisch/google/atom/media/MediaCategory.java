/*
 * Copyright (c) 2014 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.atom.media;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;
import de.chaosfisch.google.atom.Category;

@SuppressWarnings("FieldCanBeLocal")
@XStreamAlias("media:category")
@XStreamConverter(value = ToAttributedValueConverter.class, strings = {"category"})
public class MediaCategory extends Category {
	private final String category;

	public MediaCategory(final String term, final String label, final String scheme) {
		this.term = term;
		this.label = label;
		this.scheme = scheme;
		category = term;
	}
}
