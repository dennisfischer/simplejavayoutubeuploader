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

import javax.swing.table.AbstractTableModel;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A TableModel that better supports the processing of rows of data. That
 * is, the data is treated more like a row than an individual cell. Hopefully
 * this class can be used as a parent class instead of extending the
 * AbstractTableModel when you need custom models that contain row related
 * data.
 * <p/>
 * A few methods have also been added to make it easier to customize
 * properties of the model, such as the column class and column editability.
 * <p/>
 * Any class that extends this class must make sure to invoke the
 * setRowClass() and setDataAndColumnNames() methods either directly,
 * by using the various constructors, or indirectly.
 */
public abstract class RowTableModel<T> extends AbstractTableModel
{
	protected List<T>      modelData;
	protected List<String> columnNames;
	protected Class[]      columnClasses;
	protected Boolean[]    isColumnEditable;
	private Class   rowClass        = Object.class;
	private boolean isModelEditable = true;

	/**
	 * Constructs a <code>RowTableModel</code> with the row class.
	 * <p/>
	 * This value is used by the getRowsAsArray() method.
	 * <p/>
	 * Sub classes creating a model using this constructor must make sure
	 * to invoke the setDataAndColumnNames() method.
	 *
	 * @param rowClass the class of row data to be added to the model
	 */
	protected RowTableModel(final Class rowClass)
	{
		this.setRowClass(rowClass);
	}

	/**
	 * Constructs a <code>RowTableModel</code> with column names.
	 * <p/>
	 * Each column's name will be taken from the <code>columnNames</code>
	 * List and the number of colums is determined by thenumber of items
	 * in the <code>columnNames</code> List.
	 * <p/>
	 * Sub classes creating a model using this constructor must make sure
	 * to invoke the setRowClass() method.
	 *
	 * @param columnNames <code>List</code> containing the names
	 *                    of the new columns
	 */
	protected RowTableModel(final List<String> columnNames)
	{
		this(new ArrayList<T>(0), columnNames);
	}

	/**
	 * Constructs a <code>RowTableModel</code> with initial data and
	 * customized column names.
	 * <p/>
	 * Each item in the <code>modelData</code> List must also be a List Object
	 * containing items for each column of the row.
	 * <p/>
	 * Each column's name will be taken from the <code>columnNames</code>
	 * List and the number of colums is determined by thenumber of items
	 * in the <code>columnNames</code> List.
	 * <p/>
	 * Sub classes creating a model using this constructor must make sure
	 * to invoke the setRowClass() method.
	 *
	 * @param modelData   the data of the table
	 * @param columnNames <code>List</code> containing the names
	 *                    of the new columns
	 */
	protected RowTableModel(final List<T> modelData, final List<String> columnNames)
	{
		this.setDataAndColumnNames(modelData, columnNames);
	}

	/**
	 * Full Constructor for creating a <code>RowTableModel</code>.
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
	 * @param rowClass    the class of row data to be added to the model
	 */
	protected RowTableModel(final List<T> modelData, final List<String> columnNames, final Class rowClass)
	{
		this.setDataAndColumnNames(modelData, columnNames);
		this.setRowClass(rowClass);
	}

	/**
	 * Reset the data and column names of the model.
	 * <p/>
	 * A fireTableStructureChanged event will be generated.
	 *
	 * @param modelData   the data of the table
	 * @param columnNames <code>List</code> containing the names
	 *                    of the new columns
	 */
	protected void setDataAndColumnNames(final List<T> modelData, final List<String> columnNames)
	{
		this.modelData = modelData;
		this.columnNames = columnNames;
		this.columnClasses = new Class[this.getColumnCount()];
		this.isColumnEditable = new Boolean[this.getColumnCount()];
		this.fireTableStructureChanged();
	}

	/**
	 * The class of the Row being stored in the TableModel
	 * <p/>
	 * This is required for the getRowsAsArray() method to return the
	 * proper class of row.
	 *
	 * @param rowClass the class of the row
	 */
	protected void setRowClass(final Class rowClass)
	{
		this.rowClass = rowClass;
	}
//
//  Implement the TableModel interface
//

	/**
	 * Returns the Class of the queried <code>column</code>.
	 * <p/>
	 * First it will check to see if a Class has been specified for the
	 * <code>column</code> by using the <code>setColumnClass</code> method.
	 * If not, then the superclass value is returned.
	 *
	 * @param column the column being queried
	 * @return the Class of the column being queried
	 */
	public Class getColumnClass(final int column)
	{
		Class columnClass = null;

		//  Get the class, if set, for the specified column

		if (column < this.columnClasses.length) {
			columnClass = this.columnClasses[column];
		}

		//  Get the default class

		if (columnClass == null) {
			columnClass = super.getColumnClass(column);
		}

		return columnClass;
	}

