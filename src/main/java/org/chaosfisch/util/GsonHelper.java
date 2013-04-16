/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.util;

import com.google.gson.Gson;

public final class GsonHelper {

    private static final Gson gson = new Gson();

    public static String toJSON(final Object object) {
        return gson.toJson(object);
    }

    public static <T> T fromJSON(final String json, final Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

}
