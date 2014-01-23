/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.upload.metadata;

import de.chaosfisch.google.atom.media.MediaCategory;

public enum Category {
	FILM("Film"), AUTOS("Autos"), MUSIC("Music"), ANIMALS("Animals"), SPORTS("Sports"), TRAVEL("Travel"), GAMES("Games"), PEOPLE("People"), COMEDY("Comedy"), ENTERTAINMENT("Entertainment"), NEWS("News"), HOWTO("Howto"), EDUCATION("Education"), TECH("Tech"), NONPROFIT("Nonprofit");

	private final String term;
	private final String label;
	private final String scheme;

	private static final String DEFAULT_SCHEME = "http://gdata.youtube.com/schemas/2007/categories.cat";

	Category(final String term) {
		this(term, DEFAULT_SCHEME);
	}

	Category(final String term, final String scheme) {
		this.term = term;
		label = term;
		this.scheme = scheme;
	}

	public MediaCategory toCategory() {
		return new MediaCategory(term, label, scheme);
	}
}
