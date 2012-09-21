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

package org.chaosfisch.youtubeuploader;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 18.04.12
 * Time: 16:29
 * To change this template use File | Settings | File Templates.
 */
public class DisabledGlassPane extends JComponent implements KeyListener
{
	private static final Border       MESSAGE_BORDER   = new EmptyBorder(10, 10, 10, 10);
	private static final long         serialVersionUID = 6021041734286185845L;
	private static final int          DEFAULT_ALPHA    = 128;
	private final        JLabel       message          = new JLabel();
	private final        JProgressBar progressBar      = new JProgressBar(0, 100);
	private Timer timer;

	public DisabledGlassPane()
	{
		//  Set glass pane properties

		setOpaque(false);
		final Color base = UIManager.getColor("inactiveCaptionBorder");
		final Color background = new Color(base.getRed(), base.getGreen(), base.getBlue(), DisabledGlassPane.DEFAULT_ALPHA);
		setBackground(background);
		setLayout(new GridBagLayout());

		//  Add a message label to the glass pane
		final GridBagConstraints mGrid = new GridBagConstraints();
		mGrid.gridx = 0;
		mGrid.gridy = 0;

		final GridBagConstraints pGrid = new GridBagConstraints();
		pGrid.gridx = 0;
		pGrid.gridy = 1;
		pGrid.fill = GridBagConstraints.HORIZONTAL;

		add(message, mGrid);
		add(progressBar, pGrid);
		message.setOpaque(true);
		message.setBorder(DisabledGlassPane.MESSAGE_BORDER);

		//  Disable Mouse, Key and Focus events for the glass pane

		addMouseListener(new MouseAdapter() {});
		addMouseMotionListener(new MouseMotionAdapter() {});

		addKeyListener(this);

		setFocusTraversalKeysEnabled(false);
	}

	/*
	 *  The component is transparent but we want to paint the background
	 *  to give it the disabled look.
	 */
	@Override
	protected void paintComponent(final Graphics g)
	{
		g.setColor(getBackground());
		g.fillRect(0, 0, getSize().width, getSize().height);
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
		message.setBackground(messageBackground);
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
		if ((text != null) && !text.isEmpty()) {
			message.setVisible(true);
			message.setText(text);
			message.setForeground(getForeground());
		} else {
			message.setVisible(false);
		}
		startTimer();
		setVisible(true);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		requestFocusInWindow();
	}

	/*
	 *  Hide the glass pane and restore the cursor
	 */
	public void deactivate()
	{
		setCursor(null);
		setVisible(false);
		timer.stop();
	}

	void startTimer()
	{
		if (timer == null) {
			timer = new Timer(600, new ActionListener()
			{
				int progress;

				public void actionPerformed(final ActionEvent event)
				{
					progress += 1;
					progressBar.setValue(progress);

					// Once we hit 100%, remove the glass pane and reset the
					// progress bar stuff
					if (progress >= 100) {
						progress = 0;
						timer.stop();
						progressBar.setValue(0);
					}
				}
			});
		}
		if (timer.isRunning()) {
			timer.stop();
		}
		timer.start();
	}
}
