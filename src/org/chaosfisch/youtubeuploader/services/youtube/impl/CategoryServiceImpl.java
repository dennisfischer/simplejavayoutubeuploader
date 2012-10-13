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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.chaosfisch.google.atom.AppCategories;
import org.chaosfisch.google.atom.AtomCategory;
import org.chaosfisch.util.InputStreams;
import org.chaosfisch.youtubeuploader.services.youtube.spi.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Ordering;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class CategoryServiceImpl implements CategoryService
{
	private List<AtomCategory>	categories	= new ArrayList<AtomCategory>(20);

	Logger						logger		= LoggerFactory.getLogger(CategoryServiceImpl.class);

	@Override
	public List<AtomCategory> load()
	{
		final HttpGet httpGet = new HttpGet(CATEGORY_URL);
		final DefaultHttpClient defaultHttpClient = new DefaultHttpClient();

		try
		{
			final HttpResponse httpResponse = defaultHttpClient.execute(httpGet);

			final XStream xstream = new XStream(new DomDriver("UTF-8"));
			xstream.processAnnotations(AppCategories.class);
			final String xml = InputStreams.toString(httpResponse.getEntity().getContent());
			final AppCategories appCategories = (AppCategories) xstream.fromXML(xml);
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

		} catch (final IOException e)
		{
			logger.warn("I/O Error", e);
		}
		return categories;
	}
}
