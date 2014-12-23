/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.data.category;

import org.sormula.Database;
import org.sormula.SormulaException;
import org.sormula.Table;

public class CategoryDAO extends Table<CategoryDTO> {

	public CategoryDAO(final Database database) throws SormulaException {
		super(database, CategoryDTO.class);
	}
}