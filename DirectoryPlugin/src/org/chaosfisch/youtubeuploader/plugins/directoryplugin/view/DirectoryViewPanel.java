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
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.GenericListModel;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Preset;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.controller.DirectoryController;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.models.Directory;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.models.DirectoryTableModel;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.services.spi.DirectoryService;
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
	private JTable     directoryTable;
	private JPanel     contentPane;
	private JButton    addButton;
	private JButton    deleteButton;
	private JButton    selectButton;
	private JComboBox  presetList;
	private JTextField directoryTextField;
	private JCheckBox  activeCheckbox;

	private final   DirectoryController      directoryController;
	private final   DirectoryService         directoryService;
	private final   GenericListModel<Preset> presetListModel;
	@Inject private JFileChooser             fileChooser;

	@Inject
	public DirectoryViewPanel(final DirectoryController directoryController, final GenericListModel<Preset> presetListModel, final DirectoryService directoryService)
	{
		this.directoryController = directoryController;
		this.presetListModel = presetListModel;
		this.directoryService = directoryService;
		this.directoryController.run();
		setContentPane(contentPane);
		setModal(true);
		initCompononents();
		initListeners();
	}

	private void initCompononents()
	{
		presetList.removeAllItems();
		for (final Preset preset : directoryService.findPresets()) {
			presetListModel.addElement(preset);
		}
		presetList.setModel(presetListModel);
		directoryTable.setModel(directoryController.getDirectoryTableModel());
		directoryTable.setDefaultRenderer(Object.class, new DefaultTableRenderer()
		{
			private static final long serialVersionUID = -905536623165714507L;

			@Override public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column)
			{
				final Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				@SuppressWarnings("OverlyStrongTypeCast") final Directory directory = ((DirectoryTableModel) table.getModel()).getRow(row);
				if ((directory.locked != null) && directory.locked) {
					// noinspection MagicNumber
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
		addButton.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				if (directoryTextField.getText().isEmpty()) {
					directoryTextField.setBackground(Color.RED);
					return;
				}
				directoryTextField.setBackground(null);

				if (presetList.getSelectedIndex() == -1) {
					presetList.setBackground(Color.RED);
					return;
				}
				presetList.setBackground(null);

				directoryController.addAction(activeCheckbox.isSelected(), directoryTextField.getText(), (Preset) presetList.getSelectedItem());
			}
		});

		deleteButton.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				final DirectoryTableModel directoryTableModel = directoryController.getDirectoryTableModel();
				if ((directoryTableModel.getRowCount() > directoryTable.getSelectedRow()) && (directoryTable.getSelectedRow() > -1)) {
					directoryController.deleteAction(directoryTableModel.getRow(directoryTable.getSelectedRow()));
				}
			}
		});

		selectButton.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				final int result = fileChooser.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					directoryTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
				}
			}
		});

		activeCheckbox.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				final DirectoryTableModel directoryTableModel = directoryController.getDirectoryTableModel();
				if ((directoryTableModel.getRowCount() > directoryTable.getSelectedRow()) && (directoryTable.getSelectedRow() > -1)) {
					directoryController.checkboxChangeAction(activeCheckbox.isSelected(), directoryTableModel.getRow(directoryTable.getSelectedRow()));
				}
			}
		});

		directoryTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override public void valueChanged(final ListSelectionEvent e)
			{

				if (directoryTable.getSelectedRow() == -1) {
					return;
				}
				final Directory directory = directoryController.getDirectoryTableModel().getRow(directoryTable.getSelectedRow());
				if (directory != null) {
					activeCheckbox.setSelected(directory.active);
				}
			}
		});
	}
}
