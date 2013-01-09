/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class XStreamHelper {
	final static XStream	xStream	= new XStream(new DomDriver("UTF-8"));
	
	public static String parseObjectToFeed(final Object o) {
		xStream.processAnnotations(o.getClass());
		return xStream.toXML(o);
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
