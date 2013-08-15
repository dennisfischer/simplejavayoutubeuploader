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

import java.io.File;

public interface IXmlSerializer {
	String toXML(Object o);

	<T> T fromXML(String xml, Class<T> clazz);

	<T> T fromXML(File xml, Class<T> clazz);

	void toXML(Object o, File xml);

	void addAlias(Class<?> clazz, String alias);
}
