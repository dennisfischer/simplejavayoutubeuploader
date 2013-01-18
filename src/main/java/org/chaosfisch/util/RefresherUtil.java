/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.util;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

public class RefresherUtil {
	public static <T> void refresh(final TableView<T> table, final ObservableList<T> tableList) {
		table.setItems(null);
		table.layout();
		table.setItems(tableList);
	}
}
