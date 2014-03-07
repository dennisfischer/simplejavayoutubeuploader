/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.category;

import com.google.api.services.youtube.YouTube;
import javafx.beans.property.SimpleListProperty;

import java.io.IOException;

public interface ICategoryService {

	/**
	 * Refreshes the internal category storage
	 *
	 * @param youTube the YouTube instance to use
	 * @throws IOException if request to YouTube fails.
	 */
	void refresh(YouTube youTube) throws IOException;

	/**
	 * Returns the list property for CategoryModel
	 *
	 * @return SimpleListProperty<CategoryModel> categoryModels
	 */
	SimpleListProperty<CategoryModel> categoryModelsProperty();
}
