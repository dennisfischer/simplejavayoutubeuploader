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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.google.common.base.Charsets;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class XStreamHelper {
	final static XStream	xStream	= new XStream(new DomDriver(Charsets.UTF_8.name()));

	public static String parseObjectToFeed(final Object o) {
		xStream.processAnnotations(o.getClass());
		return xStream.toXML(o);
	}

	public static void parseObjectToFile(final Object o, final File file) throws FileNotFoundException {
		FileOutputStream fio = null;
		try {
			fio = new FileOutputStream(file);
			xStream.processAnnotations(o.getClass());
			xStream.toXML(o, fio);
		} finally {
			if (fio != null) {
				try {
					fio.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static <T> T parseFileToObject(final File xml, final Class<T> clazz) {
		xStream.processAnnotations(clazz);
		final Object o = xStream.fromXML(xml);
		if (clazz.isInstance(o)) {
			return clazz.cast(o);
		}
		throw new IllegalArgumentException("atomData of invalid clazz object!");
	}

	public static <T> T parseFeed(final String atomData, final Class<T> clazz) {
		xStream.processAnnotations(clazz);
		final Object o = xStream.fromXML(atomData);
		if (clazz.isInstance(o)) {
			return clazz.cast(o);
		}
		throw new IllegalArgumentException("atomData of invalid clazz object!");
	}
}