	/**
	 * Returns the number of columns in this table model.
	 *
	 * @return the number of columns in the model
	 */
	public int getColumnCount()
	{
		return this.columnNames.size();
	}

	/**
	 * Returns the column name.
	 *
	 * @return a name for this column using the string value of the
	 *         appropriate member in <code>columnNames</code>. If
	 *         <code>columnNames</code> does not have an entry for this index
	 *         then the default name provided by the superclass is returned
	 */
	public String getColumnName(final int column)
	{
		Object columnName = null;

		if (column < this.columnNames.size()) {
			columnName = this.columnNames.get(column);
		}

		return (columnName == null) ? super.getColumnName(column) : columnName.toString();
	}

	/**
	 * Returns the number of rows in this table model.
	 *
	 * @return the number of rows in the model
	 */
	public int getRowCount()
	{
		return this.modelData.size();
	}

	/**
	 * Returns true regardless of parameter values.
	 *
	 * @param row    the row whose value is to be queried
	 * @param column the column whose value is to be queried
	 * @return true
	 */
	public boolean isCellEditable(final int row, final int column)
	{
		Boolean isEditable = null;

		//  Check is column editability has been set

		if (column < this.isColumnEditable.length) {
			isEditable = this.isColumnEditable[column];
		}

		return (isEditable == null) ? this.isModelEditable : isEditable;
	}
//
//  Implement custom methods
//

	/**
	 * Adds a row of data to the end of the model.
	 * Notification of the row being added will be generated.
	 *
	 * @param rowData data of the row being added
	 */
	public void addRow(final T rowData)
	{
		this.insertRow(this.getRowCount(), rowData);
	}

	/**
	 * Returns the Object of the requested <code>row</code>.
	 *
	 * @return the Object of the requested row.
	 */
	public T getRow(final int row)
	{
		return this.modelData.get(row);
	}

	/**
	 * Returns an array of Objects for the requested <code>rows</code>.
	 *
	 * @return an array of Objects for the requested rows.
	 */
	@SuppressWarnings("unchecked")
	public T[] getRowsAsArray(final int... rows)
	{
		final List<T> rowData = this.getRowsAsList(rows);
		final T[] array = (T[]) Array.newInstance(this.rowClass, rowData.size());
		return rowData.toArray(array);
	}

	/**
	 * Returns a List of Objects for the requested <code>rows</code>.
	 *
	 * @return a List of Objects for the requested rows.
	 */
	public List<T> getRowsAsList(final int... rows)
	{
		final ArrayList<T> rowData = new ArrayList<T>(rows.length);

		for (final int row : rows) {
			rowData.add(this.getRow(row));
		}

		return rowData;
	}

	/**
	 * Insert a row of data at the <code>row</code> location in the model.
	 * Notification of the row being added will be generated.
	 *
	 * @param row     row in the model where the data will be inserted
	 * @param rowData data of the row being added
	 */
	public void insertRow(final int row, final T rowData)
	{
		this.modelData.add(row, rowData);
		this.fireTableRowsInserted(row, row);
	}

	/**
	 * Insert multiple rows of data at the <code>row</code> location in the model.
	 * Notification of the row being added will be generated.
	 *
	 * @param row     row in the model where the data will be inserted
	 * @param rowList each item in the list is a separate row of data
	 */
	public void insertRows(final int row, final List<T> rowList)
	{
		this.modelData.addAll(row, rowList);
		this.fireTableRowsInserted(row, row + rowList.size() - 1);
	}

	/**
	 * Insert multiple rows of data at the <code>row</code> location in the model.
	 * Notification of the row being added will be generated.
	 *
	 * @param row      row in the model where the data will be inserted
	 * @param rowArray each item in the Array is a separate row of data
	 */
	public void insertRows(final int row, final T[] rowArray)
	{
		final List<T> rowList = new ArrayList<T>(rowArray.length);

		Collections.addAll(rowList, rowArray);

		this.insertRows(row, rowList);
	}

