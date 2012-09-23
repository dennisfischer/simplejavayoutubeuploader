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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.chaosfisch.google.auth.AuthenticationException;
import org.chaosfisch.google.auth.GoogleAuthorization;
import org.chaosfisch.youtubeuploader.I18nHelper;
import org.chaosfisch.youtubeuploader.controller.UploadController;
import org.chaosfisch.youtubeuploader.dao.spi.AccountDao;
import org.chaosfisch.youtubeuploader.dao.spi.PlaylistDao;
import org.chaosfisch.youtubeuploader.dao.spi.PresetDao;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Playlist;
import org.chaosfisch.youtubeuploader.models.Preset;

import com.google.inject.Inject;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.ValidationResultModel;
import com.jgoodies.validation.util.DefaultValidationResultModel;
import com.jgoodies.validation.view.ValidationResultViewFactory;

public class MenuViewPanel
{
	private final UploadController	uploadController;
	private final AccountDao		accountService;
	private final PresetDao			presetService;
	private final PlaylistDao		playlistService;

	private JMenu					importMenu;
	private JMenu					exportMenu;

	private JMenuItem				addPresetMenuItem;
	private JMenuItem				addAccountMenuItem;
	private JMenuItem				addPlaylistMenuItem;

	@Inject
	public MenuViewPanel(final UploadController uploadController, final AccountDao accountService, final PresetDao presetService,
			final PlaylistDao playlistService)
	{
		this.uploadController = uploadController;
		this.accountService = accountService;
		this.presetService = presetService;
		this.playlistService = playlistService;
		initMenuComponents();
	}

