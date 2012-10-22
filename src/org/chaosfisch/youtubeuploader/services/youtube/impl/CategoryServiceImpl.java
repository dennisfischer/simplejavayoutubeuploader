/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.services.youtube.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.chaosfisch.google.atom.AppCategories;
import org.chaosfisch.google.atom.AtomCategory;
import org.chaosfisch.util.io.Request;
import org.chaosfisch.util.io.RequestHelper;
import org.chaosfisch.util.io.Request.Method;
import org.chaosfisch.util.XStreamHelper;
import org.chaosfisch.youtubeuploader.services.youtube.spi.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Ordering;

public class CategoryServiceImpl implements CategoryService
{
	private List<AtomCategory>	categories	= new ArrayList<AtomCategory>(20);

	Logger						logger		= LoggerFactory.getLogger(CategoryServiceImpl.class);

	@Override
	public List<AtomCategory> load()
	{
		try
		{
			final HttpResponse httpResponse = RequestHelper.execute(new Request.Builder(CATEGORY_URL, Method.GET).buildHttpUriRequest());
			if (httpResponse.getStatusLine().getStatusCode() == 200)
			{
				final AppCategories appCategories = XStreamHelper.parseFeed(EntityUtils.toString(httpResponse.getEntity()), AppCategories.class);

				final Iterator<AtomCategory> iterator = appCategories.categories.iterator();
				while (iterator.hasNext())
				{
					final AtomCategory atomCategory = iterator.next();

					if ((atomCategory.ytDeprecated != null) || (atomCategory.ytAssignable == null))
					{
						iterator.remove();
						continue;
					}
				}

				categories = appCategories.categories;

				Collections.sort(categories, Ordering.usingToString());
			} else
			{
				logger.warn("Couldn't fetch categories: {}", httpResponse.getStatusLine());
			}

		} catch (final IOException e)
		{
			logger.warn("I/O Error", e);
		}
		return categories;
	}
}
