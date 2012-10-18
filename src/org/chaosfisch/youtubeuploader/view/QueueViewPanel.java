/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.youtubeuploader.I18nHelper;
import org.chaosfisch.youtubeuploader.services.uploader.Uploader;

public final class QueueViewPanel
{
	private void initComponents()
	{
		speedLimitSpinner.setModel(new SpinnerNumberModel(0, 0, 10000, 10));
		speedLimitSpinner.setEditor(new JSpinner.NumberEditor(speedLimitSpinner, "Max: # kb/s")); // NON-NLS

		maxUploadsSpinner.setModel(new SpinnerNumberModel(1, 1, 10, 1));
		maxUploadsSpinner.setEditor(new JSpinner.NumberEditor(maxUploadsSpinner, "Max: # Uploads")); // NON-NLS

		final String[] queuefinishedaction = new String[] { I18nHelper.message("queuefinishedlist.donothing"),
				I18nHelper.message("queuefinishedlist.closeapplication"), I18nHelper.message("queuefinishedlist.shutdown"),
				I18nHelper.message("queuefinishedlist.hibernate") };
	}

	private void initListeners()
	{
		// Start, Stop, End Listeners
		startenButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if (Boolean.parseBoolean((String) settingsDao.get("coreplugin.general.uploadconfirmation", "true")))
				{ // NON-NLS
					final JCheckBox checkBox = new JCheckBox(I18nHelper.message("upload.confirmdialog.checkbox"));
					final Object[] message = { I18nHelper.message("upload.confirmdialog.message"), checkBox };

					final int result = JOptionPane.showConfirmDialog(null, message, I18nHelper.message("youtube.confirmdialog.title"),
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (result != 0) { return; }

					if (checkBox.isSelected())
					{
						settingsDao.set("coreplugin.general.uploadconfirmation", "false"); // NON-NLS
						settingsDao.save();
					}
				}

				controller.startQueue();
				startenButton.setEnabled(false);
				stoppenButton.setEnabled(true);
			}
		});

		stoppenButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				controller.stopQueue();
				startenButton.setEnabled(true);
				stoppenButton.setEnabled(false);
			}
		});

		// ADD Arrow Listeners
		arrowTop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int selectedRow = queueTable.getSelectedRow();
				if (controller.getQueueList().hasIndex(selectedRow))
				{
					controller.moveTop(controller.getQueueList().getRow(selectedRow));
				}
			}
		});
		arrowUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int selectedRow = queueTable.getSelectedRow();
				if (controller.getQueueList().hasIndex(selectedRow))
				{
					controller.moveUp(controller.getQueueList().getRow(selectedRow));
				}
			}
		});
		arrowDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int selectedRow = queueTable.getSelectedRow();
				if (controller.getQueueList().hasIndex(selectedRow))
				{
					controller.moveDown(controller.getQueueList().getRow(selectedRow));
				}
			}
		});
		arrowBottom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int selectedRow = queueTable.getSelectedRow();
				if (controller.getQueueList().hasIndex(selectedRow))
				{
					controller.moveBottom(controller.getQueueList().getRow(selectedRow));
				}
			}
		});

		// Edit, Delete, View Buttons
		editButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int selectedRow = queueTable.getSelectedRow();
				if (controller.getQueueList().hasIndex(selectedRow))
				{
					controller.editEntry(controller.getQueueList().getRow(selectedRow));
					controller.deleteEntry(controller.getQueueList().getRow(selectedRow));
				}
			}
		});

		speedLimitSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e)
			{
				controller.changeSpeedLimit((Integer) speedLimitSpinner.getValue());
			}
		});
		maxUploadsSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e)
			{
				controller.changeMaxUpload(Short.parseShort(maxUploadsSpinner.getValue().toString()));
			}
		});
	}

	@EventTopicSubscriber(topic = Uploader.QUEUE_START)
	public void onQueueStart(final String topic, final Object o)
	{
		startenButton.setEnabled(false);
		stoppenButton.setEnabled(true);
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_FINISHED)
	public void onUploadFinished(final String topic, final Object o)
	{
		startenButton.setEnabled(true);
		stoppenButton.setEnabled(false);
	}

}