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

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public class MultiMapProperty<K, V> {

	private final SimpleListProperty<K>                       keys = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final SimpleMapProperty<K, SimpleListProperty<V>> data = new SimpleMapProperty<>(FXCollections.observableHashMap());


	public ObservableList<K> keys() {
		return keys;
	}

	public ObservableList<V> values(final V object) {
		return data.get(object);
	}

	public int size() {
		return data.size();
	}

	public boolean isEmpty() {
		return keys.isEmpty();
	}

	public boolean containsKey(final Object key) {
		return keys.contains(key);
	}

	public boolean containsValue(final Object value) {
		return data.containsValue(value);
	}

	public SimpleListProperty<V> get(final Object key) {
		return data.get(key);
	}

	public ObservableList<V> put(final K key, final V value) {
		keys.add(key);

		final SimpleListProperty<V> values;
		if (data.containsKey(key)) {
			values = data.get(key);
		} else {
			values = new SimpleListProperty<>(FXCollections.observableArrayList());
		}

		if (null != value) {
			values.add(value);
		}
		return data.get(key);
	}

	public ObservableList<V> remove(final K key) {
		keys.remove(key);
		if (data.containsKey(key)) {
			data.get(key)
				.clear();
		}
		return data.remove(key);
	}

	public void remove(final K key, final V value) {
		if (data.containsKey(key)) {
			data.get(key)
				.remove(value);
		}
	}

	public void clear() {
		keys.clear();
		data.clear();
	}

	public ObservableMap<K, SimpleListProperty<V>> getData() {
		return data.get();
	}

	public void setData(final ObservableMap<K, SimpleListProperty<V>> data) {
		this.data.set(data);
	}

	public SimpleMapProperty<K, SimpleListProperty<V>> dataProperty() {
		return data;
	}
}
