package org.chaosfisch.youtubeuploader.services.youtube.impl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.chaosfisch.google.atom.Category;
import org.chaosfisch.google.request.Request;
import org.chaosfisch.google.request.Response;
import org.chaosfisch.youtubeuploader.services.youtube.spi.CategoryService;

public class CategoryServiceImpl implements CategoryService
{
	private List<Category>	categories;

	@Override
	public List<Category> load()
	{
		if (categories == null)
		{
			try
			{
				Request request = new Request.Builder("GET", new URL(CATEGORY_URL)).build();
				Response response = request.send();
				System.out.println(response.body);
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			categories = new ArrayList<Category>();
		}
		return categories;
	}
}
