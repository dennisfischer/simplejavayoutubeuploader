/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;

import org.chaosfisch.youtubeuploader.I18nHelper;
import org.chaosfisch.youtubeuploader.controller.MessageController;
import org.chaosfisch.youtubeuploader.dao.spi.SettingsDao;
import org.chaosfisch.youtubeuploader.models.Message;
import org.chaosfisch.youtubeuploader.models.Queue;
import org.chaosfisch.youtubeuploader.services.socialize.FacebookSocialProvider;
import org.chaosfisch.youtubeuploader.services.socialize.TwitterSocialProvider;
import org.scribe.model.Token;

import com.google.inject.Inject;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.ValidationResultModel;
import com.jgoodies.validation.util.ValidationUtils;

public class SocializeView
{
	private JButton					addButton;
	private final MessageController	controller;
	private JButton					deleteButton;
	private JToggleButton			facebookButton;
	private JToggleButton			googlePlusButton;
	private JTable					messagesTable;
	private JTextArea				messageTextArea;
	private JComboBox<String>		publishComboBox;
	private final SettingsDao		settingsDao;
	private JToggleButton			twitterButton;
	private JComboBox<String>		uploadsCombobox;
	private ValidationResultModel	validationResultModel;

	private JToggleButton			youtubeButton;

	@Inject
	public SocializeView(final MessageController messageController, final SettingsDao settingsDao)
	{
		controller = messageController;
		this.settingsDao = settingsDao;
		initComponents();
		initListeners();
	}

	public Message getData()
	{
		final Message data = new Message();
		data.message = messageTextArea.getText();
		data.facebook = facebookButton.isSelected();
		data.twitter = twitterButton.isSelected();
		data.youtube = youtubeButton.isSelected();
		data.googleplus = googlePlusButton.isSelected();
		data.uploadid = ((Queue) uploadsCombobox.getSelectedItem()).identity;

		return data;
	}

	private void initComponents()
	{
		controller.setup();
		messagesTable.setModel(controller.getMessageTableModel());
		messagesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		uploadsCombobox.setModel(controller.getUploadsModel());

		publishComboBox.setModel(new DefaultComboBoxModel<String>(new String[] { I18nHelper.message("publishlist.uploadid"),
				I18nHelper.message("publishlist.uploadsfinished"), I18nHelper.message("publishlist.now") }));
	}

	private void initListeners()
	{
		publishComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e)
			{
				if ((e.getStateChange() == ItemEvent.SELECTED) && (publishComboBox.getSelectedIndex() == 0))
				{
					uploadsCombobox.setEnabled(true);
				} else
				{
					uploadsCombobox.setEnabled(false);
				}
			}
		});

		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				validationResultModel.setResult(validate());
				if (validationResultModel.hasErrors()) { return; }
				controller.addMessage(publishComboBox.getSelectedIndex(), getData());
			}
		});

		twitterButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final TwitterSocialProvider socialProvider = new TwitterSocialProvider();
				final String settingsString = settingsDao.get("socialize.socialize.twitter"); // NON-NLS
				if (twitterButton.isSelected())
				{
					if (settingsString.contains("___"))
					{
						final String token = new String(settingsString.substring(0, settingsString.indexOf("___")));
						final String secret = new String(settingsString.substring(settingsString.indexOf("___") + 3, settingsString.length()));
						socialProvider.setAccessToken(new Token(token, secret));
					}

					if (!socialProvider.hasValidAccessToken())
					{
						socialProvider.setAccessToken(null);
						socialProvider.authenticate();
						if (!socialProvider.hasValidAccessToken())
						{
							twitterButton.setSelected(false);
						} else
						{
							settingsDao.set("socialize.socialize.twitter",
									String.format("%s___%s", socialProvider.getAccessToken().getToken(), socialProvider.getAccessToken().getSecret())); // NON-NLS
							settingsDao.save();
						}
					}
				}
			}
		});
		facebookButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {

					@Override
					protected Void doInBackground() throws Exception
					{
						final FacebookSocialProvider socialProvider = new FacebookSocialProvider();
						final String settingsString = settingsDao.get("socialize.socialize.facebook"); // NON-NLS
						if (facebookButton.isSelected())
						{
							if (settingsString.contains("___"))
							{
								final String token = new String(settingsString.substring(0, settingsString.indexOf("___")));
								final String secret = new String(settingsString.substring(settingsString.indexOf("___") + 3, settingsString.length()));
								socialProvider.setAccessToken(new Token(token, secret));
							}

							if (!socialProvider.hasValidAccessToken())
							{

								socialProvider.setAccessToken(null);
								socialProvider.authenticate();
								if (!socialProvider.hasValidAccessToken())
								{
									facebookButton.setSelected(false);
								} else
								{
									settingsDao.set("socialize.socialize.facebook", String.format("%s___%s", socialProvider.getAccessToken()
											.getToken(), socialProvider.getAccessToken().getSecret())); // NON-NLS
									settingsDao.save();
								}
							}
						}

						return null;
					}

				};
				swingWorker.execute();
			}
		});

		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				controller.removeEntryAt(messagesTable.getSelectedRow());
			}
		});
	}

	public void setData(final Message data)
	{
		messageTextArea.setText(data.message);
		facebookButton.setSelected(data.facebook);
		twitterButton.setSelected(data.twitter);
		googlePlusButton.setSelected(data.googleplus);
		youtubeButton.setSelected(data.youtube);
		publishComboBox.setSelectedIndex((data.uploadid == null) ? 0 : 1);
	}

	// validate each of the three input fields
	private ValidationResult validate()
	{
		final ValidationResult validationResult = new ValidationResult();

		if (!googlePlusButton.isSelected() && !facebookButton.isSelected() && !twitterButton.isSelected() && !youtubeButton.isSelected())
		{
			validationResult.addError(I18nHelper.message("validation.service"));
		}

		if (!ValidationUtils.hasBoundedLength(messageTextArea.getText(), 5, 140))
		{
			validationResult.addError(I18nHelper.message("validation.message"));
		}

		if (messageTextArea.getText().contains("{video}") && !ValidationUtils.hasBoundedLength(messageTextArea.getText(), 5, 120))
		{ // NON-NLS
			validationResult.addError(I18nHelper.message("validation.message.video"));
		}

		return validationResult;
	}
}
