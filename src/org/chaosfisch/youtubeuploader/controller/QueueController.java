/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/

package org.chaosfisch.youtubeuploader.controller;

import org.chaosfisch.youtubeuploader.services.uploader.Uploader;

import com.google.inject.Inject;

public class QueueController
{

	@Inject private Uploader	uploader;

	private void initComponents()
	{
		// final String[] queuefinishedaction = new String[] {
		// I18nHelper.message("queuefinishedlist.donothing"),
		// I18nHelper.message("queuefinishedlist.closeapplication"),
		// I18nHelper.message("queuefinishedlist.shutdown"),
		// I18nHelper.message("queuefinishedlist.hibernate") };
		// }
		//
		// final JCheckBox checkBox = new
		// JCheckBox(I18nHelper.message("upload.confirmdialog.checkbox"));
		// final Object[] message = {
		// I18nHelper.message("upload.confirmdialog.message"), checkBox };
		//
		// final int result = JOptionPane.showConfirmDialog(null, message,
		// I18nHelper.message("youtube.confirmdialog.title"),
		// JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		// if (result != 0) { return; }
		//
		// if (checkBox.isSelected())
		// {
		// settingsDao.set("coreplugin.general.uploadconfirmation", "false"); //
		// NON-NLS
		// settingsDao.save();
		// }
		// // ADD Arrow Listeners
		// arrowTop.addActionListener(new ActionListener() {
		// @Override
		// public void actionPerformed(final ActionEvent e)
		// {
		// final int selectedRow = queueTable.getSelectedRow();
		// if (controller.getQueueList().hasIndex(selectedRow))
		// {
		// controller.moveTop(controller.getQueueList().getRow(selectedRow));
		// }
		// }
		// });
		// arrowUp.addActionListener(new ActionListener() {
		// @Override
		// public void actionPerformed(final ActionEvent e)
		// {
		// final int selectedRow = queueTable.getSelectedRow();
		// if (controller.getQueueList().hasIndex(selectedRow))
		// {
		// controller.moveUp(controller.getQueueList().getRow(selectedRow));
		// }
		// }
		// });
		// arrowDown.addActionListener(new ActionListener() {
		// @Override
		// public void actionPerformed(final ActionEvent e)
		// {
		// final int selectedRow = queueTable.getSelectedRow();
		// if (controller.getQueueList().hasIndex(selectedRow))
		// {
		// controller.moveDown(controller.getQueueList().getRow(selectedRow));
		// }
		// }
		// });
		// arrowBottom.addActionListener(new ActionListener() {
		// @Override
		// public void actionPerformed(final ActionEvent e)
		// {
		// final int selectedRow = queueTable.getSelectedRow();
		// if (controller.getQueueList().hasIndex(selectedRow))
		// {
		// controller.moveBottom(controller.getQueueList().getRow(selectedRow));
		// }
		// }
		// });
		// });
	}

	/*
	 * public void moveBottom(final Queue queue) { queueService.sort(queue,
	 * QueuePosition.QUEUE_BOTTOM); queueList.sortQueueEntry(queue,
	 * QueuePosition.QUEUE_BOTTOM); }
	 * 
	 * public void moveDown(final Queue queue) { queueService.sort(queue,
	 * QueuePosition.QUEUE_DOWN); queueList.sortQueueEntry(queue,
	 * QueuePosition.QUEUE_DOWN); }
	 * 
	 * public void moveTop(final Queue queue) { queueService.sort(queue,
	 * QueuePosition.QUEUE_TOP); queueList.sortQueueEntry(queue,
	 * QueuePosition.QUEUE_TOP); }
	 * 
	 * public void moveUp(final Queue queue) { queueService.sort(queue,
	 * QueuePosition.QUEUE_UP); queueList.sortQueueEntry(queue,
	 * QueuePosition.QUEUE_UP); }
	 */
}