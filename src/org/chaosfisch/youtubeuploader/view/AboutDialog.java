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

package org.chaosfisch.youtubeuploader.view;

import javax.swing.*;
import java.awt.event.*;

public class AboutDialog extends JDialog
{
	private JPanel  contentPane;
	private JButton buttonOK;
	private JButton buttonCancel;

	public AboutDialog()
	{
		this.setContentPane(this.contentPane);
		this.setModal(true);
		this.getRootPane().setDefaultButton(this.buttonOK);

		this.buttonOK.addActionListener(new ActionListener()
		{
			public void actionPerformed(final ActionEvent e) {AboutDialog.this.onOK();}
		});

		this.buttonCancel.addActionListener(new ActionListener()
		{
			public void actionPerformed(final ActionEvent e) {AboutDialog.this.onCancel();}
		});

// call onCancel() when cross is clicked
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(final WindowEvent e)
			{
				AboutDialog.this.onCancel();
			}
		});

// call onCancel() on ESCAPE
		this.contentPane.registerKeyboardAction(new ActionListener()
		{
			public void actionPerformed(final ActionEvent e)
			{
				AboutDialog.this.onCancel();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}

	private void onOK()
	{
// add your code here
		this.dispose();
	}

	private void onCancel()
	{
// add your code here if necessary
		this.dispose();
	}
}
