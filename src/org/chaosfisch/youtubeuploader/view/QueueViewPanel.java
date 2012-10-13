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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.youtubeuploader.I18nHelper;
import org.chaosfisch.youtubeuploader.controller.QueueController;
import org.chaosfisch.youtubeuploader.models.Queue;
import org.chaosfisch.youtubeuploader.services.uploader.Uploader;

import com.google.inject.Inject;

public final class QueueViewPanel
{
	private JButton					abortButton;
	private JButton					arrowBottom;
	private JButton					arrowDown;
	private JButton					arrowTop;
	private JButton					arrowUp;
	private final QueueController	controller;
	private JButton					deleteButton;
	private JButton					editButton;
	private JSpinner				maxUploadsSpinner;
	private JComboBox				queueFinishedList;
	private JPanel					queuePanel;
	public JTable					queueTable;
	private JComboBox				queueViewList;
	@Inject private SettingsDao		settingsDao;
	private JSpinner				speedLimitSpinner;
	private JButton					startenButton;

	private JButton					stoppenButton;

	@Inject
	public QueueViewPanel(final QueueController controller)
	{
		this.controller = controller;
		queueTable.setModel(controller.getQueueList());

		initComponents();
		initListeners();

		AnnotationProcessor.process(this);
	}

	private void initComponents()
	{
		queueTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		queueTable.setRowSorter(null);
		queueTable.setDefaultRenderer(Object.class, new DefaultTableRenderer() {
			private static final long	serialVersionUID	= -8124241179871597973L;

			@Override
			public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus,
					final int row, final int column)
			{
				final Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				final Queue queue = ((QueueTableModel) table.getModel()).getRow(row);
				if (queue.locked)
				{
					component.setBackground(new Color(250, 128, 114));
					component.setForeground(Color.white);
				} else if (!isSelected)
				{
					component.setBackground(null);
					component.setForeground(null);
				}
				return component;
			}
		});

		queueTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		// ColumnsAutoSizer.sizeColumnsToFit(this.queueTable);
		stoppenButton.setEnabled(false);

		speedLimitSpinner.setModel(new SpinnerNumberModel(0, 0, 10000, 10));
		speedLimitSpinner.setEditor(new JSpinner.NumberEditor(speedLimitSpinner, "Max: # kb/s")); // NON-NLS

		maxUploadsSpinner.setModel(new SpinnerNumberModel(1, 1, 10, 1));
		maxUploadsSpinner.setEditor(new JSpinner.NumberEditor(maxUploadsSpinner, "Max: # Uploads")); // NON-NLS

		queueFinishedList.setModel(new DefaultComboBoxModel(new String[] { I18nHelper.message("queuefinishedlist.donothing"),
				I18nHelper.message("queuefinishedlist.closeapplication"), I18nHelper.message("queuefinishedlist.shutdown"),
				I18nHelper.message("queuefinishedlist.hibernate") }));
		queueViewList.setModel(new DefaultComboBoxModel(new String[] { I18nHelper.message("queueviewlist.all"),
				I18nHelper.message("queueviewlist.uploads"), I18nHelper.message("queueviewlist.queue") }));
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

		abortButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int selectedRow = queueTable.getSelectedRow();
				if (controller.getQueueList().hasIndex(selectedRow))
				{
					controller.abortUpload(controller.getQueueList().getRow(selectedRow));
				}
			}
		});

		queueFinishedList.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e)
			{
				controller.changeQueueFinished(queueFinishedList.getSelectedIndex());
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

		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int[] selectedRows = queueTable.getSelectedRows();
				final Collection<Queue> queues = new ArrayList<Queue>(queueTable.getRowCount());
				for (final int selectedRow : selectedRows)
				{
					if (controller.getQueueList().hasIndex(selectedRow))
					{
						queues.add(controller.getQueueList().getRow(selectedRow));
					}
				}

				for (final Queue queue : queues)
				{
					controller.deleteEntry(queue);
				}
			}
		});

		queueViewList.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e)
			{
				if (e.getStateChange() == ItemEvent.SELECTED)
				{
					controller.changeQueueView((short) queueViewList.getSelectedIndex());
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