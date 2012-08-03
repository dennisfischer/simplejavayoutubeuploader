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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.models;

import org.bushe.swing.event.annotation.AnnotationProcessor;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 10.01.12
 * Time: 21:53
 * To change this template use File | Settings | File Templates.
 */
public class GenericListModel<E> extends AbstractListModel implements MutableComboBoxModel
{

	private static final long serialVersionUID = -8891330887837958077L;
	final IdentityVector<E> objects;
	Object selectedObject;

	public GenericListModel()
	{
		this(Collections.<E>emptyList());
	}

	public GenericListModel(final Collection<E> elements)
	{
		AnnotationProcessor.process(this);
		objects = new IdentityVector<E>(elements.size());

		for (final E element : elements) {
			addElement(element);
		}
	}

	/**
	 * Constructs a DefaultComboBoxModel object initialized with
	 * an array of objects.
	 *
	 * @param items an array of Object objects
	 */
	public GenericListModel(final E... items)
	{
		objects = new IdentityVector<E>(items.length);
		objects.ensureCapacity(items.length);

		int i;
		final int c;
		for (i = 0, c = items.length; i < c; i++) {
			objects.addElement(items[i]);
		}

		if (getSize() > 0) {
			selectedObject = getElementAt(0);
		}
	}

	/**
	 * Constructs a DefaultComboBoxModel object initialized with
	 * a vector.
	 *
	 * @param v a Vector object ...
	 */
	public GenericListModel(final IdentityVector<E> v)
	{
		objects = v;

		if (getSize() > 0) {
			selectedObject = getElementAt(0);
		}
	}

	// implements javax.swing.ComboBoxModel

	/**
	 * Set the value of the selected item. The selected item may be null.
	 * <p/>
	 *
	 * @param anObject The combo box value or null for no selection.
	 */
	@Override
	public void setSelectedItem(final Object anObject)
	{
		if (((selectedObject != null) && !selectedObject.equals(anObject)) || ((selectedObject == null) && (anObject != null))) {
			selectedObject = anObject;
			fireContentsChanged(this, -1, -1);
		}
	}

	// implements javax.swing.ComboBoxModel
	@Override
	public Object getSelectedItem()
	{
		return selectedObject;
	}

	// implements javax.swing.ListModel
	@Override
	public int getSize()
	{
		return objects.size();
	}

	// implements javax.swing.ListModel
	@Override
	public E getElementAt(final int index)
	{
		if ((index >= 0) && (index < objects.size())) {
			return objects.elementAt(index);
		} else {
			return null;
		}
	}

	/**
	 * Returns the index-position of the specified object in the list.
	 *
	 * @param anObject the object to check
	 * @return an int representing the index position, where 0 is
	 *         the first position
	 */
	public int getIndexOf(final Object anObject)
	{
		return objects.indexOf(anObject);
	}

	// implements javax.swing.MutableComboBoxModel
	@Override
	public void addElement(final Object anObject)
	{
		//noinspection unchecked
		objects.addElement((E) anObject);
		fireIntervalAdded(this, objects.size() - 1, objects.size() - 1);
		if ((objects.size() == 1) && (selectedObject == null) && (anObject != null)) {
			setSelectedItem(anObject);
		}
	}

	// implements javax.swing.MutableComboBoxModel
	public void insertElementAt(final Object anObject, final int index)
	{
		//noinspection unchecked
		objects.insertElementAt((E) anObject, index);
		fireIntervalAdded(this, index, index);
	}

	// implements javax.swing.MutableComboBoxModel
	@Override
	public void removeElementAt(final int index)
	{
		if (getElementAt(index) == selectedObject) {
			if (index == 0) {
				setSelectedItem((getSize() == 1) ? null : getElementAt(index + 1));
			} else {
				setSelectedItem(getElementAt(index - 1));
			}
		}

		objects.removeElementAt(index);

		fireIntervalRemoved(this, index, index);
	}

	// implements javax.swing.MutableComboBoxModel
	@Override
	public void removeElement(final Object anObject)
	{
		final int index = getIndexOf(anObject);
		if (index != -1) {
			removeElementAt(index);
		}
	}

	/**
	 * Empties the list.
	 */
	public void removeAllElements()
	{
		if (!objects.isEmpty()) {
			final int lastIndex = objects.size() - 1;
			objects.removeAllElements();
			selectedObject = null;
			final int firstIndex = 0;
			fireIntervalRemoved(this, firstIndex, lastIndex);
		} else {
			selectedObject = null;
		}
	}

	public List<E> getAll()
	{
		return Collections.unmodifiableList(objects);
	}

	public boolean hasIndex(final int index)
	{
		return (objects.size() > index) && (index > -1);
	}

	/**
	 * Replace a row of data at the {@code row} location in the model.
	 * Notification of the row being replaced will be generated.
	 *
	 * @param row     row in the model where the data will be replaced
	 * @param rowData data of the row to replace the existing data
	 * @throws IllegalArgumentException when the Class of the row data
	 *                                  does not match the row Class of the model.
	 */
	public void replaceRow(final int row, final E rowData)
	{
		objects.set(row, rowData);
		fireContentsChanged(this, row, row);
	}
}
