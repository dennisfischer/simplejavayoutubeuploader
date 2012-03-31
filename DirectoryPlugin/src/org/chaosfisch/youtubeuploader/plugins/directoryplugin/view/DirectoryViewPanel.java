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
import org.chaosfisch.youtubeuploader.db.PresetEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.PresetListModel;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.controller.DirectoryController;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.db.DirectoryEntry;
import org.chaosfisch.youtubeuploader.plugins.directoryplugin.models.DirectoryTableModel;
import org.chaosfisch.youtubeuploader.services.PresetService;

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
	private JTable     directoryTable;
	private JPanel     contentPane;
	private JButton    addButton;
	private JButton    deleteButton;
	private JButton    selectButton;
	private JComboBox  presetList;
	private JTextField directoryTextField;
	private JCheckBox  activeCheckbox;

	@Inject private DirectoryController directoryController;
	@Inject private PresetService       presetService;
	@Inject private PresetListModel     presetListModel;
	@Inject private Injector            injector;

	public DirectoryViewPanel()
	{
	}

	private void initCompononents()
	{
		this.presetListModel.addPresetEntryList(this.presetService.getAllPresetEntry());
		this.presetList.setModel(this.presetListModel);
		this.directoryTable.setModel(this.directoryController.getDirectoryTableModel());
	}

	private void initListeners()
	{
		this.addButton.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				if (DirectoryViewPanel.this.directoryTextField.getText().equals("")) {
					DirectoryViewPanel.this.directoryTextField.setBackground(Color.RED);
					return;
				}
				DirectoryViewPanel.this.directoryTextField.setBackground(null);

				if (DirectoryViewPanel.this.presetList.getSelectedIndex() == -1) {
					DirectoryViewPanel.this.presetList.setBackground(Color.RED);
					return;
				}
				DirectoryViewPanel.this.presetList.setBackground(null);

				DirectoryViewPanel.this.directoryController.addAction(DirectoryViewPanel.this.activeCheckbox.isSelected(), DirectoryViewPanel.this.directoryTextField.getText(),
						(PresetEntry) DirectoryViewPanel.this.presetList.getSelectedItem());
			}
		});

		this.deleteButton.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				final DirectoryTableModel directoryTableModel = DirectoryViewPanel.this.directoryController.getDirectoryTableModel();
				if (directoryTableModel.hasDirectoryAt(DirectoryViewPanel.this.directoryTable.getSelectedRow())) {
					DirectoryViewPanel.this.directoryController.deleteAction(directoryTableModel.getDirectoryAt(DirectoryViewPanel.this.directoryTable.getSelectedRow()));
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
				if (directoryTableModel.hasDirectoryAt(DirectoryViewPanel.this.directoryTable.getSelectedRow())) {
					DirectoryViewPanel.this.directoryController.checkboxChangeAction(DirectoryViewPanel.this.activeCheckbox.isSelected(), directoryTableModel.getDirectoryAt(DirectoryViewPanel
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
				final DirectoryEntry directoryEntry = DirectoryViewPanel.this.directoryController.getDirectoryTableModel().getDirectoryAt(DirectoryViewPanel.this.directoryTable.getSelectedRow());
				if (directoryEntry != null) {
					DirectoryViewPanel.this.activeCheckbox.setSelected(directoryEntry.isActive());
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
