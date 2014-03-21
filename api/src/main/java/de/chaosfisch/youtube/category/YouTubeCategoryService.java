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
import de.chaosfisch.data.category.CategoryDTO;
import de.chaosfisch.data.category.ICategoryDAO;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

public class YouTubeCategoryService implements ICategoryService {
	private final SimpleListProperty<CategoryModel> categoryModels = new SimpleListProperty<>(
			FXCollections.observableArrayList());
	private final ICategoryDAO categoryDAO;

	@Inject
	public YouTubeCategoryService(final ICategoryDAO categoryDAO) {
		this.categoryDAO = categoryDAO;
		loadCategories();
	}

	private void loadCategories() {
		categoryModels.addAll(transformFromDTOs(categoryDAO.getAll()));
		Collections.sort(categoryModels);
	}

	private List<CategoryModel> transformFromDTOs(final Collection<CategoryDTO> categoryDTOs) {
		final ArrayList<CategoryModel> categories = new ArrayList<>(categoryDTOs.size());
		for (final CategoryDTO category : categoryDTOs) {
			final CategoryModel categoryModel = new CategoryModel();
			categoryModel.setYoutubeId(category.getYoutubeId());
			categoryModel.setName(category.getName());
			categories.add(categoryModel);
		}

		return categories;
	}

	@Override
	public void refresh(final YouTube youTube) throws IOException {
		final VideoCategoryListResponse videoCategoryListResponse = youTube.videoCategories()
																		   .list("id,snippet")
																		   .setHl(Locale.getDefault()
																						.getCountry())
																		   .setRegionCode(Locale.getDefault()
																								.getCountry())
																		   .execute();

		videoCategoryListResponse.getItems()
								 .forEach(t -> addOrUpdateCategory(t.getId(), t.getSnippet()));
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

			categoryDAO.store(transformToDTO(categoryModel));
			if (!categoryModels.get()
							   .contains(categoryModel)) {
				categoryModels.add(categoryModel);
			} else {
				categoryModels.set(categoryModels.indexOf(categoryModel), categoryModel);
			}
		}
	}

	private CategoryDTO transformToDTO(final CategoryModel categoryModel) {
		return new CategoryDTO(categoryModel.getYoutubeId(), categoryModel.getName());
	}

	public ObservableList<CategoryModel> getCategoryModels() {
		return categoryModels.get();
	}

	public void setCategoryModels(final ObservableList<CategoryModel> categoryModels) {
		this.categoryModels.set(categoryModels);
	}
}
