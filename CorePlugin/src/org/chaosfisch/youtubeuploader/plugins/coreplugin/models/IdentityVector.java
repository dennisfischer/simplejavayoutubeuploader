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

import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 22.05.12
 * Time: 18:59
 * To change this template use File | Settings | File Templates.
 */
public class IdentityVector<E> extends Vector<E>
{
	private static final long serialVersionUID = -5332035741200112233L;

	public IdentityVector(final int initialCapacity, final int capacityIncrement)
	{
		super(initialCapacity, capacityIncrement);    //To change body of overridden methods use File | Settings | File Templates.
	}

	public IdentityVector(final int initialCapacity)
	{
		super(initialCapacity);    //To change body of overridden methods use File | Settings | File Templates.
	}

	public IdentityVector()
	{
	}

	@Override public synchronized int lastIndexOf(final Object o, final int index)
	{
		if (index >= elementCount) {
			throw new IndexOutOfBoundsException(String.format("%d >= %d", index, elementCount)); //NON-NLS
		}

		if (o == null) {
			for (int i = index; i >= 0; i--) {
				if (elementData[i] == null) {
					return i;
				}
			}
		} else {
			for (int i = index; i >= 0; i--) {
				if (IModel.class.isInstance(o) && IModel.class.isInstance(elementData[i])) {
					final IModel model1 = (IModel) o;
					final IModel model2 = (IModel) elementData[i];
					if (model1.getIdentity().equals(model2.getIdentity())) {
						return i;
					}
				}
			}
		}
		return -1;
	}

	@Override public synchronized int indexOf(final Object o, final int index)
	{
		if (o == null) {
			for (int i = index; i < elementCount; i++) {
				if (elementData[i] == null) {
					return i;
				}
			}
		} else {
			for (int i = index; i < elementCount; i++) {
				if (IModel.class.isInstance(o) && IModel.class.isInstance(elementData[i])) {
					final IModel model1 = (IModel) o;
					final IModel model2 = (IModel) elementData[i];
					if (model1.getIdentity().equals(model2.getIdentity())) {
						return i;
					}
				}
			}
		}
		return -1;
	}
}
