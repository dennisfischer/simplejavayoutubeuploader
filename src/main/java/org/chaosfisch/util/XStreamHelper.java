package org.chaosfisch.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class XStreamHelper
{
	final static XStream	xStream	= new XStream(new DomDriver("UTF-8"));

	public static String parseObjectToFeed(final Object o)
	{
		xStream.processAnnotations(o.getClass());
		return xStream.toXML(o);
	}

	public static <T> T parseFeed(final String atomData, final Class<T> clazz)
	{
		xStream.processAnnotations(clazz);
		final Object o = xStream.fromXML(atomData);
		if (clazz.isInstance(o)) { return clazz.cast(o); }
		throw new IllegalArgumentException("atomData of invalid clazz object!");
	}
}
