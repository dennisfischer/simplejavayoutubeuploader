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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 30.05.12
 * Time: 09:09
 * To change this template use File | Settings | File Templates.
 */
public class TextDocument extends PlainDocument
{
	private static final long serialVersionUID = -1072024932284744923L;
	private final int maxLength;

	public TextDocument(final int maxLength)
	{
		this.maxLength = maxLength;
	}

	@Override
	public void insertString(final int offs, final String str, final AttributeSet a) throws BadLocationException
	{
		if (str.isEmpty()) {
			return;
		}
		if ((this.getLength() + str.length()) <= this.maxLength) {
			super.insertString(offs, str, a);
		}
	}
}