package org.chaosfisch.util;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

public class RefresherUtil
{
	public static <T> void refresh(final TableView<T> table, final ObservableList<T> tableList)
	{
		table.setItems(null);
		table.layout();
		table.setItems(tableList);
	}
}
