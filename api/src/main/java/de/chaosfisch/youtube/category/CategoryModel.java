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

import de.chaosfisch.data.UniqueObject;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import org.jetbrains.annotations.NotNull;

public class CategoryModel implements UniqueObject<CategoryDTO>, Comparable<CategoryModel> {
	private final SimpleIntegerProperty youtubeId = new SimpleIntegerProperty();
	private final SimpleStringProperty  name      = new SimpleStringProperty();

	public SimpleStringProperty nameProperty() {
		return name;
	}


	@Override
	public int hashCode() {
		return youtubeId.get();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CategoryModel)) {
			return false;
		}
		final CategoryModel that = (CategoryModel) obj;
		return youtubeId.get() == that.getYoutubeId();
	}

	@Override
	public String toString() {
		return getName();
	}

	public String getName() {
		return name.get();
	}

	public void setName(final String name) {
		this.name.set(name);
	}

	public SimpleIntegerProperty youtubeIdProperty() {
		return youtubeId;
	}

	@Override
	public String uniqueId() {
		return String.valueOf(youtubeId.get());
	}

	@Override
	public CategoryDTO toDTO() {
		return new CategoryDTO(name.get(), youtubeId.get());
	}

	@Override
	public void fromDTO(final CategoryDTO o) {
		setName(o.getName());
		setYoutubeId(o.getYoutubeId());
	}

	public int getYoutubeId() {
		return youtubeId.get();
	}

	public void setYoutubeId(final int youtubeId) {
		this.youtubeId.set(youtubeId);
	}


	@Override
	public int compareTo(@NotNull final CategoryModel o) {
		if (0 > name.get().compareTo(o.name.get())) {
			return -1;
		} else if (0 < name.get().compareTo(o.name.get())) {
			return 1;
		}
		return 0;
	}
}
