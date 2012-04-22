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

package org.chaosfisch.youtubeuploader.plugins.socializeplugin;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 18.04.12
 * Time: 16:29
 * To change this template use File | Settings | File Templates.
 */
public class DisabledGlassPane extends JComponent implements KeyListener
{
	private final static Border MESSAGE_BORDER = new EmptyBorder(10, 10, 10, 10);
	private final        JLabel message        = new JLabel();

	public DisabledGlassPane()
	{
		//  Set glass pane properties

		this.setOpaque(false);
		final Color base = UIManager.getColor("inactiveCaptionBorder");
		final Color background = new Color(base.getRed(), base.getGreen(), base.getBlue(), 128);
		this.setBackground(background);
		this.setLayout(new GridBagLayout());

		//  Add a message label to the glass pane

		this.add(this.message, new GridBagConstraints());
		this.message.setOpaque(true);
		this.message.setBorder(MESSAGE_BORDER);

		//  Disable Mouse, Key and Focus events for the glass pane

		this.addMouseListener(new MouseAdapter() {});
		this.addMouseMotionListener(new MouseMotionAdapter() {});

		this.addKeyListener(this);

		this.setFocusTraversalKeysEnabled(false);
	}

	/*
	 *  The component is transparent but we want to paint the background
	 *  to give it the disabled look.
	 */
	@Override
	protected void paintComponent(final Graphics g)
	{
		g.setColor(this.getBackground());
		g.fillRect(0, 0, this.getSize().width, this.getSize().height);
	}

	/*
	 *  The	background color of the message label will be the same as the
	 *  background of the glass pane without the alpha value
	 */
	@Override
	public void setBackground(final Color background)
	{
		super.setBackground(background);

		final Color messageBackground = new Color(background.getRGB());
		this.message.setBackground(messageBackground);
	}

	//
//  Implement the KeyListener to consume events
//
	public void keyPressed(final KeyEvent e)
	{
		e.consume();
	}

	public void keyTyped(final KeyEvent e) {}

	public void keyReleased(final KeyEvent e)
	{
		e.consume();
	}

	/*
	 *  Make the glass pane visible and change the cursor to the wait cursor
	 *
	 *  A message can be displayed and it will be centered on the frame.
	 */
	public void activate(final String text)
	{
		if (text != null && text.length() > 0) {
			this.message.setVisible(true);
			this.message.setText(text);
			this.message.setForeground(this.getForeground());
		} else {
			this.message.setVisible(false);
		}

		this.setVisible(true);
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		this.requestFocusInWindow();
	}

	/*
	 *  Hide the glass pane and restore the cursor
	 */
	public void deactivate()
	{
		this.setCursor(null);
		this.setVisible(false);
	}
}
