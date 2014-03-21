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

import java.util.List;

public interface IGenericDAO<T> {
	/**
	 * Returns all T's in the system
	 *
	 * @return List<T> accounts
	 */
	List<T> getAll();

	/**
	 * Stores (inserts/updates) a single record
	 *
	 * @param object to store
	 */
	void store(T object);

	/**
	 * Removed (deletes) a single record
	 *
	 * @param object to remove
	 */
	void remove(T object);
}
