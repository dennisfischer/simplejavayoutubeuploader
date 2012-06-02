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

package org.chaosfisch.youtubeuploader.plugins.directoryplugin.view;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.GenericListModel;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Preset;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PresetService;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.controller.DirectoryController;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.models.Directory;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.models.DirectoryTableModel;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 03.03.12
 * Time: 22:33
 * To change this template use File | Settings | File Templates.
 */
public class DirectoryViewPanel extends JDialog
{
	private static final long serialVersionUID = -7077457843089222621L;
	private JTable            directoryTable;
	private JPanel            contentPane;
	private JButton           addButton;
	private JButton           deleteButton;
	private JButton           selectButton;
	private JComboBox<Preset> presetList;
	private JTextField        directoryTextField;
	private JCheckBox         activeCheckbox;

	@Inject private DirectoryController      directoryController;
	@Inject private PresetService            presetService;
	@Inject private GenericListModel<Preset> presetListModel;
	@Inject private Injector                 injector;

	private void initCompononents()
	{
		for (final Preset preset : this.presetService.getAll()) {
			this.presetListModel.addElement(preset);
		}
		this.presetList.setModel(this.presetListModel);
		this.directoryTable.setModel(this.directoryController.getDirectoryTableModel());
		this.directoryTable.setDefaultRenderer(Object.class, new DefaultTableRenderer()
		{
			private static final long serialVersionUID = -905536623165714507L;

			@Override public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column)
			{
				final Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				final Directory directory = ((DirectoryTableModel) table.getModel()).getRow(row);
				if (directory.locked) {
					component.setBackground(new Color(250, 128, 114));
					component.setForeground(Color.white);
				} else {
					component.setBackground(null);
					component.setForeground(null);
				}
				return component;
			}
		});
	}

	private void initListeners()
	{
		this.addButton.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				if (DirectoryViewPanel.this.directoryTextField.getText().isEmpty()) {
					DirectoryViewPanel.this.directoryTextField.setBackground(Color.RED);
					return;
				}
				DirectoryViewPanel.this.directoryTextField.setBackground(null);

				if (DirectoryViewPanel.this.presetList.getSelectedIndex() == -1) {
					DirectoryViewPanel.this.presetList.setBackground(Color.RED);
					return;
				}
				DirectoryViewPanel.this.presetList.setBackground(null);

				DirectoryViewPanel.this.directoryController.addAction(DirectoryViewPanel.this.activeCheckbox.isSelected(), DirectoryViewPanel.this.directoryTextField.getText(), (Preset) DirectoryViewPanel.this.presetList.getSelectedItem());
			}
		});

		this.deleteButton.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				final DirectoryTableModel directoryTableModel = DirectoryViewPanel.this.directoryController.getDirectoryTableModel();
				if ((directoryTableModel.getRowCount() > DirectoryViewPanel.this.directoryTable.getSelectedRow()) && (DirectoryViewPanel.this.directoryTable.getSelectedRow() > -1)) {
					DirectoryViewPanel.this.directoryController.deleteAction(directoryTableModel.getRow(DirectoryViewPanel.this.directoryTable.getSelectedRow()));
				}
			}
		});

		this.selectButton.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				final JFileChooser fileChooser = DirectoryViewPanel.this.injector.getInstance(JFileChooser.class);
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				final int result = fileChooser.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					DirectoryViewPanel.this.directoryTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
				}
			}
		});

		this.activeCheckbox.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				final DirectoryTableModel directoryTableModel = DirectoryViewPanel.this.directoryController.getDirectoryTableModel();
				if ((directoryTableModel.getRowCount() > DirectoryViewPanel.this.directoryTable.getSelectedRow()) && (DirectoryViewPanel.this.directoryTable.getSelectedRow() > -1)) {
					DirectoryViewPanel.this.directoryController.checkboxChangeAction(DirectoryViewPanel.this.activeCheckbox.isSelected(), directoryTableModel.getRow(DirectoryViewPanel
																																											 .this.directoryTable.getSelectedRow()));
				}
			}
		});

		this.directoryTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override public void valueChanged(final ListSelectionEvent e)
			{

				if (DirectoryViewPanel.this.directoryTable.getSelectedRow() == -1) {
					return;
				}
				final Directory directory = DirectoryViewPanel.this.directoryController.getDirectoryTableModel().getRow(DirectoryViewPanel.this.directoryTable.getSelectedRow());
				if (directory != null) {
					DirectoryViewPanel.this.activeCheckbox.setSelected(directory.active);
				}
			}
		});
	}

	public void run()
	{
		this.directoryController.run();
		this.setContentPane(this.contentPane);
		this.setModal(true);
		this.initCompononents();
		this.initListeners();
	}
}
