package org.chaosfisch.util;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

import com.guigarage.fx.grid.GridView;

public class RefresherUtil
{
	public static <T> void refresh(final TableView<T> table, final ObservableList<T> tableList)
	{
		table.setItems(null);
		table.layout();
		table.setItems(tableList);
	}

	public static <T> void refresh(final GridView<T> gridview, final ObservableList<T> tableList)
	{
		gridview.setItems(null);
		gridview.layout();
		gridview.setItems(tableList);
	}
}
