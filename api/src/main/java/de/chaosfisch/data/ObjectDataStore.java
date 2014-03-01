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

import org.mapdb.DB;
import org.mapdb.HTreeMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

public class ObjectDataStore<T extends UniqueObject<E>, E> implements IDataStore<T, E> {

	private final HTreeMap<String, E> hashMap;
	private final DB                  db;
	private final Class<T>            clazz;

	public ObjectDataStore(final DB db, final HTreeMap<String, E> hashMap, final Class<T> clazz) {
		this.db = db;
		this.hashMap = hashMap;
		this.clazz = clazz;
	}

	@Override
	public void store(final T o) {
		hashMap.put(o.uniqueId(), o.toDTO());
		db.commit();
	}

	@Override
	public Collection<T> loadAll() {
		final Collection<E> values = hashMap.values();
		return transformDTOs(values);
	}

	@Override
	public Collection<T> load(final Predicate<? super E> predicate) {
		final Collection<E> values = hashMap.values();
		values.removeIf(predicate.negate());
		return transformDTOs(values);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T loadOne(final Predicate<? super E> predicate) {
		final Collection<E> values = hashMap.values();
		values.removeIf(predicate.negate());
		final Collection<T> transformDTOs = transformDTOs(values);
		return transformDTOs.isEmpty() ? null : (T) transformDTOs.toArray()[0];
	}

	private Collection<T> transformDTOs(final Collection<E> values) {
		final Collection<T> result = new ArrayList<>(values.size());
		values.forEach(t -> result.add(transformDTO(t)));
		return result;
	}

	private T transformDTO(final E t) {
		try {
			final T object = clazz.newInstance();
			object.fromDTO(t);
			return object;
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}


}
