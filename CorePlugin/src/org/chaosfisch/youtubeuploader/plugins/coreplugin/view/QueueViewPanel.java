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
		queueTable.setModel(controller.getQueueList());

		initComponents();
		initListeners();

		AnnotationProcessor.process(this);
		setup();
	}

	private void setup()
	{
		for (final Queue queue : controller.getQueueService().getAll()) {
			controller.getQueueList().addRow(queue);
		}
	}

	private void initComponents()
	{
		queueTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		queueTable.setRowSorter(null);
		queueTable.setDefaultRenderer(Object.class, new DefaultTableRenderer()
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

		queueTable.getColumn(queueTable.getColumnName(6)).setCellRenderer(new ProgressbarTableCellRenderer());
		queueTable.addMouseListener(new HyperlinkMouseAdapter(5));

		queueTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		//ColumnsAutoSizer.sizeColumnsToFit(this.queueTable);
		stoppenButton.setEnabled(false);

		speedLimitSpinner.setModel(new SpinnerNumberModel(0, 0, 10000, 10));
		speedLimitSpinner.setEditor(new JSpinner.NumberEditor(speedLimitSpinner, "Max: # kb/s")); //NON-NLS

		maxUploadsSpinner.setModel(new SpinnerNumberModel(1, 1, 10, 1));
		maxUploadsSpinner.setEditor(new JSpinner.NumberEditor(maxUploadsSpinner, "Max: # Uploads")); //NON-NLS

		queueFinishedList.setModel(new DefaultComboBoxModel(new String[]{resourceBundle.getString("queuefinishedlist.donothing"), resourceBundle.getString(
				"queuefinishedlist.closeapplication"), resourceBundle.getString("queuefinishedlist.shutdown"), resourceBundle.getString("queuefinishedlist.hibernate")}));
		queueViewList.setModel(new DefaultComboBoxModel(new String[]{resourceBundle.getString("queueviewlist.all"), resourceBundle.getString("queueviewlist.uploads"), resourceBundle.getString(
				"queueviewlist.queue")}));
	}

	private void initListeners()
	{
		//Start, Stop, End Listeners
		startenButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if (Boolean.parseBoolean((String) settingsService.get("coreplugin.general.uploadconfirmation", "true"))) { //NON-NLS
					final JCheckBox checkBox = new JCheckBox(resourceBundle.getString("upload.confirmdialog.checkbox"));
					final Object[] message = {resourceBundle.getString("upload.confirmdialog.message"), checkBox};

					final int result = JOptionPane.showConfirmDialog(null, message, resourceBundle.getString("youtube.confirmdialog.title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (result != 0) {
						return;
					}

					if (checkBox.isSelected()) {
						settingsService.set("coreplugin.general.uploadconfirmation", "false"); //NON-NLS
						settingsService.save();
					}
				}

				controller.startQueue();
				startenButton.setEnabled(false);
				stoppenButton.setEnabled(true);
			}
		});

		stoppenButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				controller.stopQueue();
				startenButton.setEnabled(true);
				stoppenButton.setEnabled(false);
			}
		});

		abortButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int selectedRow = queueTable.getSelectedRow();
				if (controller.getQueueList().hasIndex(selectedRow)) {
					controller.abortUpload(controller.getQueueList().getRow(selectedRow));
				}
			}
		});

		queueFinishedList.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(final ItemEvent e)
			{
				controller.changeQueueFinished(queueFinishedList.getSelectedIndex());
			}
		});

		//ADD Arrow Listeners
		arrowTop.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int selectedRow = queueTable.getSelectedRow();
				if (controller.getQueueList().hasIndex(selectedRow)) {
					controller.moveTop(controller.getQueueList().getRow(selectedRow));
				}
			}
		});
		arrowUp.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int selectedRow = queueTable.getSelectedRow();
				if (controller.getQueueList().hasIndex(selectedRow)) {
					controller.moveUp(controller.getQueueList().getRow(selectedRow));
				}
			}
		});
		arrowDown.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int selectedRow = queueTable.getSelectedRow();
				if (controller.getQueueList().hasIndex(selectedRow)) {
					controller.moveDown(controller.getQueueList().getRow(selectedRow));
				}
			}
		});
		arrowBottom.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int selectedRow = queueTable.getSelectedRow();
				if (controller.getQueueList().hasIndex(selectedRow)) {
					controller.moveBottom(controller.getQueueList().getRow(selectedRow));
				}
			}
		});

		//Edit, Delete, View Buttons
		editButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int selectedRow = queueTable.getSelectedRow();
				if (controller.getQueueList().hasIndex(selectedRow)) {
					controller.editEntry(controller.getQueueList().getRow(selectedRow));
					controller.deleteEntry(controller.getQueueList().getRow(selectedRow));
				}
			}
		});

		deleteButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int[] selectedRows = queueTable.getSelectedRows();
				final Collection<Queue> queues = new ArrayList<Queue>(queueTable.getRowCount());
				for (final int selectedRow : selectedRows) {
					if (controller.getQueueList().hasIndex(selectedRow)) {
						queues.add(controller.getQueueList().getRow(selectedRow));
					}
				}

				for (final Queue queue : queues) {
					controller.deleteEntry(queue);
				}
			}
		});

		queueViewList.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(final ItemEvent e)
			{
				if (e.getStateChange() == ItemEvent.SELECTED) {
					controller.changeQueueView((short) queueViewList.getSelectedIndex());
				}
			}
		});

		controller.getQueueList().addTableModelListener(new TableModelListener()
		{
			@Override
			public void tableChanged(final TableModelEvent e)
			{
				ColumnsAutoSizer.sizeColumnsToFit(queueTable);
			}
		});
		queuePanel.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(final ComponentEvent e)
			{
				ColumnsAutoSizer.sizeColumnsToFit(queueTable);
			}
		});
		speedLimitSpinner.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(final ChangeEvent e)
			{
				controller.changeSpeedLimit((Integer) speedLimitSpinner.getValue());
			}
		});
		maxUploadsSpinner.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(final ChangeEvent e)
			{
				controller.changeMaxUpload(Short.parseShort(maxUploadsSpinner.getValue().toString()));
			}
		});
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_FINISHED)
	public void onUploadFinished(final String topic, final Object o)
	{
		startenButton.setEnabled(true);
		stoppenButton.setEnabled(false);
	}

	@EventTopicSubscriber(topic = Uploader.QUEUE_START)
	public void onQueueStart(final String topic, final Object o)
	{
		startenButton.setEnabled(false);
		stoppenButton.setEnabled(true);
	}

	public JPanel getJPanel()
	{
		return queuePanel;
	}

	public QueueController getQueueController()
	{
		return controller;
	}
}
