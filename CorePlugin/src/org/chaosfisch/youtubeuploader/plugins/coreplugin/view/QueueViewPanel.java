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

/*
 * DisplayViewPanel.java
 *
 * Created on January 22, 2007, 2:36 PM
 */
package org.chaosfisch.youtubeuploader.plugins.coreplugin.view;

import com.google.inject.Inject;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.util.ProgressbarTableCellRenderer;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.controller.QueueController;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.QueueTableModel;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.entities.QueueEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.Uploader;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.ColumnsAutoSizer;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.*;

public final class QueueViewPanel
{
	private final QueueController controller;
	private       JPanel          queuePanel;
	public        JTable          queueTable;
	private       JButton         startenButton;
	private       JButton         stoppenButton;
	private       JButton         arrowTop;
	private       JButton         arrowUp;
	private       JButton         arrowDown;
	private       JButton         arrowBottom;
	private       JComboBox       queueFinishedList;
	private       JButton         editButton;
	private       JButton         deleteButton;
	private       JComboBox       queueViewList;
	private       JButton         abortButton;
	private       JSpinner        speedLimitSpinner;
	private       JSpinner        maxUploadsSpinner;

	@Inject
	public QueueViewPanel(final QueueController controller)
	{
		this.controller = controller;
		this.queueTable.setModel(controller.getQueueList());

		this.initComponents();
		this.initListeners();

		AnnotationProcessor.process(this);
		this.setup();
	}

	private void setup()
	{
		this.controller.getQueueList().addQueueEntryList(this.controller.getQueueService().getAllQueueEntry());
	}

	private void initComponents()
	{
		this.queueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.queueTable.getColumn(this.queueTable.getColumnName(6)).setCellRenderer(new ProgressbarTableCellRenderer());
		this.queueTable.setDefaultRenderer(Object.class, new DefaultTableRenderer()
		{
			@Override public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column)
			{
				final Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				final QueueEntry queueEntry = ((QueueTableModel) table.getModel()).getQueueEntryAt(row);
				if (queueEntry.isLocked()) {
					component.setBackground(new Color(250, 128, 114));
					component.setForeground(Color.white);
				} else {
					component.setBackground(null);
					component.setForeground(null);
				}
				return component;
			}
		});

		this.queueTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		ColumnsAutoSizer.sizeColumnsToFit(this.queueTable);
		this.stoppenButton.setEnabled(false);

		this.speedLimitSpinner.setModel(new SpinnerNumberModel(0, 0, 10000, 10));
		this.speedLimitSpinner.setEditor(new JSpinner.NumberEditor(this.speedLimitSpinner, "Max: # kb/s")); //NON-NLS

