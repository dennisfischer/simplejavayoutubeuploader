/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Dennis Fischer
 ******************************************************************************/
/*
 * Copyright (c) 2012, Dennis Fischer
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.chaosfisch.youtubeuploader.dao.spi;

import java.util.List;

public interface CRUDDao<E>
{
	/**
	 * Creates / persists a new object inside the persistence storage
	 * 
	 * @param object
	 *            the object to persist
	 * @return the persisted object
	 */
	E create(E object);

	/**
	 * Updates an existing object inside the persistence storage
	 * 
	 * @param object
	 *            the object to update
	 * @return the updated object
	 */
	E update(E object);

	/**
	 * Deletes an existing object from the persistence storage
	 * 
	 * @param object
	 *            the object to delete
	 */
	void delete(E object);

	/**
	 * Retrieves all stored elements
	 * 
	 * @return List<E> the retrieved elements
	 */
	List<E> getAll();

	/**
	 * Searches for a single object inside the persistence storage
	 * 
	 * @param object
	 *            - parameters to search with
	 * @return the found object
	 */
	E find(E object);
}