	/**
	 * Moves one or more rows from the inlcusive range <code>start</code> to
	 * <code>end</code> to the <code>to</code> position in the model.
	 * After the move, the row that was at index <code>start</code>
	 * will be at index <code>to</code>.
	 * This method will send a <code>tableRowsUpdated</code> notification
	 * message to all the listeners. <p>
	 * <p/>
	 * <pre>
	 *  Examples of moves:
	 *
	 *  1. moveRow(1,3,5);
	 * 		  a|B|C|D|e|f|g|h|i|j|k   - before
	 * 		  a|e|f|g|h|B|C|D|i|j|k   - after
	 *
	 *  2. moveRow(6,7,1);
	 * 		  a|b|c|d|e|f|G|H|i|j|k   - before
	 * 		  a|G|H|b|c|d|e|f|i|j|k   - after
	 *
	 *  </pre>
	 *
	 * @param start the starting row index to be moved
	 * @param end   the ending row index to be moved
	 * @param to    the destination of the rows to be moved
	 * @throws IllegalArgumentException if any of the elements would be moved out
	 *                                  of the table's range
	 */
	public void moveRow(final int start, final int end, final int to)
	{
		if (start < 0) {
			final String message = "Start index must be positive: " + start; //NON-NLS
			throw new IllegalArgumentException(message);
		}

		if (end > this.getRowCount() - 1) {
			final String message = "End index must be less than total rows: " + end; //NON-NLS
			throw new IllegalArgumentException(message);
		}

		if (start > end) {
			final String message = "Start index cannot be greater than end index"; //NON-NLS
			throw new IllegalArgumentException(message);
		}

		final int rowsMoved = end - start + 1;

		if (to < 0 || to > this.getRowCount() - rowsMoved) {
			final String message = "New destination row (" + to + ") is invalid"; //NON-NLS NON-NLS
			throw new IllegalArgumentException(message);
		}

		//  Save references to the rows that are about to be moved

		final ArrayList<T> temp = new ArrayList<T>(rowsMoved);

		for (int i = start; i < end + 1; i++) {
			temp.add(this.modelData.get(i));
		}

		//  Remove the rows from the current location and add them back
		//  at the specified new location

		this.modelData.subList(start, end + 1).clear();
		this.modelData.addAll(to, temp);

		//  Determine the rows that need to be repainted to reflect the move

		final int first;
		final int last;

		if (to < start) {
			first = to;
			last = end;
		} else {
			first = start;
			last = to + end - start;
		}

		this.fireTableRowsUpdated(first, last);
	}

	/**
	 * Remove the specified rows from the model. Rows between the starting
	 * and ending indexes, inclusively, will be removed.
	 * Notification of the rows being removed will be generated.
	 *
	 * @param start starting row index
	 * @param end   ending row index
	 * @throws ArrayIndexOutOfBoundsException if any row index is invalid
	 */
	public void removeRowRange(final int start, final int end)
	{
		this.modelData.subList(start, end + 1).clear();
		this.fireTableRowsDeleted(start, end);
	}

	/**
	 * Remove the specified rows from the model. The row indexes in the
	 * array must be in increasing order.
	 * Notification of the rows being removed will be generated.
	 *
	 * @param rows array containing indexes of rows to be removed
	 * @throws ArrayIndexOutOfBoundsException if any row index is invalid
	 */
	public void removeRows(final int... rows)
	{
		for (int i = rows.length - 1; i >= 0; i--) {
			final int row = rows[i];
			this.modelData.remove(row);
			this.fireTableRowsDeleted(row, row);
		}
	}

	/**
	 * Replace a row of data at the <code>row</code> location in the model.
	 * Notification of the row being replaced will be generated.
	 *
	 * @param row     row in the model where the data will be replaced
	 * @param rowData data of the row to replace the existing data
	 * @throws IllegalArgumentException when the Class of the row data
	 *                                  does not match the row Class of the model.
	 */
	public void replaceRow(final int row, final T rowData)
	{
		this.modelData.set(row, rowData);
		this.fireTableRowsUpdated(row, row);
	}

	/**
	 * Sets the Class for the specified column.
	 *
	 * @param column      the column whose Class is being changed
	 * @param columnClass the new Class of the column
	 * @throws ArrayIndexOutOfBoundsException if an invalid column was given
	 */
	public void setColumnClass(final int column, final Class columnClass)
	{
		this.columnClasses[column] = columnClass;
		this.fireTableRowsUpdated(0, this.getColumnCount() - 1);
	}

	/**
	 * Sets the editability for the specified column.
	 *
	 * @param column     the column whose Class is being changed
	 * @param isEditable indicates if the column is editable or not
	 * @throws ArrayIndexOutOfBoundsException if an invalid column was given
	 */
	public void setColumnEditable(final int column, final boolean isEditable)
	{
		this.isColumnEditable[column] = isEditable ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * Set the ability to edit cell data for the entire model
	 * <p/>
	 * Note: values set by the setColumnEditable(...) method will have
	 * prioritiy over this value.
	 *
	 * @param isModelEditable true/false
	 */
	public void setModelEditable(final boolean isModelEditable)
	{
		this.isModelEditable = isModelEditable;
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
}
