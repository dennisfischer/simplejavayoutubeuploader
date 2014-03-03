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

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.VideoCategoryListResponse;
import com.google.api.services.youtube.model.VideoCategorySnippet;
import de.chaosfisch.data.IDataStore;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Locale;

public class YouTubeCategoryService implements ICategoryService {
	private final SimpleListProperty<CategoryModel> categoryModels = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final IDataStore<CategoryModel, CategoryModel.CategoryDTO> dataStore;

	@Inject
	public YouTubeCategoryService(final IDataStore<CategoryModel, CategoryModel.CategoryDTO> dataStore) {
		this.dataStore = dataStore;
		loadCategories();
	}

	private void loadCategories() {
		categoryModels.addAll(dataStore.loadAll());
	}

	@Override
	public void refresh(final YouTube youTube) throws IOException {
		final VideoCategoryListResponse videoCategoryListResponse = youTube.videoCategories().list("id,snippet")
				.setHl(Locale.getDefault().getCountry())
				.setRegionCode(Locale.getDefault().getCountry()).execute();

		videoCategoryListResponse.getItems().forEach(t -> addOrUpdateCategory(t.getId(), t.getSnippet()));
	}

	@Override
	public SimpleListProperty<CategoryModel> categoryModelsProperty() {
		return categoryModels;
	}

	private void addOrUpdateCategory(final String id, final VideoCategorySnippet snippet) {
		if (snippet.getAssignable()) {
			final CategoryModel categoryModel = new CategoryModel();
			categoryModel.setName(snippet.getTitle());
			categoryModel.setYoutubeId(Integer.parseInt(id));

			dataStore.store(categoryModel);
			if (!categoryModels.contains(categoryModel)) {
				categoryModels.add(categoryModel);
			} else {
				categoryModels.set(categoryModels.indexOf(categoryModel), categoryModel);
			}
		}
	}

	public ObservableList<CategoryModel> getCategoryModels() {
		return categoryModels.get();
	}

	public void setCategoryModels(final ObservableList<CategoryModel> categoryModels) {
		this.categoryModels.set(categoryModels);
	}
}
