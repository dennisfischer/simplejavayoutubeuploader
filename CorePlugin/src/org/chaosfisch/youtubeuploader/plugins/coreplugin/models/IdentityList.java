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

import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.entities.IEntry;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 23.03.12
 * Time: 22:35
 * To change this template use File | Settings | File Templates.
 */
public class IdentityList<E extends IEntry> extends ArrayList<E>
{
	@Override
	public int indexOf(final Object o)
	{
		final IEntry entry = (IEntry) o;
		if (o == null) {
			for (int i = 0; i < this.size(); i++)
				if (this.get(i) == null) {
					return i;
				}
		} else {
			for (int i = 0; i < this.size(); i++)
				if (entry.getIdentity() == this.get(i).getIdentity()) {
					return i;
				}
		}
		return -1;
	}

	/**
	 * Returns the index of the last occurrence of the specified element
	 * in this list, or -1 if this list does not contain the element.
	 * More formally, returns the highest index <tt>i</tt> such that
	 * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
	 * or -1 if there is no such index.
	 */
	@Override
	public int lastIndexOf(final Object o)
	{
		final IEntry entry = (IEntry) o;
		if (entry == null) {
			for (int i = this.size() - 1; i >= 0; i--)
				if (this.get(i) == null) {
					return i;
				}
		} else {
			for (int i = this.size() - 1; i >= 0; i--)
				if (entry.getIdentity() == this.get(i).getIdentity()) {
					return i;
				}
		}
		return -1;
	}
}
