package org.chaosfisch.util;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import jfxtras.labs.scene.control.grid.GridView;

import org.javalite.activejdbc.Model;

public class RefresherUtil
{
	public static <T> void refresh(final TableView<T> table, final ObservableList<T> tableList)
	{
		table.setItems(null);
		table.layout();
		table.setItems(tableList);
	}

	public static <T> void refresh(final GridView<Model> playlistSourcezone, final ObservableList<? extends Model> list)
	{
		playlistSourcezone.itemsProperty().set(null);
		playlistSourcezone.layout();
		playlistSourcezone.itemsProperty().set((ObservableList<Model>) list);
	}
}
