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

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;

import org.javalite.activejdbc.Model;

public class ActiveCellValueFactory<T extends Model, V> implements Callback<TableColumn.CellDataFeatures<T, V>, ObservableValue<V>> {
	private final String					cellName;
	private final Class<? extends Model>	parent;

	public ActiveCellValueFactory(final String cellName) {
		this.cellName = cellName;
		parent = null;
	}

	public ActiveCellValueFactory(final String cellName, final Class<? extends Model> parent) {
		this.cellName = cellName;
		this.parent = parent;
	}

	@Override
	public ObservableValue<V> call(final CellDataFeatures<T, V> arg0) {
		if (arg0.getValue() != null) {
			if (parent != null) {
				if (arg0.getValue().parent(parent) != null) {
					return getReturnValue(arg0.getValue().parent(parent));
				} else {
					return null;
				}
			}
			return getReturnValue(arg0.getValue());
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private ObservableValue<V> getReturnValue(final Model model) {
		if (cellName == "this") {
			return new ReadOnlyObjectWrapper<V>((V) model);
		}
		if (cellName == "id") {
			return new ReadOnlyObjectWrapper<V>((V) model.getLongId());
		}
		return new ReadOnlyObjectWrapper<V>((V) model.get(cellName));
	}
}
