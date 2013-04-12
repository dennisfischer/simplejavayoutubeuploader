/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.db.data;

import org.chaosfisch.google.atom.AtomCategory;
import org.chaosfisch.util.TextUtil;

public enum Category {
	FILM("Film", "category.film"), AUTOS("Autos", "category.autos"), MUSIC("Music", "category.music"), ANIMALS("Animals",
			"category.animals"), SPORTS("Sports", "category.sports"), TRAVEL("Travel", "category.travel"), GAMES("Games", "category.games"), PEOPLE(
			"People", "category.people"), COMEDY("Comedy", "category.comedy"), ENTERTAINMENT("Entertainment", "category.entertainment"), NEWS(
			"News", "category.news"), HOWTO("Howto", "category.howto"), EDUCATION("Education", "category.education"), TECH("Tech",
			"category.tech"), NONPROFIT("Nonprofit", "category.nonprofit");

	private String				term;
	private String				label;
	private String				scheme;

	private final static String	defaultScheme	= "http://gdata.youtube.com/schemas/2007/categories.cat";

	private Category(final String term, final String label) {
		this(term,
			label,
			defaultScheme);
	}

	private Category(final String term, final String label, final String scheme) {
		this.term = term;
		this.label = label;
		this.scheme = scheme;
	}

	public AtomCategory toCategory() {
		return new AtomCategory(term,
			label,
			scheme);
	}

	@Override
	public String toString() {
		return TextUtil.getString(label);
	}
}
