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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import de.chaosfisch.google.GDataConfig;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class GoogleCategoryService implements ICategoryService {
	private static final String                            CATEGORY_URL        = "https://www.googleapis.com/youtube/v3/videoCategories?part=id,snippet&hl=%s&regionCode=%s&key=%s";
	private static final Logger                            LOGGER              = LoggerFactory.getLogger(GoogleCategoryService.class);
	private static final int                               NOT_MODIFIED_HEADER = 304;
	private final        SimpleListProperty<CategoryModel> categoryModels      = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final        Gson                              gson                = new GsonBuilder().create();


	@Override
	public void refresh() {
		//TODO USE ETAG
		Unirest.get(String.format(CATEGORY_URL, Locale.getDefault().getCountry(), Locale.getDefault().getCountry(), GDataConfig.ACCESS_KEY))
				.header("If-None-Match", "\"79S54kzisD_9SOTfQLu_0TVQSpY/989lLPchL_UFmD-nVHyKAQ3PmHg\"")
				.header("Accept-Encoding", "gzip")
				.asStringAsync(new Callback<String>() {
					@Override
					public void completed(final HttpResponse<String> response) {
						if (NOT_MODIFIED_HEADER == response.getCode()) {
							LOGGER.info("Categories up to date!");
							return;
						}
						LOGGER.info("Category refresh succeeded");
						final CategoryFeed categoryFeed = gson.fromJson(response.getBody(), CategoryFeed.class);
						System.out.println(categoryFeed.getEtag());
						for (final CategoryItem item : categoryFeed.getItems()) {
							if (item.getSnippet().isAssignable()) {
								final CategoryModel categoryModel = new CategoryModel();
								categoryModel.setName(item.getSnippet().getTitle());
								categoryModel.setYoutubeId(Integer.parseInt(item.getId()));
								if (!categoryModels.contains(categoryModel)) {
									categoryModels.add(categoryModel);
								} else {
									categoryModels.set(categoryModels.indexOf(categoryModel), categoryModel);
								}
							}
						}
					}

					@Override
					public void failed(final UnirestException e) {
						LOGGER.warn("Failed refreshing categories!", e);
					}

					@Override
					public void cancelled() {
						LOGGER.info("Category refresh canceled");
					}
				});
	}

	@Override
	public SimpleListProperty<CategoryModel> categoryModelsProperty() {
		return categoryModels;
	}

	public ObservableList<CategoryModel> getCategoryModels() {
		return categoryModels.get();
	}

	public void setCategoryModels(final ObservableList<CategoryModel> categoryModels) {
		this.categoryModels.set(categoryModels);
	}
}
