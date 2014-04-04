/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.upload.metadata;

import de.chaosfisch.data.upload.metadata.MetadataDTO;
import de.chaosfisch.youtube.category.CategoryModel;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class Metadata {

	private final SimpleObjectProperty<CategoryModel> category    = new SimpleObjectProperty<>(new CategoryModel());
	private final SimpleStringProperty                description = new SimpleStringProperty("");
	private final SimpleObjectProperty<License>       license     = new SimpleObjectProperty<>(License.YOUTUBE);
	private final SimpleStringProperty                tags        = new SimpleStringProperty("");
	private final SimpleStringProperty                title       = new SimpleStringProperty("");

	public Metadata() {
	}

	public Metadata(final MetadataDTO metadataDTO, final CategoryModel categoryModel) {
		category.set(categoryModel);
		description.set(metadataDTO.getDescription());
		license.set(License.valueOf(metadataDTO.getLicense()));
		tags.set(metadataDTO.getTags());
		title.set(metadataDTO.getTitle());
	}

	public CategoryModel getCategory() {
		return category.get();
	}

	public void setCategory(final CategoryModel category) {
		this.category.set(category);
	}

	public SimpleObjectProperty<CategoryModel> categoryProperty() {
		return category;
	}

	public License getLicense() {
		return license.get();
	}

	public void setLicense(final License license) {
		this.license.set(license);
	}

	public SimpleObjectProperty<License> licenseProperty() {
		return license;
	}

	public String getTitle() {
		return title.get();
	}

	public void setTitle(final String title) {
		this.title.set(title);
	}

	public SimpleStringProperty titleProperty() {
		return title;
	}

	public String getDescription() {
		return description.get();
	}

	public void setDescription(final String description) {
		this.description.set(description);
	}

	public SimpleStringProperty descriptionProperty() {
		return description;
	}

	public String getTags() {
		return tags.get();
	}

	public void setTags(final String tags) {
		this.tags.set(tags);
	}

	public SimpleStringProperty tagsProperty() {
		return tags;
	}

	public String getLicenseIdentifier() {
		return license.get()
					  .getIdentifier();
	}

	public int getCategoryId() {
		return category.get()
					   .getYoutubeId();
	}
}
