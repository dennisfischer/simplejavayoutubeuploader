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
import org.chaosfisch.util.HyperlinkMouseAdapter;
import org.chaosfisch.util.ProgressbarTableCellRenderer;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.controller.QueueController;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Queue;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.QueueTableModel;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.Uploader;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.ColumnsAutoSizer;
import org.chaosfisch.youtubeuploader.services.settingsservice.spi.SettingsService;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

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

	private final ResourceBundle resourceBundle = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.plugins.coreplugin.resources.coreplugin"); //NON-NLS
	@Inject private SettingsService settingsService;

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
		for (final Queue queue : this.controller.getQueueService().getAll()) {
			this.controller.getQueueList().addRow(queue);
		}
	}

	private void initComponents()
	{
		this.queueTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.queueTable.setRowSorter(null);
		this.queueTable.setDefaultRenderer(Object.class, new DefaultTableRenderer()
		{
			private static final long serialVersionUID = -8124241179871597973L;

			@Override public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column)
			{
				final Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				@SuppressWarnings("OverlyStrongTypeCast") final Queue queue = ((QueueTableModel) table.getModel()).getRow(row);
				if (queue.locked) {
					component.setBackground(new Color(250, 128, 114));
					component.setForeground(Color.white);
				} else if (!isSelected) {
					component.setBackground(null);
					component.setForeground(null);
				}
				return component;
			}
		});

		this.queueTable.getColumn(this.queueTable.getColumnName(6)).setCellRenderer(new ProgressbarTableCellRenderer());
		this.queueTable.addMouseListener(new HyperlinkMouseAdapter(5));

		this.queueTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		//ColumnsAutoSizer.sizeColumnsToFit(this.queueTable);
		this.stoppenButton.setEnabled(false);

		this.speedLimitSpinner.setModel(new SpinnerNumberModel(0, 0, 10000, 10));
		this.speedLimitSpinner.setEditor(new JSpinner.NumberEditor(this.speedLimitSpinner, "Max: # kb/s")); //NON-NLS

		this.maxUploadsSpinner.setModel(new SpinnerNumberModel(1, 1, 10, 1));
		this.maxUploadsSpinner.setEditor(new JSpinner.NumberEditor(this.maxUploadsSpinner, "Max: # Uploads")); //NON-NLS

		this.queueFinishedList.setModel(new DefaultComboBoxModel(new String[]{this.resourceBundle.getString("queuefinishedlist.donothing"), this.resourceBundle.getString("queuefinishedlist.closeapplication"), this.resourceBundle.getString(
				"queuefinishedlist.shutdown"), this.resourceBundle.getString("queuefinishedlist.hibernate")}));
		this.queueViewList.setModel(new DefaultComboBoxModel(new String[]{this.resourceBundle.getString("queueviewlist.all"), this.resourceBundle.getString("queueviewlist.uploads"), this.resourceBundle.getString("queueviewlist.queue")}));
	}

	private void initListeners()
	{
		//Start, Stop, End Listeners
		this.startenButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if (Boolean.parseBoolean((String) QueueViewPanel.this.settingsService.get("coreplugin.general.uploadconfirmation", "true"))) { //NON-NLS
					final JCheckBox checkBox = new JCheckBox(QueueViewPanel.this.resourceBundle.getString("upload.confirmdialog.checkbox"));
					final Object[] message = {QueueViewPanel.this.resourceBundle.getString("upload.confirmdialog.message"), checkBox};

					final int result = JOptionPane.showConfirmDialog(null, message, QueueViewPanel.this.resourceBundle.getString("youtube.confirmdialog.title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (result != 0) {
						return;
					}

					if (checkBox.isSelected()) {
						QueueViewPanel.this.settingsService.set("coreplugin.general.uploadconfirmation", "false");
						QueueViewPanel.this.settingsService.save();
					}
				}

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
				if (QueueViewPanel.this.controller.getQueueList().hasIndex(selectedRow)) {
					QueueViewPanel.this.controller.abortUpload(QueueViewPanel.this.controller.getQueueList().getRow(selectedRow));
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
				if (QueueViewPanel.this.controller.getQueueList().hasIndex(selectedRow)) {
					QueueViewPanel.this.controller.moveTop(QueueViewPanel.this.controller.getQueueList().getRow(selectedRow));
				}
			}
		});
		this.arrowUp.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int selectedRow = QueueViewPanel.this.queueTable.getSelectedRow();
				if (QueueViewPanel.this.controller.getQueueList().hasIndex(selectedRow)) {
					QueueViewPanel.this.controller.moveUp(QueueViewPanel.this.controller.getQueueList().getRow(selectedRow));
				}
			}
		});
		this.arrowDown.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int selectedRow = QueueViewPanel.this.queueTable.getSelectedRow();
				if (QueueViewPanel.this.controller.getQueueList().hasIndex(selectedRow)) {
					QueueViewPanel.this.controller.moveDown(QueueViewPanel.this.controller.getQueueList().getRow(selectedRow));
				}
			}
		});
		this.arrowBottom.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int selectedRow = QueueViewPanel.this.queueTable.getSelectedRow();
				if (QueueViewPanel.this.controller.getQueueList().hasIndex(selectedRow)) {
					QueueViewPanel.this.controller.moveBottom(QueueViewPanel.this.controller.getQueueList().getRow(selectedRow));
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
				if (QueueViewPanel.this.controller.getQueueList().hasIndex(selectedRow)) {
					QueueViewPanel.this.controller.editEntry(QueueViewPanel.this.controller.getQueueList().getRow(selectedRow));
					QueueViewPanel.this.controller.deleteEntry(QueueViewPanel.this.controller.getQueueList().getRow(selectedRow));
				}
			}
		});

		this.deleteButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int[] selectedRows = QueueViewPanel.this.queueTable.getSelectedRows();
				final Collection<Queue> queues = new ArrayList<Queue>(QueueViewPanel.this.queueTable.getRowCount());
				for (final int selectedRow : selectedRows) {
					if (QueueViewPanel.this.controller.getQueueList().hasIndex(selectedRow)) {
						queues.add(QueueViewPanel.this.controller.getQueueList().getRow(selectedRow));
					}
				}

				for (final Queue queue : queues) {
					QueueViewPanel.this.controller.deleteEntry(queue);
				}
			}
		});

		this.queueViewList.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(final ItemEvent e)
			{
				if (e.getStateChange() == ItemEvent.SELECTED) {
					QueueViewPanel.this.controller.changeQueueView((short) QueueViewPanel.this.queueViewList.getSelectedIndex());
				}
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

	@EventTopicSubscriber(topic = Uploader.UPLOAD_FINISHED)
	public void onUploadFinished(final String topic, final Object o)
	{
		this.startenButton.setEnabled(true);
		this.stoppenButton.setEnabled(false);
	}

	@EventTopicSubscriber(topic = Uploader.QUEUE_START)
	public void onQueueStart(final String topic, final Object o)
	{
		this.startenButton.setEnabled(false);
		this.stoppenButton.setEnabled(true);
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
