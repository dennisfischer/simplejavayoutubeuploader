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

import java.util.Collection;
import java.util.function.Predicate;

public interface IDataStore<T extends UniqueObject<E>, E> {

	void store(T o);

	Collection<T> loadAll();

	Collection<T> load(Predicate<? super E> predicate);

	T loadOne(Predicate<? super E> predicate);
}