		this.maxUploadsSpinner.setModel(new SpinnerNumberModel(1, 1, 10, 1));
		this.maxUploadsSpinner.setEditor(new JSpinner.NumberEditor(this.maxUploadsSpinner, "Max: # Uploads")); //NON-NLS
	}

	private void initListeners()
	{
		//Start, Stop, End Listeners
		this.startenButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				QueueViewPanel.this.controller.startQueue();
				QueueViewPanel.this.startenButton.setEnabled(false);
				QueueViewPanel.this.stoppenButton.setEnabled(true);
			}
		});

		this.stoppenButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				QueueViewPanel.this.controller.stopQueue();
				QueueViewPanel.this.startenButton.setEnabled(true);
				QueueViewPanel.this.stoppenButton.setEnabled(false);
			}
		});

		this.abortButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int selectedRow = QueueViewPanel.this.queueTable.getSelectedRow();
				if (QueueViewPanel.this.controller.getQueueList().hasQueueEntryAt(selectedRow)) {
					QueueViewPanel.this.controller.abortUpload(QueueViewPanel.this.controller.getQueueList().getQueueEntryAt(selectedRow));
				}
			}
		});

		this.queueFinishedList.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(final ItemEvent e)
			{
				QueueViewPanel.this.controller.changeQueueFinished(QueueViewPanel.this.queueFinishedList.getSelectedIndex());
			}
		});

		//ADD Arrow Listeners
		this.arrowTop.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int selectedRow = QueueViewPanel.this.queueTable.getSelectedRow();
				if (QueueViewPanel.this.controller.getQueueList().hasQueueEntryAt(selectedRow)) {
					QueueViewPanel.this.controller.moveTop(QueueViewPanel.this.controller.getQueueList().getQueueEntryAt(selectedRow));
				}
			}
		});
		this.arrowUp.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int selectedRow = QueueViewPanel.this.queueTable.getSelectedRow();
				if (QueueViewPanel.this.controller.getQueueList().hasQueueEntryAt(selectedRow)) {
					QueueViewPanel.this.controller.moveUp(QueueViewPanel.this.controller.getQueueList().getQueueEntryAt(selectedRow));
				}
			}
		});
		this.arrowDown.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int selectedRow = QueueViewPanel.this.queueTable.getSelectedRow();
				if (QueueViewPanel.this.controller.getQueueList().hasQueueEntryAt(selectedRow)) {
					QueueViewPanel.this.controller.moveDown(QueueViewPanel.this.controller.getQueueList().getQueueEntryAt(selectedRow));
				}
			}
		});
		this.arrowBottom.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int selectedRow = QueueViewPanel.this.queueTable.getSelectedRow();
				if (QueueViewPanel.this.controller.getQueueList().hasQueueEntryAt(selectedRow)) {
					QueueViewPanel.this.controller.moveBottom(QueueViewPanel.this.controller.getQueueList().getQueueEntryAt(selectedRow));
				}
			}
		});

		//Edit, Delete, View Buttons
		this.editButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int selectedRow = QueueViewPanel.this.queueTable.getSelectedRow();
				if (QueueViewPanel.this.controller.getQueueList().hasQueueEntryAt(selectedRow)) {
					QueueViewPanel.this.controller.editEntry(QueueViewPanel.this.controller.getQueueList().getQueueEntryAt(selectedRow));
					QueueViewPanel.this.controller.deleteEntry(QueueViewPanel.this.controller.getQueueList().getQueueEntryAt(selectedRow));
				}
			}
		});

		this.deleteButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int selectedRow = QueueViewPanel.this.queueTable.getSelectedRow();
				if (QueueViewPanel.this.controller.getQueueList().hasQueueEntryAt(selectedRow)) {
					QueueViewPanel.this.controller.deleteEntry(QueueViewPanel.this.controller.getQueueList().getQueueEntryAt(selectedRow));
					QueueViewPanel.this.queueTable.changeSelection(selectedRow - 1, 0, false, false);
				}
			}
		});

		this.queueViewList.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(final ItemEvent e)
			{
				QueueViewPanel.this.controller.changeQueueView((short) QueueViewPanel.this.queueViewList.getSelectedIndex());
			}
		});

		this.controller.getQueueList().addTableModelListener(new TableModelListener()
		{
			@Override
			public void tableChanged(final TableModelEvent e)
			{
				ColumnsAutoSizer.sizeColumnsToFit(QueueViewPanel.this.queueTable);
			}
		});
		this.queuePanel.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(final ComponentEvent e)
			{
				ColumnsAutoSizer.sizeColumnsToFit(QueueViewPanel.this.queueTable);
			}
		});
		this.speedLimitSpinner.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(final ChangeEvent e)
			{
				QueueViewPanel.this.controller.changeSpeedLimit((Integer) QueueViewPanel.this.speedLimitSpinner.getValue());
			}
		});
		this.maxUploadsSpinner.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(final ChangeEvent e)
			{
				QueueViewPanel.this.controller.changeMaxUpload(Short.parseShort(QueueViewPanel.this.maxUploadsSpinner.getValue().toString()));
			}
		});
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = Uploader.UPLOAD_FINISHED)
	public void onUploadFinished(final String topic, final Object o)
	{
		this.startenButton.setEnabled(true);
		this.stoppenButton.setEnabled(false);
	}

	@EventTopicSubscriber(topic = Uploader.QUEUE_START)
	public void onQueueStart(final String topic, final Object o)
	{
		QueueViewPanel.this.startenButton.setEnabled(false);
		QueueViewPanel.this.stoppenButton.setEnabled(true);
	}

	public JPanel getJPanel()
	{
		return this.queuePanel;
	}

	public QueueController getQueueController()
	{
		return this.controller;
	}
}
