/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.google.category;

class CategoryItem {
	private String          kind;
	private String          etag;
	private String          id;
	private CategorySnippet snippet;

	public String getKind() {
		return kind;
	}

	public void setKind(final String kind) {
		this.kind = kind;
	}

	public String getEtag() {
		return etag;
	}

	public void setEtag(final String etag) {
		this.etag = etag;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public CategorySnippet getSnippet() {
		return snippet;
	}

	public void setSnippet(final CategorySnippet snippet) {
		this.snippet = snippet;
	}

/*	public boolean isAssignable() {
		return snippet.isAssignable();
	}

	public String getTitle() {
		return snippet.getTitle();
	}*/
}
