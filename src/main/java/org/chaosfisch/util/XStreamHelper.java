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

import com.google.common.base.Charsets;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public final class XStreamHelper {
	private final static XStream xStream = new XStream(new DomDriver(Charsets.UTF_8.name()));

	public static String parseObjectToFeed(final Object o) {
		xStream.processAnnotations(o.getClass());
		return xStream.toXML(o);
	}

	public static void parseObjectToFile(final Object o, final File file) throws IOException {
		try (FileOutputStream fio = new FileOutputStream(file)) {
			xStream.processAnnotations(o.getClass());
			xStream.toXML(o, fio);
		}
	}

	public static <T> T parseFeed(final File xml, final Class<T> clazz) {
		xStream.processAnnotations(clazz);
		return getObject(clazz, xStream.fromXML(xml));
	}

	public static <T> T parseFeed(final String atomData, final Class<T> clazz) {
		xStream.processAnnotations(clazz);
		return getObject(clazz, xStream.fromXML(atomData));
	}

	private static <T> T getObject(final Class<T> clazz, final Object o) {
		if (clazz.isInstance(o)) {
			return clazz.cast(o);
		}
		throw new IllegalArgumentException("atomData of invalid clazz object!");
	}
}
