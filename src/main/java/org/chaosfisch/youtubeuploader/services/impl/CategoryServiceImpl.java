/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.services.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.http.util.EntityUtils;
import org.chaosfisch.exceptions.SystemException;
import org.chaosfisch.google.atom.AppCategories;
import org.chaosfisch.google.atom.AtomCategory;
import org.chaosfisch.io.http.Request;
import org.chaosfisch.io.http.Response;
import org.chaosfisch.util.XStreamHelper;
import org.chaosfisch.youtubeuploader.ApplicationData;
import org.chaosfisch.youtubeuploader.services.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.collect.Ordering;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class CategoryServiceImpl implements CategoryService {
	@Inject
	@Named(value = ApplicationData.SERVICE_EXECUTOR)
	private ListeningExecutorService	service;

	private List<AtomCategory>			categories;
	private final Logger				logger			= LoggerFactory.getLogger(CategoryServiceImpl.class);

	/**
	 * The default category url
	 */
	private final String				CATEGORY_URL	= "http://gdata.youtube.com/schemas/2007/categories.cat?hl=" + Locale.getDefault()
															.getLanguage();

	@Override
	public List<AtomCategory> load() throws SystemException {
		logger.debug("Loading categories.");
		if (categories != null && !categories.isEmpty()) {
			logger.debug("Categories already existing.");
			return categories;
		}

		logger.debug("Sending http request");
		try (final Response response = new Request.Builder(CATEGORY_URL).get()
			.build()
			.execute();) {
			if (response.getStatusCode() != 200) {
				throw new SystemException(CategoryCode.UNEXPECTED_RESPONSE_CODE);
			}
			logger.debug("Received 200 response");
			final AppCategories appCategories = XStreamHelper.parseFeed(EntityUtils.toString(response.getEntity(), Charsets.UTF_8),
				AppCategories.class);

			if (appCategories.categories == null || appCategories.categories.isEmpty()) {
				throw new SystemException(CategoryCode.CATGORIES_EMPTY);
			}

			logger.debug("Removing deprecated and unassignable categories");
			final Iterator<AtomCategory> iterator = appCategories.categories.iterator();
			while (iterator.hasNext()) {
				final AtomCategory atomCategory = iterator.next();

				if (atomCategory.ytDeprecated != null || atomCategory.ytAssignable == null) {
					iterator.remove();
					continue;
				}
			}
			logger.debug("Sorting categories by name.");
			Collections.sort(appCategories.categories, Ordering.usingToString());
			categories = appCategories.categories;
			return categories;
		} catch (final IOException e) {
			throw SystemException.wrap(e, CategoryCode.LOAD_IO_ERROR);
		}
	}
}
