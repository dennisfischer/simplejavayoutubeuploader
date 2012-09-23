package org.chaosfisch.youtubeuploader.services.youtube.spi;

import java.util.List;
import java.util.Locale;

import org.chaosfisch.google.atom.Category;

public interface CategoryService
{
	String	CATEGORY_URL	= "http://gdata.youtube.com/schemas/2007/categories.cat?hl=" + Locale.getDefault().getLanguage();

	List<Category> load();
}
