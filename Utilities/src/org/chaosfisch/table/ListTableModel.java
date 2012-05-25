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

package org.chaosfisch.table;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListTableModel extends RowTableModel<List>
{
	/**
	 * Initialize the List with null values. This will set the size of the
	 * List and will prevent an IndexOutOfBoundsException when trying to
	 * access an Element in the List.
	 *
	 * @param size the number of Elements to add to the List
	 */
	protected static List<String> newList(final int size)
	{
		final ArrayList<String> list = new ArrayList<String>(size);

		for (int i = 0; i < size; i++) {
			list.add(null);
		}

		return list;
	}

	/**
	 * Constructs an empty <code>ListTableModel</code> with default
	 * column names for the specified number of <code>columns</code>.
	 *
	 * @param columns the number of columns the table holds
	 */
	public ListTableModel(final int columns)
	{
		super(newList(columns));
		this.setRowClass(List.class);
	}

	/**
	 * Constructs an empty <code>ListTableModel</code> with customized
	 * column names. The number of columns is determined bye the
	 * number of items in the <code>columnNames</code> List.
	 *
	 * @param columnNames <code>List</code> containing the names
	 *                    of the new columns
	 */
	public ListTableModel(final List<String> columnNames)
	{
		super(columnNames);
		this.setRowClass(List.class);
	}

	/**
	 * Constructs a <code>ListTableModel</code> with the specified number
	 * of rows. Default column names will be used for the specified number
	 * of <code>columns</code>.
	 *
	 * @param rows    the number of initially empty rows to create
	 * @param columns the number of columns the table holds
	 */
	public ListTableModel(final int rows, final int columns)
	{
		super(newList(columns));
		this.setRowClass(List.class);

		final List<List> data = new ArrayList<List>(rows);

		for (int i = 0; i < rows; i++)
			data.add(new ArrayList(columns));

		this.insertRows(0, data);
	}

	/**
	 * Constructs a <code>ListTableModel</code> with initial data and
	 * customized column names.
	 * <p/>
	 * Each item in the <code>modelData</code> List must also be a List Object
	 * containing items for each column of the row.
	 * <p/>
	 * Each column's name will be taken from the <code>columnNames</code>
	 * List and the number of colums is determined by thenumber of items
	 * in the <code>columnNames</code> List.
	 *
	 * @param modelData   the data of the table
	 * @param columnNames <code>List</code> containing the names
	 *                    of the new columns
	 */
	public ListTableModel(final List<List> modelData, final List<String> columnNames)
	{
		super(modelData, columnNames);
		this.setRowClass(List.class);
	}
//
//  Implement the TableModel interface
//

	/**
	 * Returns an attribute value for the cell at <code>row</code>
	 * and <code>column</code>.
	 *
	 * @param row    the row whose value is to be queried
	 * @param column the column whose value is to be queried
	 * @throws IndexOutOfBoundsException if an invalid row or column was given
	 * @return the value Object at the specified cell
	 */
	public Object getValueAt(final int row, final int column)
	{
		final List rowData = this.getRow(row);
		return rowData.get(column);
	}

	/**
	 * Sets the object value for the cell at <code>column</code> and
	 * <code>row</code>.  <code>value</code> is the new value.  This method
	 * will generate a <code>tableChanged</code> notification.
	 *
	 * @param value  the new value; this can be null
	 * @param row    the row whose value is to be changed
	 * @param column the column whose value is to be changed
	 * @throws IndexOutOfBoundsException if an invalid row or
	 *                                   column was given
	 */
	@SuppressWarnings("unchecked")
	public void setValueAt(final Object value, final int row, final int column)
	{
		final List rowData = this.getRow(row);
		rowData.set(column, value);
		this.fireTableCellUpdated(row, column);
	}

	/**
	 * Insert a row of data at the <code>row</code> location in the model.
	 * Notification of the row being added will be generated.
	 *
	 * @param row     row in the model where the data will be inserted
	 * @param rowData data of the row being added
	 */
	@Override
	public void insertRow(final int row, final List rowData)
	{
		this.justifyRow(rowData);
		super.insertRow(row, rowData);
	}

	/**
	 * Insert multiple rows of data at the <code>row</code> location in the model.
	 * Notification of the row being added will be generated.
	 *
	 * @param row     row in the model where the data will be inserted
	 * @param rowList each item in the list is a separate row of data
	 */
	@Override
	public void insertRows(final int row, final List<List> rowList)
	{
		for (final List rowData : rowList) {
			this.justifyRow(rowData);
		}

		super.insertRows(row, rowList);
	}

	/*
	 *  Make sure each List row contains the required number of columns.
	 */
	@SuppressWarnings("unchecked")
	private void justifyRow(final List rowData)
	{
		for (int i = rowData.size(); i < this.getColumnCount(); i++) {
			rowData.add(null);
		}
	}

	/**
	 * Adds a row of data to the end of the model.
	 * Notification of the row being added will be generated.
	 *
	 * @param rowData data of the row being added
	 */
	public void addRow(final Object[] rowData)
	{
		this.insertRow(this.getRowCount(), rowData);
	}

	/**
	 * Insert a row of data at the <code>row</code> location in the model.
	 * Notification of the row being added will be generated.
	 *
	 * @param row     row in the model where the data will be inserted
	 * @param rowData data of the row being added
	 */
	public void insertRow(final int row, final Object[] rowData)
	{
		this.insertRow(row, this.copyToList(rowData));
	}

	/**
	 * Insert multiple rows of data at the <code>row</code> location in the model.
	 * Notification of the row being added will be generated.
	 *
	 * @param row      row in the model where the data will be inserted
	 * @param rowArray each item in the Array is a separate row of data
	 */
	public void insertRows(final int row, final Object[][] rowArray)
	{
		final List<List> data = new ArrayList<List>(rowArray.length);

		for (Object[] aRowArray : rowArray) {
			data.add(this.copyToList(aRowArray));
		}

		this.insertRows(row, data);
	}

	/*
	 *  Copy the information in the Array to a List so a List can be added
	 *  to the model
	 */
	private List copyToList(final Object[] rowData)
	{
		final List<Object> row = new ArrayList<Object>(rowData.length);

		Collections.addAll(row, rowData);

		return row;
	}

	/**
	 * Create a ListTableModel given a specific ResultSet.
	 * <p/>
	 * The column names and class type will be retrieved from the
	 * ResultSetMetaData. The data is retrieved from each row found in the
	 * ResultSet. The class of
	 *
	 * @param resultSet ResultSet containing results of a database query
	 * @throws SQLException when an SQL error is encountered
	 * @return a newly created ListTableModel
	 */
	public static ListTableModel createModelFromResultSet(final ResultSet resultSet) throws SQLException
	{
		final ResultSetMetaData metaData = resultSet.getMetaData();
		final int columns = metaData.getColumnCount();

		//  Create empty model using the column names

		final ArrayList<String> columnNames = new ArrayList<String>();

		for (int i = 1; i <= columns; i++) {
			final String columnName = metaData.getColumnName(i);
			final String columnLabel = metaData.getColumnLabel(i);

			if (columnLabel.equals(columnName)) {
				columnNames.add(formatColumnName(columnName));
			} else {
				columnNames.add(columnLabel);
			}
		}

		final ListTableModel model = new ListTableModel(columnNames);
		model.setModelEditable(false);

		//  Assign the class of each column

		for (int i = 1; i <= columns; i++) {
			try {
				final String className = metaData.getColumnClassName(i);
				model.setColumnClass(i - 1, Class.forName(className));
			} catch (Exception ignored) {
			}
		}

		//  Get row data

		final ArrayList<List> data = new ArrayList<List>();

		while (resultSet.next()) {
			final ArrayList<Object> row = new ArrayList<Object>(columns);

			for (int i = 1; i <= columns; i++) {
				final Object o = resultSet.getObject(i);
				row.add(o);
			}

			data.add(row);
		}

		model.insertRows(0, data);

		return model;
	}
}