	private void initMenuComponents()
	{
		final JMenuItem importAccountMenuItem = new JMenuItem(I18nHelper.message("menu.accounts"));
		importAccountMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				uploadController.importAccount();
			}
		});

		final JMenuItem importPresetMenuItem = new JMenuItem(I18nHelper.message("menu.presets"));
		importPresetMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				uploadController.importPreset();
			}
		});
		final JMenuItem importQueueMenuItem = new JMenuItem(I18nHelper.message("menu.queue"));
		importQueueMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				uploadController.importQueue();
			}
		});

		final JMenuItem exportAccountMenuItem = new JMenuItem(I18nHelper.message("menu.accounts"));
		exportAccountMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				uploadController.exportAccount();
			}
		});
		final JMenuItem exportPresetMenuItem = new JMenuItem(I18nHelper.message("menu.presets"));
		exportPresetMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				uploadController.exportPreset();
			}
		});
		final JMenuItem exportQueueMenuItem = new JMenuItem(I18nHelper.message("menu.queue"));
		exportQueueMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				uploadController.exportQueue();
			}
		});

		addAccountMenuItem = new JMenuItem(I18nHelper.message("accountDialog.addAccountMenuLabel"), new ImageIcon(getClass().getResource(
				"/youtubeuploader/resources/images/key_add.png"))); // NON-NLS
		addAccountMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final JTextField nameTextField = new JTextField("");
				final JPasswordField passwordField = new JPasswordField("");

				final Object[] message = { I18nHelper.message("accountDialog.accountLabel"), nameTextField,
						I18nHelper.message("accountDialog.passwordLabel"), passwordField };

				while (true)
				{
					final int result = JOptionPane.showConfirmDialog(null, message, I18nHelper.message("accountDialog.addAccountLabel"),
							JOptionPane.OK_CANCEL_OPTION);
					if (result == JOptionPane.OK_OPTION)
					{
						if (!nameTextField.getText().equals(""))
						{

							final Account account = new Account();
							account.name = nameTextField.getText().trim();
							account.setPassword(String.valueOf(passwordField.getPassword()).trim());
							try
							{
								final GoogleAuthorization googleAuthorization = new GoogleAuthorization(GoogleAuthorization.TYPE.CLIENTLOGIN,
										account.name, account.getPassword());
								System.out.println(googleAuthorization.getAuthHeader());
								accountService.create(account);
								break;
							} catch (AuthenticationException ignored)
							{
								nameTextField.setBackground(Color.RED);
								passwordField.setBackground(Color.RED);
							}
						} else
						{
							nameTextField.setBackground(Color.RED);
							passwordField.setBackground(Color.RED);
						}
					} else
					{
						break;
					}
				}
			}
		});

		addPlaylistMenuItem = new JMenuItem(I18nHelper.message("playlistDialog.addPlaylistLabel"), new ImageIcon(getClass().getResource(
				String.format("/youtubeuploader/resources/images/table_add.png")))); // NON-NLS
		addPlaylistMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
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

				final Object[] message = { I18nHelper.message("playlistDialog.playlistLabel"), nameTextField,
						I18nHelper.message("playlistDialog.descriptionLabel"), scrollPane, "Account", accountList, validationPanel };

				while (true)
				{
					final int result = JOptionPane.showConfirmDialog(null, message, I18nHelper.message("playlistDialog.addPlaylistLabel"),
							JOptionPane.OK_CANCEL_OPTION);
					if (result == JOptionPane.OK_OPTION)
					{

						final ValidationResult validationResult = new ValidationResult();
						if (nameTextField.getText().isEmpty())
						{
							validationResult.addError(I18nHelper.message("validation.playlistname"));
						} else if (descriptionTextArea.getText().isEmpty())
						{
							validationResult.addError(I18nHelper.message("validation.playlistdescription"));
						} else if (accountList.getSelectedItem() == null)
						{
							validationResult.addError(I18nHelper.message("validation.playlistaccount"));
						} else
						{
							final Playlist playlist = new Playlist();
							playlist.title = nameTextField.getText();
							playlist.summary = descriptionTextArea.getText();
							playlist.account = (Account) accountList.getSelectedItem();
							if (playlist.account != null)
							{
								playlistService.addYoutubePlaylist(playlist);
								break;
							} else
							{
								break;
							}
						}
						validationResultModel.setResult(validationResult);
					} else
					{
						break;
					}
				}
			}
		});

		addPresetMenuItem = new JMenuItem(I18nHelper.message("presetDialog.addPresetLabel"), new ImageIcon(getClass().getResource(
				String.format("/youtubeuploader/resources/images/report_add.png")))); // NON-NLS
		addPresetMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final JTextField nameTextField = new JTextField("");

				final ValidationResultModel validationResultModel = new DefaultValidationResultModel();
				final JPanel validationPanel = (JPanel) ValidationResultViewFactory.createReportIconAndTextPane(validationResultModel);
				final Object[] message = { I18nHelper.message("presetDialog.presetLabel"), nameTextField, validationPanel };

				while (true)
				{
					final int result = JOptionPane.showConfirmDialog(null, message, I18nHelper.message("presetDialog.addPresetLabel"),
							JOptionPane.OK_CANCEL_OPTION);
					if (result == JOptionPane.OK_OPTION)
					{
						if (nameTextField.getText().isEmpty())
						{
							final ValidationResult validationResult = new ValidationResult();
							validationResult.addError(I18nHelper.message("validation.presetname"));
							validationResultModel.setResult(validationResult);
						} else
						{
							final Preset preset = new Preset();
							preset.name = nameTextField.getText();
							presetService.create(preset);
							break;
						}
					} else
					{
						break;
					}
				}
			}
		});

		importMenu = new JMenu(I18nHelper.message("importMenuItemLabel"));
		importMenu.add(importAccountMenuItem);
		importMenu.add(importPresetMenuItem);
		importMenu.add(importQueueMenuItem);

		exportMenu = new JMenu(I18nHelper.message("exportMenuItemLabel"));
		exportMenu.add(exportAccountMenuItem);
		exportMenu.add(exportPresetMenuItem);
		exportMenu.add(exportQueueMenuItem);
	}
}
