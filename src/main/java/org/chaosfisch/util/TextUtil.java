package org.chaosfisch.util;

import java.util.ResourceBundle;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class TextUtil {

	@Inject
	@Named("i18n-resources")
	static ResourceBundle	resources;

	public static String getString(final String key) {
		return resources.getString(key);
	}

}
