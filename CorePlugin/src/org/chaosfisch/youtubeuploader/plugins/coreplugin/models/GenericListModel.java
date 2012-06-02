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
public class GenericListModel<E> extends AbstractListModel<E> implements MutableComboBoxModel<E>
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
		this.objects = new IdentityVector<E>(elements.size());

		for (final E element : elements) {
			this.addElement(element);
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
		this.objects = new IdentityVector<E>(items.length);
		this.objects.ensureCapacity(items.length);

		int i;
		final int c;
		for (i = 0, c = items.length; i < c; i++) {
			this.objects.addElement(items[i]);
		}

		if (this.getSize() > 0) {
			this.selectedObject = this.getElementAt(0);
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
		this.objects = v;

		if (this.getSize() > 0) {
			this.selectedObject = this.getElementAt(0);
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
		if (((this.selectedObject != null) && !this.selectedObject.equals(anObject)) || ((this.selectedObject == null) && (anObject != null))) {
			this.selectedObject = anObject;
			this.fireContentsChanged(this, -1, -1);
		}
	}

	// implements javax.swing.ComboBoxModel
	@Override
	public Object getSelectedItem()
	{
		return this.selectedObject;
	}

	// implements javax.swing.ListModel
	@Override
	public int getSize()
	{
		return this.objects.size();
	}

	// implements javax.swing.ListModel
	@Override
	public E getElementAt(final int index)
	{
		if ((index >= 0) && (index < this.objects.size())) {
			return this.objects.elementAt(index);
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
		return this.objects.indexOf(anObject);
	}

	// implements javax.swing.MutableComboBoxModel
	@Override
	public void addElement(final E anObject)
	{
		this.objects.addElement(anObject);
		this.fireIntervalAdded(this, this.objects.size() - 1, this.objects.size() - 1);
		if ((this.objects.size() == 1) && (this.selectedObject == null) && (anObject != null)) {
			this.setSelectedItem(anObject);
		}
	}

	// implements javax.swing.MutableComboBoxModel
	@Override
	public void insertElementAt(final E anObject, final int index)
	{
		this.objects.insertElementAt(anObject, index);
		this.fireIntervalAdded(this, index, index);
	}

	// implements javax.swing.MutableComboBoxModel
	@Override
	public void removeElementAt(final int index)
	{
		if (this.getElementAt(index) == this.selectedObject) {
			if (index == 0) {
				this.setSelectedItem((this.getSize() == 1) ? null : this.getElementAt(index + 1));
			} else {
				this.setSelectedItem(this.getElementAt(index - 1));
			}
		}

		this.objects.removeElementAt(index);

		this.fireIntervalRemoved(this, index, index);
	}

	// implements javax.swing.MutableComboBoxModel
	@Override
	public void removeElement(final Object anObject)
	{
		final int index = this.getIndexOf(anObject);
		if (index != -1) {
			this.removeElementAt(index);
		}
	}

	/**
	 * Empties the list.
	 */
	public void removeAllElements()
	{
		if (!this.objects.isEmpty()) {
			final int lastIndex = this.objects.size() - 1;
			this.objects.removeAllElements();
			this.selectedObject = null;
			final int firstIndex = 0;
			this.fireIntervalRemoved(this, firstIndex, lastIndex);
		} else {
			this.selectedObject = null;
		}
	}

	public List<E> getAll()
	{
		return Collections.unmodifiableList(this.objects);
	}

	public boolean hasIndex(final int index)
	{
		return (this.objects.size() > index) && (index > -1);
	}
}
