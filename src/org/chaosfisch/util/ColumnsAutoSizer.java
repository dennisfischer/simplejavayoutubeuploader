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

package org.chaosfisch.util;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 07.01.12
 * Time: 20:25
 * To change this template use File | Settings | File Templates.
 */

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;

public class ColumnsAutoSizer
{

	public static void sizeColumnsToFit(final JTable table)
	{
		ColumnsAutoSizer.sizeColumnsToFit(table, 5);
	}

	@SuppressWarnings({})
	private static void sizeColumnsToFit(final JTable table, final int columnMargin)
	{
		final JTableHeader tableHeader = table.getTableHeader();

		if (tableHeader == null) {
			// can't auto size a table without a header
			return;
		}

		final FontMetrics headerFontMetrics = tableHeader.getFontMetrics(tableHeader.getFont());

		final int[] minWidths = new int[table.getColumnCount()];
		final int[] maxWidths = new int[table.getColumnCount()];

		for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
			final int headerWidth = headerFontMetrics.stringWidth(table.getColumnName(columnIndex));

			minWidths[columnIndex] = headerWidth + 20;

			final int maxWidth = ColumnsAutoSizer.getMaximalRequiredColumnWidth(table, columnIndex, headerWidth);

			maxWidths[columnIndex] = Math.max(maxWidth, minWidths[columnIndex]) + 20;
		}

		ColumnsAutoSizer.adjustMaximumWidths(table, minWidths, maxWidths);

		for (int i = 0; i < minWidths.length; i++) {
			if (minWidths[i] > 0) {
				table.getColumnModel().getColumn(i).setMinWidth(minWidths[i]);
			}

			if (maxWidths[i] > 0) {
				table.getColumnModel().getColumn(i).setMaxWidth(maxWidths[i]);

				table.getColumnModel().getColumn(i).setWidth(maxWidths[i]);
			}
		}

		ColumnsAutoSizer.stretchTable(table, maxWidths);
	}

	private static void stretchTable(final JTable table, final int... maxWidths)
	{
		if (ColumnsAutoSizer.sum(maxWidths) < table.getWidth()) {
			final int diff = table.getWidth() - ColumnsAutoSizer.sum(maxWidths);
			table.getColumnModel().getColumn(maxWidths.length - 1).setMinWidth(maxWidths[maxWidths.length - 1] + diff);
			table.getColumnModel().getColumn(maxWidths.length - 1).setMaxWidth(maxWidths[maxWidths.length - 1] + diff);
			table.getColumnModel().getColumn(maxWidths.length - 1).setWidth(maxWidths[maxWidths.length - 1] + diff);
		}
	}

	private static void adjustMaximumWidths(final JTable table, final int[] minWidths, final int... maxWidths)
	{
		if (table.getWidth() > 0) {
			// to prevent infinite loops in exceptional situations
			int breaker = 0;

			// keep stealing one pixel of the maximum width of the highest column until we can fit in the width of the table
			while ((ColumnsAutoSizer.sum(maxWidths) > table.getWidth()) && (breaker < 10000)) {
				final int highestWidthIndex = ColumnsAutoSizer.findLargestIndex(maxWidths);

				maxWidths[highestWidthIndex] -= 1;

				maxWidths[highestWidthIndex] = Math.max(maxWidths[highestWidthIndex], minWidths[highestWidthIndex]);

				breaker++;
			}
		}
	}

	private static int getMaximalRequiredColumnWidth(final JTable table, final int columnIndex, final int headerWidth)
	{
		int maxWidth = headerWidth;

		final TableColumn column = table.getColumnModel().getColumn(columnIndex);

		TableCellRenderer cellRenderer = column.getCellRenderer();

		if (cellRenderer == null) {
			cellRenderer = new DefaultTableCellRenderer();
		}

		for (int row = 0; row < table.getModel().getRowCount(); row++) {
			final Component rendererComponent = cellRenderer.getTableCellRendererComponent(table, table.getModel().getValueAt(row, columnIndex), false, false, row, columnIndex);

			final double valueWidth = rendererComponent.getPreferredSize().getWidth();

			maxWidth = (int) Math.max(maxWidth, valueWidth);
		}

		return maxWidth;
	}

	private static int findLargestIndex(final int... widths)
	{
		int largestIndex = 0;
		int largestValue = 0;

		for (int i = 0; i < widths.length; i++) {
			if (widths[i] > largestValue) {
				largestIndex = i;
				largestValue = widths[i];
			}
		}

		return largestIndex;
	}

	private static int sum(final int... widths)
	{
		int sum = 0;

		for (final int width : widths) {
			sum += width;
		}

		return sum;
	}
}