/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.data;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AbstractDAO<T> {
	protected final ResultSetHandler<List<T>> listResultSetHandler;
	protected final ResultSetHandler<T>       singleResultSetHandler;
	private final   Interner<T>               objectInterner;

	public AbstractDAO(final Class<T> clazz) {
		singleResultSetHandler = new BeanHandler<>(clazz);
		listResultSetHandler = new BeanListHandler<>(clazz);
		objectInterner = Interners.newWeakInterner();
	}

	protected List<T> intern(final List<T> objects) {
		if (null == objects) {
			return Collections.emptyList();
		}
		return objects.stream()
					  .map(objectInterner::intern)
					  .collect(Collectors.toList());
	}

	protected T intern(final T object) {
		return null == object ? null : objectInterner.intern(object);
	}
}
