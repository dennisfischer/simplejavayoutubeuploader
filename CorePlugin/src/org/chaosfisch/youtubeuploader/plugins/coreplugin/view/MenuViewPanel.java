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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.view;

import com.google.inject.Inject;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.controller.UploadController;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Account;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Playlist;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Preset;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.AccountService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PlaylistService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PresetService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 28.01.12
 * Time: 14:57
 * To change this template use File | Settings | File Templates.
 */
public class MenuViewPanel
{
	private final UploadController uploadController;
	private final AccountService   accountService;
	private final PresetService    presetService;
	private final PlaylistService  playlistService;

	private JMenu importMenu;
	private JMenu exportMenu;

	private JMenuItem addPresetMenuItem;
	private JMenuItem addAccountMenuItem;
	private JMenuItem addPlaylistMenuItem;

	private final ResourceBundle resourceBundle = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.plugins.coreplugin.resources.menuView"); //NON-NLS

	@Inject
	public MenuViewPanel(final UploadController uploadController, final AccountService accountService, final PresetService presetService, final PlaylistService playlistService)
	{
		this.uploadController = uploadController;
		this.accountService = accountService;
		this.presetService = presetService;
		this.playlistService = playlistService;
		this.initMenuComponents();
	}

	private void initMenuComponents()
	{
		final JMenuItem importAccountMenuItem = new JMenuItem(this.resourceBundle.getString("menu.accounts"));
		importAccountMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				MenuViewPanel.this.uploadController.importAccount();
			}
		});

		final JMenuItem importPresetMenuItem = new JMenuItem(this.resourceBundle.getString("menu.presets"));
		importPresetMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				MenuViewPanel.this.uploadController.importPreset();
			}
		});
		final JMenuItem importQueueMenuItem = new JMenuItem(this.resourceBundle.getString("menu.queue"));
		importQueueMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				MenuViewPanel.this.uploadController.importQueue();
			}
		});

		final JMenuItem exportAccountMenuItem = new JMenuItem(this.resourceBundle.getString("menu.accounts"));
		exportAccountMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				MenuViewPanel.this.uploadController.exportAccount();
			}
		});
		final JMenuItem exportPresetMenuItem = new JMenuItem(this.resourceBundle.getString("menu.presets"));
		exportPresetMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				MenuViewPanel.this.uploadController.exportPreset();
			}
		});
		final JMenuItem exportQueueMenuItem = new JMenuItem(this.resourceBundle.getString("menu.queue"));
		exportQueueMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				MenuViewPanel.this.uploadController.exportQueue();
			}
		});

		this.addAccountMenuItem = new JMenuItem("Account hinzufügen", new ImageIcon(this.getClass().getResource("/youtubeuploader/resources/images/key_add.png"))); //NON-NLS
		this.addAccountMenuItem.addActionListener(new ActionListener()
		{
			@SuppressWarnings("CallToStringEquals") @Override
			public void actionPerformed(final ActionEvent e)
			{
				final JTextField nameTextField = new JTextField("");
				final JPasswordField passwordField = new JPasswordField("");

				final Object[] message = new Object[]{MenuViewPanel.this.resourceBundle.getString("accountDialog.accountLabel"), nameTextField, MenuViewPanel.this.resourceBundle.getString(
						"accountDialog.passwordLabel"), passwordField};

				while (true) {
					final int result = JOptionPane.showConfirmDialog(null, message, MenuViewPanel.this.resourceBundle.getString("accountDialog.addAccountLabel"), JOptionPane.OK_CANCEL_OPTION);
					if (result == JOptionPane.OK_OPTION) {
						if (!nameTextField.getText().equals("")) {

							final Account account = new Account();
							account.name = nameTextField.getText();
							account.password = String.valueOf(passwordField.getPassword());
							//try {

							MenuViewPanel.this.accountService.createAccountEntry(account);
							break;
							/*} catch (AuthenticationException ex) {
								nameTextField.setBackground(Color.RED);
								passwordField.setBackground(Color.RED);
							} */
						} else {
							nameTextField.setBackground(Color.RED);
							passwordField.setBackground(Color.RED);
						}
					} else {
						break;
					}
				}
			}
		});

		this.addPlaylistMenuItem = new JMenuItem(this.resourceBundle.getString("playlistDialog.addPlaylistLabel"), new ImageIcon(this.getClass().getResource(
				"/youtubeuploader/resources/images/table_add" + ".png"))); //NON-NLS
		this.addPlaylistMenuItem.addActionListener(new ActionListener()
		{
			@SuppressWarnings("CallToStringEquals") @Override public void actionPerformed(final ActionEvent e)
			{
				final JTextField nameTextField = new JTextField("");
				final JTextArea descriptionTextArea = new JTextArea("");
				descriptionTextArea.setLineWrap(true);

				final JScrollPane scrollPane = new JScrollPane(descriptionTextArea);
				scrollPane.setPreferredSize(new Dimension(350, 150));
				final Object[] message = new Object[]{MenuViewPanel.this.resourceBundle.getString("playlistDialog.playlistLabel"), nameTextField, MenuViewPanel.this.resourceBundle.getString(
						"playlistDialog.descriptionLabel"), scrollPane};

				while (true) {
					final int result = JOptionPane.showConfirmDialog(null, message, MenuViewPanel.this.resourceBundle.getString("playlistDialog.addPlaylistLabel"), JOptionPane.OK_CANCEL_OPTION);
					if (result == JOptionPane.OK_OPTION) {
						if (nameTextField.getText().equals("")) {
							nameTextField.setBackground(Color.RED);
							continue;
						}
						nameTextField.setBackground(Color.WHITE);
						if (descriptionTextArea.getText().equals("")) {
							descriptionTextArea.setBackground(Color.RED);
							continue;
						}
						descriptionTextArea.setBackground(Color.WHITE);

						final Playlist playlist = new Playlist();
						playlist.title = nameTextField.getText();
						playlist.summary = descriptionTextArea.getText();
						playlist.account = MenuViewPanel.this.uploadController.getAccountListModel().getSelectedItem();
						if (playlist.account != null) {
							MenuViewPanel.this.playlistService.createPlaylist(playlist);
							break;
						} else {
							break;
						}
					} else {
						break;
					}
				}
			}
		});

		this.addPresetMenuItem = new JMenuItem(this.resourceBundle.getString("presetDialog.addPresetLabel"), new ImageIcon(this.getClass().getResource(
				"/youtubeuploader/resources/images/report_add" + ".png"))); //NON-NLS
		this.addPresetMenuItem.addActionListener(new ActionListener()
		{
			@SuppressWarnings("CallToStringEquals") @Override public void actionPerformed(final ActionEvent e)
			{
				final JTextField nameTextField = new JTextField("");

				final Object[] message = new Object[]{MenuViewPanel.this.resourceBundle.getString("presetDialog.presetLabel"), nameTextField};

				while (true) {
					final int result = JOptionPane.showConfirmDialog(null, message, MenuViewPanel.this.resourceBundle.getString("presetDialog.addPresetLabel"), JOptionPane.OK_CANCEL_OPTION);
					if (result == JOptionPane.OK_OPTION) {
						if (!nameTextField.getText().equals("")) {
							final Preset preset = new Preset();
							preset.name = nameTextField.getText();
							MenuViewPanel.this.presetService.createPresetEntry(preset);
							break;
						} else {
							nameTextField.setBackground(Color.RED);
						}
					} else {
						break;
					}
				}
			}
		});

		this.importMenu = new JMenu(this.resourceBundle.getString("importMenuItemLabel"));
		this.importMenu.add(importAccountMenuItem);
		this.importMenu.add(importPresetMenuItem);
		this.importMenu.add(importQueueMenuItem);

		this.exportMenu = new JMenu(this.resourceBundle.getString("exportMenuItemLabel"));
		this.exportMenu.add(exportAccountMenuItem);
		this.exportMenu.add(exportPresetMenuItem);
		this.exportMenu.add(exportQueueMenuItem);
	}

	public JMenu[] getFileMenus()
	{
		return new JMenu[]{this.importMenu, this.exportMenu};
	}

	public JMenuItem[] getEditMenuItems()
	{
		return new JMenuItem[]{this.addAccountMenuItem, this.addPresetMenuItem, this.addPlaylistMenuItem};
	}
}
