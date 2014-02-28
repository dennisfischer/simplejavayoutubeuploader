/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.category;

import java.util.List;

class CategoryFeed {
	private String             kind;
	private String             etag;
	private List<CategoryItem> items;

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

	public List<CategoryItem> getItems() {
		return items;
	}

	public void setItems(final List<CategoryItem> items) {
		this.items = items;
	}
}
