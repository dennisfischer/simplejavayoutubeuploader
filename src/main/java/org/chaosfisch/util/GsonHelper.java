package org.chaosfisch.util;

import com.google.gson.Gson;

public class GsonHelper {

	private static final Gson	gson	= new Gson();

	public static String toJSON(final Object object) {
		return gson.toJson(object);
	}

	public static <T> T fromJSON(final String json, final Class<T> clazz) {
		return gson.fromJson(json, clazz);
	}

}
