/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Dennis Fischer
 ******************************************************************************/

package org.chaosfisch.youtubeuploader.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.chaosfisch.youtubeuploader.controller.DirectoryController;
import org.chaosfisch.youtubeuploader.dao.spi.DirectoryDao;
import org.chaosfisch.youtubeuploader.models.Directory;
import org.chaosfisch.youtubeuploader.models.Preset;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

import com.google.inject.Inject;

public class DirectoryViewPanel extends JDialog
{
	private static final long				serialVersionUID	= -7077457843089222621L;
	private JTable							directoryTable;
	private JPanel							contentPane;
	private JButton							addButton;
	private JButton							deleteButton;
	private JButton							selectButton;
	private JComboBox						presetList;
	private JTextField						directoryTextField;
	private JCheckBox						activeCheckbox;

	private final DirectoryController		directoryController;
	private final DirectoryDao				directoryService;
	private final GenericListModel<Preset>	presetListModel;
	@Inject
	private JFileChooser					fileChooser;

	@Inject
	public DirectoryViewPanel(final DirectoryController directoryController, final GenericListModel<Preset> presetListModel,
			final DirectoryDao directoryService)
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
		for (final Preset preset : directoryService.findPresets())
		{
			presetListModel.addElement(preset);
		}
		presetList.setModel(presetListModel);
		directoryTable.setModel(directoryController.getDirectoryTableModel());
		directoryTable.setDefaultRenderer(Object.class, new DefaultTableRenderer() {
			private static final long	serialVersionUID	= -905536623165714507L;

			@Override
			public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus,
					final int row, final int column)
			{
				final Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				final Directory directory = ((DirectoryTableModel) table.getModel()).getRow(row);
				if ((directory.locked != null) && directory.locked)
				{
					// noinspection MagicNumber
					component.setBackground(new Color(250, 128, 114));
					component.setForeground(Color.white);
				} else
				{
					component.setBackground(null);
					component.setForeground(null);
				}
				return component;
			}
		});
	}

	private void initListeners()
	{
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				if (directoryTextField.getText().isEmpty())
				{
					directoryTextField.setBackground(Color.RED);
					return;
				}
				directoryTextField.setBackground(null);

				if (presetList.getSelectedIndex() == -1)
				{
					presetList.setBackground(Color.RED);
					return;
				}
				presetList.setBackground(null);

				directoryController.addAction(activeCheckbox.isSelected(), directoryTextField.getText(), (Preset) presetList.getSelectedItem());
			}
		});

		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final DirectoryTableModel directoryTableModel = directoryController.getDirectoryTableModel();
				if ((directoryTableModel.getRowCount() > directoryTable.getSelectedRow()) && (directoryTable.getSelectedRow() > -1))
				{
					directoryController.deleteAction(directoryTableModel.getRow(directoryTable.getSelectedRow()));
				}
			}
		});

		selectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				final int result = fileChooser.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION)
				{
					directoryTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
				}
			}
		});

		activeCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final DirectoryTableModel directoryTableModel = directoryController.getDirectoryTableModel();
				if ((directoryTableModel.getRowCount() > directoryTable.getSelectedRow()) && (directoryTable.getSelectedRow() > -1))
				{
					directoryController.checkboxChangeAction(activeCheckbox.isSelected(), directoryTableModel.getRow(directoryTable.getSelectedRow()));
				}
			}
		});

		directoryTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(final ListSelectionEvent e)
			{

				if (directoryTable.getSelectedRow() == -1) { return; }
				final Directory directory = directoryController.getDirectoryTableModel().getRow(directoryTable.getSelectedRow());
				if (directory != null)
				{
					activeCheckbox.setSelected(directory.active);
				}
			}
		});
	}
}
