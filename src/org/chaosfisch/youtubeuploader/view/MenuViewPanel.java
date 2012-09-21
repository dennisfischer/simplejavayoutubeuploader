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

package org.chaosfisch.youtubeuploader.view;

import com.google.inject.Inject;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.ValidationResultModel;
import com.jgoodies.validation.util.DefaultValidationResultModel;
import com.jgoodies.validation.view.ValidationResultViewFactory;
import org.chaosfisch.google.auth.AuthenticationException;
import org.chaosfisch.google.auth.GoogleAuthorization;
import org.chaosfisch.youtubeuploader.controller.UploadController;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.models.Preset;
import org.chaosfisch.youtubeuploader.services.spi.AccountService;
import org.chaosfisch.youtubeuploader.services.spi.PlaylistService;
import org.chaosfisch.youtubeuploader.services.spi.PresetService;

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

	private final ResourceBundle resourceBundle = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.plugins.coreplugin.resources.coreplugin"); //NON-NLS

	@Inject
	public MenuViewPanel(final UploadController uploadController, final AccountService accountService, final PresetService presetService, final PlaylistService playlistService)
	{
		this.uploadController = uploadController;
		this.accountService = accountService;
		this.presetService = presetService;
		this.playlistService = playlistService;
		initMenuComponents();
	}

	private void initMenuComponents()
	{
		final JMenuItem importAccountMenuItem = new JMenuItem(resourceBundle.getString("menu.accounts"));
		importAccountMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				uploadController.importAccount();
			}
		});

		final JMenuItem importPresetMenuItem = new JMenuItem(resourceBundle.getString("menu.presets"));
		importPresetMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				uploadController.importPreset();
			}
		});
		final JMenuItem importQueueMenuItem = new JMenuItem(resourceBundle.getString("menu.queue"));
		importQueueMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				uploadController.importQueue();
			}
		});

		final JMenuItem exportAccountMenuItem = new JMenuItem(resourceBundle.getString("menu.accounts"));
		exportAccountMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				uploadController.exportAccount();
			}
		});
		final JMenuItem exportPresetMenuItem = new JMenuItem(resourceBundle.getString("menu.presets"));
		exportPresetMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				uploadController.exportPreset();
			}
		});
		final JMenuItem exportQueueMenuItem = new JMenuItem(resourceBundle.getString("menu.queue"));
		exportQueueMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				uploadController.exportQueue();
			}
		});

		addAccountMenuItem = new JMenuItem(resourceBundle.getString("accountDialog.addAccountMenuLabel"), new ImageIcon(getClass().getResource("/youtubeuploader/resources/images/key_add.png"))); //NON-NLS
		addAccountMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final JTextField nameTextField = new JTextField("");
				final JPasswordField passwordField = new JPasswordField("");

				final Object[] message = {resourceBundle.getString("accountDialog.accountLabel"), nameTextField, resourceBundle.getString("accountDialog.passwordLabel"), passwordField};

				while (true) {
					final int result = JOptionPane.showConfirmDialog(null, message, resourceBundle.getString("accountDialog.addAccountLabel"), JOptionPane.OK_CANCEL_OPTION);
					if (result == JOptionPane.OK_OPTION) {
						if (!nameTextField.getText().equals("")) {

							final Account account = new Account();
							account.name = nameTextField.getText().trim();
							account.setPassword(String.valueOf(passwordField.getPassword()).trim());
							try {
								final GoogleAuthorization googleAuthorization = new GoogleAuthorization(GoogleAuthorization.TYPE.CLIENTLOGIN, account.name, account.getPassword());
								System.out.println(googleAuthorization.getAuthHeader());
								accountService.create(account);
								break;
							} catch (AuthenticationException ignored) {
								nameTextField.setBackground(Color.RED);
								passwordField.setBackground(Color.RED);
							}
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

		addPlaylistMenuItem = new JMenuItem(resourceBundle.getString("playlistDialog.addPlaylistLabel"), new ImageIcon(getClass().getResource(String.format("/youtubeuploader/resources/images/table_add.png")))); //NON-NLS
		addPlaylistMenuItem.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				final JTextField nameTextField = new JTextField("");
				final JTextArea descriptionTextArea = new JTextArea("");
				descriptionTextArea.setLineWrap(true);

				final JScrollPane scrollPane = new JScrollPane(descriptionTextArea);
				scrollPane.setPreferredSize(new Dimension(350, 150));

				final ValidationResultModel validationResultModel = new DefaultValidationResultModel();
				final JPanel validationPanel = (JPanel) ValidationResultViewFactory.createReportIconAndTextPane(validationResultModel);

				final Account[] accounts = new Account[accountService.getAll().size()];
				accountService.getAll().toArray(accounts);
				final JComboBox accountList = new JComboBox(accounts);

				final Object[] message = {resourceBundle.getString("playlistDialog.playlistLabel"), nameTextField, resourceBundle.getString("playlistDialog.descriptionLabel"), scrollPane, "Account", accountList, validationPanel};

				while (true) {
					final int result = JOptionPane.showConfirmDialog(null, message, resourceBundle.getString("playlistDialog.addPlaylistLabel"), JOptionPane.OK_CANCEL_OPTION);
					if (result == JOptionPane.OK_OPTION) {

						final ValidationResult validationResult = new ValidationResult();
						if (nameTextField.getText().isEmpty()) {
							validationResult.addError(resourceBundle.getString("validation.playlistname"));
						} else if (descriptionTextArea.getText().isEmpty()) {
							validationResult.addError(resourceBundle.getString("validation.playlistdescription"));
						} else if (accountList.getSelectedItem() == null) {
							validationResult.addError(resourceBundle.getString("validation.playlistaccount"));
						} else {
							final Playlist playlist = new Playlist();
							playlist.title = nameTextField.getText();
							playlist.summary = descriptionTextArea.getText();
							playlist.account = (Account) accountList.getSelectedItem();
							if (playlist.account != null) {
								playlistService.addYoutubePlaylist(playlist);
								break;
							} else {
								break;
							}
						}
						validationResultModel.setResult(validationResult);
					} else {
						break;
					}
				}
			}
		});

		addPresetMenuItem = new JMenuItem(resourceBundle.getString("presetDialog.addPresetLabel"), new ImageIcon(getClass().getResource(String.format("/youtubeuploader/resources/images/report_add.png")))); //NON-NLS
		addPresetMenuItem.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				final JTextField nameTextField = new JTextField("");

				final ValidationResultModel validationResultModel = new DefaultValidationResultModel();
				final JPanel validationPanel = (JPanel) ValidationResultViewFactory.createReportIconAndTextPane(validationResultModel);
				final Object[] message = {resourceBundle.getString("presetDialog.presetLabel"), nameTextField, validationPanel};

				while (true) {
					final int result = JOptionPane.showConfirmDialog(null, message, resourceBundle.getString("presetDialog.addPresetLabel"), JOptionPane.OK_CANCEL_OPTION);
					if (result == JOptionPane.OK_OPTION) {
						if (nameTextField.getText().isEmpty()) {
							final ValidationResult validationResult = new ValidationResult();
							validationResult.addError(resourceBundle.getString("validation.presetname"));
							validationResultModel.setResult(validationResult);
						} else {
							final Preset preset = new Preset();
							preset.name = nameTextField.getText();
							presetService.create(preset);
							break;
						}
					} else {
						break;
					}
				}
			}
		});

		importMenu = new JMenu(resourceBundle.getString("importMenuItemLabel"));
		importMenu.add(importAccountMenuItem);
		importMenu.add(importPresetMenuItem);
		importMenu.add(importQueueMenuItem);

		exportMenu = new JMenu(resourceBundle.getString("exportMenuItemLabel"));
		exportMenu.add(exportAccountMenuItem);
		exportMenu.add(exportPresetMenuItem);
		exportMenu.add(exportQueueMenuItem);
	}

	public JMenu[] getFileMenus()
	{
		return new JMenu[]{importMenu, exportMenu};
	}

	public JMenuItem[] getEditMenuItems()
	{
		return new JMenuItem[]{addAccountMenuItem, addPresetMenuItem, addPlaylistMenuItem};
	}
}
