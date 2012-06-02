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
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 22.04.12
 * Time: 13:59
 * To change this template use File | Settings | File Templates.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HyperlinkMouseAdapter extends MouseAdapter
{
	private final int column;

	public HyperlinkMouseAdapter(final int column)
	{
		this.column = column;
	}

	@Override public void mouseClicked(final MouseEvent e)
	{
		final JTable target = (JTable) e.getSource();
		final int selectedColumn = target.getSelectedColumn();
		if ((e.getClickCount() == 2) && (selectedColumn == this.column)) {
			final String label = (String) target.getValueAt(target.getSelectedRow(), selectedColumn);
			if ((this.extractUrl(label) != null) && Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().browse(new URI(this.extractUrl(label)));
				} catch (URISyntaxException e1) {
					e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				} catch (IOException e1) {
					e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				}
			}
		}
	}

	private String extractUrl(final String value)
	{
		final String urlPattern = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)"; //NON-NLS
		final Pattern p = Pattern.compile(urlPattern, Pattern.CASE_INSENSITIVE);
		final Matcher m = p.matcher(value);
		if (m.find()) {
			return value.substring(m.start(0), m.end(0));
		}
		return null;
	}
}