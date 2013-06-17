/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.serialization;

import com.google.common.base.Charsets;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public final class XStreamHelper implements IXmlSerializer {
	private static final XStream xStream = new XStream(new DomDriver(Charsets.UTF_8.name()));

	public String toXML(final Object o) {
		xStream.processAnnotations(o.getClass());
		return xStream.toXML(o);
	}

	public <T> T fromXML(final String xml, final Class<T> clazz) {
		xStream.processAnnotations(clazz);
		return castObject(clazz, xStream.fromXML(xml));
	}

	private <T> T castObject(final Class<T> clazz, final Object o) {
		if (clazz.isInstance(o)) {
			return clazz.cast(o);
		}
		throw new IllegalArgumentException("XML of invalid clazz object!");
	}
}
