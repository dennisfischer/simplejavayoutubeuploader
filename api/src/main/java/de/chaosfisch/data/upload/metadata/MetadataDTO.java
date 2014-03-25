/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.data.upload.metadata;

public class MetadataDTO {
	private String uploadId;
	private int    category;
	private String license;
	private String title;
	private String description;
	private String tags;

	public MetadataDTO() {
	}

	public MetadataDTO(final String uploadId, final int category, final String license, final String title, final String description, final String tags) {
		this.uploadId = uploadId;
		this.category = category;
		this.license = license;
		this.title = title;
		this.description = description;
		this.tags = tags;
	}

	public String getUploadId() {
		return uploadId;
	}

	public void setUploadId(final String uploadId) {
		this.uploadId = uploadId;
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(final int category) {
		this.category = category;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(final String license) {
		this.license = license;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(final String tags) {
		this.tags = tags;
	}
}