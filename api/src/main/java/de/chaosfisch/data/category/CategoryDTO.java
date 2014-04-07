/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.data.category;

public class CategoryDTO {

	private int    categoryId;
	private String name;

	public CategoryDTO() {
	}

	public CategoryDTO(final int categoryId, final String name) {
		this.categoryId = categoryId;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(final int categoryId) {
		this.categoryId = categoryId;
	}

	@Override
	public int hashCode() {
		int result = categoryId;
		result = 31 * result + name.hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CategoryDTO)) {
			return false;
		}

		final CategoryDTO that = (CategoryDTO) obj;

		return categoryId == that.categoryId && name.equals(that.name);
	}

	@Override
	public String toString() {
		return "CategoryDTO{" +
				"categoryId=" + categoryId +
				", name='" + name + '\'' +
				'}';
	}
}