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

package org.chaosfisch.util.table;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ListTableModel extends RowTableModel<List<Object>>
{
	private static final long serialVersionUID = -5104035525413191687L;

	/**
	 * Initialize the List with null values. This will set the size of the
	 * List and will prevent an IndexOutOfBoundsException when trying to
	 * access an Element in the List.
	 *
	 * @param size the number of Elements to add to the List
	 */
	protected static List<String> newList(final int size)
	{
		final List<String> list = new ArrayList<String>(size);

		for (int i = 0; i < size; i++) {
			list.add(null);
		}

		return list;
	}

	/**
	 * Constructs an empty {@code ListTableModel} with default
	 * column names for the specified number of {@code columns}.
	 *
	 * @param columns the number of columns the table holds
	 */
	public ListTableModel(final int columns)
	{
		super(ListTableModel.newList(columns));
		setRowClass(List.class);
	}

	/**
	 * Constructs an empty {@code ListTableModel} with customized
	 * column names. The number of columns is determined bye the
	 * number of items in the {@code columnNames} List.
	 *
	 * @param columnNames {@code List} containing the names
	 *                    of the new columns
	 */
	public ListTableModel(final List<String> columnNames)
	{
		super(columnNames);
		setRowClass(List.class);
	}

	/**
	 * Constructs a {@code ListTableModel} with the specified number
	 * of rows. Default column names will be used for the specified number
	 * of {@code columns}.
	 *
	 * @param rows    the number of initially empty rows to create
	 * @param columns the number of columns the table holds
	 */
	public ListTableModel(final int rows, final int columns)
	{
		super(ListTableModel.newList(columns));
		setRowClass(List.class);

		final List<List<Object>> data = new ArrayList<List<Object>>(rows);

		for (int i = 0; i < rows; i++) {
			data.add(new ArrayList<Object>(columns));
		}

		insertRows(0, data);
	}

	/**
	 * Constructs a {@code ListTableModel} with initial data and
	 * customized column names.
	 * <p/>
	 * Each item in the {@code modelData} List must also be a List Object
	 * containing items for each column of the row.
	 * <p/>
	 * Each column's name will be taken from the {@code columnNames}
	 * List and the number of colums is determined by thenumber of items
	 * in the {@code columnNames} List.
	 *
	 * @param modelData   the data of the table
	 * @param columnNames {@code List} containing the names
	 *                    of the new columns
	 */
	public ListTableModel(final List<List<Object>> modelData, final List<String> columnNames)
	{
		super(modelData, columnNames);
		setRowClass(List.class);
	}

	/*
		 *  Convert an unformatted column name to a formatted column name. That is:
		 *
		 *  - insert a space when a new uppercase character is found, insert
		 *    multiple upper case characters are grouped together.
		 *  - replace any "_" with a space
		 *
		 *  @param columnName  unformatted column name
		 *  @return the formatted column name
		 */
	public static String formatColumnName(final String columnName)
	{
		if (columnName.length() < 3) {
			return columnName;
		}

		final StringBuilder buffer = new StringBuilder(columnName);
		boolean isPreviousLowerCase = false;

		for (int i = 1; i < buffer.length(); i++) {
			final boolean isCurrentUpperCase = Character.isUpperCase(buffer.charAt(i));

			if (isCurrentUpperCase && isPreviousLowerCase) {
				buffer.insert(i, " ");
				i++;
			}

			isPreviousLowerCase = !isCurrentUpperCase;
		}

		return buffer.toString().replaceAll("_", " ");
	}
//
//  Implement the TableModel interface
//

	/**
	 * Returns an attribute value for the cell at {@code row}
	 * and {@code column}.
	 *
	 * @param row    the row whose value is to be queried
	 * @param column the column whose value is to be queried
	 * @return the value Object at the specified cell
	 * @throws IndexOutOfBoundsException if an invalid row or column was given
	 */
	public Object getValueAt(final int row, final int column)
	{
		final List<Object> rowData = getRow(row);
		return rowData.get(column);
	}

	/**
	 * Sets the object value for the cell at {@code column} and
	 * {@code row}.  {@code value} is the new value.  This method
	 * will generate a {@code tableChanged} notification.
	 *
	 * @param value  the new value; this can be null
	 * @param row    the row whose value is to be changed
	 * @param column the column whose value is to be changed
	 * @throws IndexOutOfBoundsException if an invalid row or
	 *                                   column was given
	 */
	@Override
	public void setValueAt(final Object value, final int row, final int column)
	{
		final List<Object> rowData = getRow(row);
		rowData.set(column, value);
		fireTableCellUpdated(row, column);
	}

	/**
	 * Insert a row of data at the {@code row} location in the model.
	 * Notification of the row being added will be generated.
	 *
	 * @param row     row in the model where the data will be inserted
	 * @param rowData data of the row being added
	 */
	@Override
	public void insertRow(final int row, final List<Object> rowData)
	{
		justifyRow(rowData);
		super.insertRow(row, rowData);
	}

	/**
	 * Insert multiple rows of data at the {@code row} location in the model.
	 * Notification of the row being added will be generated.
	 *
	 * @param row     row in the model where the data will be inserted
	 * @param rowList each item in the list is a separate row of data
	 */
	@Override
	public void insertRows(final int row, final List<List<Object>> rowList)
	{
		for (final List<Object> rowData : rowList) {
			justifyRow(rowData);
		}

		super.insertRows(row, rowList);
	}

	/*
	 *  Make sure each List row contains the required number of columns.
	 */
	private void justifyRow(final Collection<Object> rowData)
	{
		for (int i = rowData.size(); i < getColumnCount(); i++) {
			rowData.add(null);
		}
	}

	/**
	 * Adds a row of data to the end of the model.
	 * Notification of the row being added will be generated.
	 *
	 * @param rowData data of the row being added
	 */
	public void addRow(final Object... rowData)
	{
		insertRow(getRowCount(), rowData);
	}

	/**
	 * Insert a row of data at the {@code row} location in the model.
	 * Notification of the row being added will be generated.
	 *
	 * @param row     row in the model where the data will be inserted
	 * @param rowData data of the row being added
	 */
	public void insertRow(final int row, final Object... rowData)
	{
		insertRow(row, copyToList(rowData));
	}

	/**
	 * Insert multiple rows of data at the {@code row} location in the model.
	 * Notification of the row being added will be generated.
	 *
	 * @param row      row in the model where the data will be inserted
	 * @param rowArray each item in the Array is a separate row of data
	 */
	public void insertRows(final int row, final Object[][] rowArray)
	{
		final List<List<Object>> data = new ArrayList<List<Object>>(rowArray.length);

		for (final Object[] aRowArray : rowArray) {
			data.add(copyToList(aRowArray));
		}

		insertRows(row, data);
	}

	/*
	 *  Copy the information in the Array to a List so a List can be added
	 *  to the model
	 */
	private List<Object> copyToList(final Object... rowData)
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
	 * @return a newly created ListTableModel
	 * @throws SQLException when an SQL error is encountered
	 */
	public static ListTableModel createModelFromResultSet(final ResultSet resultSet) throws SQLException
	{
		final ResultSetMetaData metaData = resultSet.getMetaData();
		final int columns = metaData.getColumnCount();

		//  Create empty model using the column names

		final ArrayList<String> columnNames = new ArrayList<String>(columns);

		for (int i = 1; i <= columns; i++) {
			final String columnName = metaData.getColumnName(i);
			final String columnLabel = metaData.getColumnLabel(i);

			//noinspection CallToStringEquals
			if (columnLabel.equals(columnName)) {
				columnNames.add(ListTableModel.formatColumnName(columnName));
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
			} catch (ClassNotFoundException ignored) {
				throw new RuntimeException("This shouldn't happen");
			} catch (SQLException ignored) {
				throw new RuntimeException("This shouldn't happen");
			}
		}

		//  Get row data

		final List<List<Object>> data = new ArrayList<List<Object>>(resultSet.getFetchSize());

		while (resultSet.next()) {
			final List<Object> row = new ArrayList<Object>(columns);

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
