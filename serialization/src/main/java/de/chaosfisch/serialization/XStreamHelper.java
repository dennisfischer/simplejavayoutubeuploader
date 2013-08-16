/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.serialization;

import com.google.common.base.Charsets;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

final class XStreamHelper implements IXmlSerializer {
	private static final XStream xStream = new XStream(new DomDriver(Charsets.UTF_8.name()));
	private static final Logger  logger  = LoggerFactory.getLogger(XStreamHelper.class);

	static {
		xStream.setMode(XStream.NO_REFERENCES);
	}

	@Override
	public String toXML(final Object o) {
		xStream.processAnnotations(o.getClass());
		return xStream.toXML(o);
	}

	@Override
	public <T> T fromXML(final String xml, final Class<T> clazz) {
		xStream.processAnnotations(clazz);
		return castObject(clazz, xStream.fromXML(xml));
	}

	@Override
	public <T> T fromXML(final File xml, final Class<T> clazz) {
		xStream.processAnnotations(clazz);
		return castObject(clazz, xStream.fromXML(xml));
	}

	@Override
	public void toXML(final Object o, final File xml) {
		xStream.processAnnotations(o.getClass());
		try (FileWriter writer = new FileWriter(xml)) {
			xStream.toXML(o, writer);
		} catch (IOException e) {
			logger.error("Couldn't write xml file", e);
		}
	}

	@Override
	public void addAlias(final Class<?> clazz, final String alias) {
		xStream.alias(alias, clazz);
	}

	private <T> T castObject(final Class<T> clazz, final Object o) {
		if (clazz.isInstance(o)) {
			return clazz.cast(o);
		}
		throw new IllegalArgumentException("XML of invalid clazz object!");
	}
}
