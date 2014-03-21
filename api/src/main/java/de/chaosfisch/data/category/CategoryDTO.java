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

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class CategoryDTO {
	private int    youtubeId;
	@NotNull
	private String name;

	public CategoryDTO() {
	}

	public CategoryDTO(final int youtubeId, @NotNull final String name) {
		this.youtubeId = youtubeId;
		this.name = name;
	}

	@NotNull
	public String getName() {
		return name;
	}

	public void setName(@NotNull final String name) {
		this.name = name;
	}

	public int getYoutubeId() {
		return youtubeId;
	}

	public void setYoutubeId(final int youtubeId) {
		this.youtubeId = youtubeId;
	}

	@Override
	public int hashCode() {
		int result = youtubeId;
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

		return youtubeId == that.youtubeId && name.equals(that.name);
	}

	@Override
	@NonNls
	public String toString() {
		return "CategoryDTO{" +
				"youtubeId=" + youtubeId +
				", name='" + name + '\'' +
				'}';
	}
}