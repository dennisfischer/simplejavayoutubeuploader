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

import com.google.common.base.Charsets;
import de.chaosfisch.google.youtube.upload.Upload;

import java.io.Serializable;

public class Metadata implements Serializable {

	private static final long serialVersionUID = -6095318226028745525L;
	private Category category;
	private String   title;
	private String   description;
	private String   keywords;
	private License  license;
	private int      version;

	public Metadata() {
		this("", Category.ANIMALS, "", "");
	}

	private Metadata(final String title, final Category category, final String description, final String keywords) {
		this(title, category, description, keywords, License.YOUTUBE);
	}

	public Metadata(final String title, final Category category, final String description, final String keywords, final License license) {
		setTitle(title);
		setCategory(category);
		setDescription(description);
		setKeywords(keywords);
		//noinspection CallToSimpleSetterFromWithinClass
		setLicense(license);
	}

	public Category getCategory() {
		return category;
	}

	void setCategory(final Category category) {
		if (null == category) {
			throw new IllegalArgumentException(Upload.Validation.CATEGORY);
		}
		this.category = category;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		if (null == title) {
			throw new IllegalArgumentException(Upload.Validation.TITLE);
		} else if (Upload.Validation.MAX_TITLE_SIZE < title.getBytes(Charsets.UTF_8).length) {
			throw new IllegalArgumentException(Upload.Validation.TITLE_SIZE);
		} else if (title.contains(">") || title.contains("<")) {
			throw new IllegalArgumentException(Upload.Validation.TITLE_CHARACTERS);
		}

		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		if (null == description) {
			this.description = "";
		} else if (Upload.Validation.MAX_DESCRIPTION_SIZE < description.getBytes().length) {
			throw new IllegalArgumentException(Upload.Validation.DESCRIPTION_SIZE);
		} else if (description.contains(">") || description.contains("<")) {
			throw new IllegalArgumentException(Upload.Validation.DESCRIPTION_CHARACTERS);
		} else {
			this.description = description;
		}
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(final String keywords) {
		if (null == keywords) {
			this.keywords = "";
		} else {
			if (!TagParser.areTagsValid(TagParser.parse(keywords))) {
				throw new IllegalArgumentException(Upload.Validation.KEYWORD);
			}
			this.keywords = keywords;
		}
	}

	public License getLicense() {
		return license;
	}

	void setLicense(final License license) {
		this.license = license;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(final int version) {
		this.version = version;
	}
}
