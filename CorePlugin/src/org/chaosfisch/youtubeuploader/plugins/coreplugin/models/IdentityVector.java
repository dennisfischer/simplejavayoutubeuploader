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
		super();    //To change body of overridden methods use File | Settings | File Templates.
	}

	@Override public synchronized int lastIndexOf(final Object o, final int index)
	{
		if (index >= this.elementCount) {
			throw new IndexOutOfBoundsException(index + " >= " + this.elementCount);
		}

		if (o == null) {
			for (int i = index; i >= 0; i--)
				if (this.elementData[i] == null) {
					return i;
				}
		} else {
			for (int i = index; i >= 0; i--) {
				if (org.chaosfisch.youtubeuploader.plugins.coreplugin.models.IModel.class.isInstance(o) && org.chaosfisch.youtubeuploader.plugins.coreplugin.models.IModel.class.isInstance(
						this.elementData[i])) {
					final org.chaosfisch.youtubeuploader.plugins.coreplugin.models.IModel model1 = (org.chaosfisch.youtubeuploader.plugins.coreplugin.models.IModel) o;
					final org.chaosfisch.youtubeuploader.plugins.coreplugin.models.IModel model2 = (org.chaosfisch.youtubeuploader.plugins.coreplugin.models.IModel) this.elementData[i];
					if (model1.getIdentity() == model2.getIdentity()) {
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
			for (int i = index; i < this.elementCount; i++)
				if (this.elementData[i] == null) {
					return i;
				}
		} else {
			for (int i = index; i < this.elementCount; i++) {
				if (org.chaosfisch.youtubeuploader.plugins.coreplugin.models.IModel.class.isInstance(o) && org.chaosfisch.youtubeuploader.plugins.coreplugin.models.IModel.class.isInstance(
						this.elementData[i])) {
					final org.chaosfisch.youtubeuploader.plugins.coreplugin.models.IModel model1 = (org.chaosfisch.youtubeuploader.plugins.coreplugin.models.IModel) o;
					final org.chaosfisch.youtubeuploader.plugins.coreplugin.models.IModel model2 = (org.chaosfisch.youtubeuploader.plugins.coreplugin.models.IModel) this.elementData[i];
					if (model1.getIdentity() == model2.getIdentity()) {
						return i;
					}
				}
			}
		}
		return -1;
	}
}
