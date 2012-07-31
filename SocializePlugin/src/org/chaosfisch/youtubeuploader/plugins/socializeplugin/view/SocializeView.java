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

package org.chaosfisch.youtubeuploader.plugins.socializeplugin.view;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.ValidationResultModel;
import com.jgoodies.validation.util.DefaultValidationResultModel;
import com.jgoodies.validation.util.ValidationUtils;
import com.jgoodies.validation.view.ValidationResultViewFactory;
import org.chaosfisch.util.BetterSwingWorker;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.DisabledGlassPane;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.I18nSupport;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.controller.MessageController;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.models.Message;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.services.Provider;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.services.providers.ISocialProvider;
import org.chaosfisch.youtubeuploader.services.settingsservice.spi.SettingsService;
import org.scribe.model.Token;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 13.04.12
 * Time: 19:57
 * To change this template use File | Settings | File Templates.
 */
public class SocializeView implements SocializeViewBinding
{
	private       JPanel            panel;
	private       JToggleButton     googlePlusButton;
	private       JToggleButton     facebookButton;
	private       JToggleButton     twitterButton;
	private       JToggleButton     youtubeButton;
	private       JTextArea         messageTextArea;
	private       JComboBox         publishComboBox;
	private       JButton           addButton;
	private       JSpinner          uploadIDSpinner;
	private       JButton           deleteButton;
	private       JTable            messagesTable;
	private       JPanel            validationComponent;
	private final MessageController controller;
	private final SettingsService   settings;
	private final Injector          injector;
	private final JFrame            mainFrame;

	private ValidationResultModel validationResultModel;

	@Inject
	public SocializeView(final MessageController messageController, final SettingsService settingsService, final Injector injector, @Named("mainFrame") final JFrame mainFrame)
	{
		controller = messageController;
		settings = settingsService;
		this.injector = injector;
		this.mainFrame = mainFrame;
		initComponents();
		initListeners();
	}

	private void initComponents()
	{
		controller.setup();
		final SpinnerModel spinnerNumberModel = new SpinnerNumberModel(1, 1, 10000, 1);
		uploadIDSpinner.setModel(spinnerNumberModel);
		uploadIDSpinner.setEditor(new JSpinner.NumberEditor(uploadIDSpinner));

		messagesTable.setModel(controller.getMessageTableModel());
		messagesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		publishComboBox.setModel(new DefaultComboBoxModel(new String[]{I18nSupport.message("publishlist.uploadid"), I18nSupport.message("publishlist.uploadsfinished"), I18nSupport.message("publishlist.now")}));
	}

	private void initListeners()
	{
		publishComboBox.addItemListener(new ItemListener()
		{
			@Override public void itemStateChanged(final ItemEvent e)
			{
				if ((e.getStateChange() == ItemEvent.SELECTED) && (publishComboBox.getSelectedIndex() == 0)) {
					uploadIDSpinner.setEnabled(true);
				} else {
					uploadIDSpinner.setEnabled(false);
				}
			}
		});

		addButton.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				validationResultModel.setResult(validate());
				if (validationResultModel.hasErrors()) {
					return;
				}
				controller.addMessage(publishComboBox.getSelectedIndex(), getData());
			}
		});

		twitterButton.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				final ISocialProvider socialProvider = controller.getMessageService().get(Provider.TWITTER);
				final String settingsString = (String) settings.get("socialize.socialize.twitter", " ___ "); //NON-NLS
				if (twitterButton.isSelected()) {
					if (settingsString.contains("___")) {
						final String token = new String(settingsString.substring(0, settingsString.indexOf("___")));
						final String secret = new String(settingsString.substring(settingsString.indexOf("___") + 3, settingsString.length()));
						socialProvider.setAccessToken(new Token(token, secret));
					}

					if (!socialProvider.hasValidAccessToken()) {
						socialProvider.setAccessToken(null);
						socialProvider.authenticate();
						if (!socialProvider.hasValidAccessToken()) {
							twitterButton.setSelected(false);
						} else {
							settings.set("socialize.socialize.twitter", String.format("%s___%s", socialProvider.getAccessToken().getToken(), socialProvider.getAccessToken().getSecret())); //NON-NLS
							settings.save();
						}
					}
				}
			}
		});
		facebookButton.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				final BetterSwingWorker swingWorker = new BetterSwingWorker()
				{
					private DisabledGlassPane glassPane;

					@Override protected void background()
					{
						final ISocialProvider socialProvider = controller.getMessageService().get(Provider.FACEBOOK);
						final String settingsString = (String) settings.get("socialize.socialize.facebook", " ___ "); //NON-NLS
						if (facebookButton.isSelected()) {
							if (settingsString.contains("___")) {
								final String token = new String(settingsString.substring(0, settingsString.indexOf("___")));
								final String secret = new String(settingsString.substring(settingsString.indexOf("___") + 3, settingsString.length()));
								socialProvider.setAccessToken(new Token(token, secret));
							}

							if (!socialProvider.hasValidAccessToken()) {

								SwingUtilities.invokeLater(new Runnable()
								{
									@Override public void run()
									{
										//noinspection UnqualifiedFieldAccess
										glassPane = injector.getInstance(DisabledGlassPane.class);
										final JRootPane rootPane = SwingUtilities.getRootPane(mainFrame);
										//noinspection UnqualifiedFieldAccess
										rootPane.setGlassPane(glassPane);
										//noinspection UnqualifiedFieldAccess
										glassPane.activate(I18nSupport.message("label.facebook.waiting"));
									}
								});

								socialProvider.setAccessToken(null);
								socialProvider.authenticate();
								if (!socialProvider.hasValidAccessToken()) {
									facebookButton.setSelected(false);
								} else {
									settings.set("socialize.socialize.facebook", String.format("%s___%s", socialProvider.getAccessToken().getToken(), socialProvider.getAccessToken().getSecret())); //NON-NLS
									settings.save();
								}
							}
						}
					}

					@Override protected void onDone()
					{
						if (glassPane != null) {
							glassPane.deactivate();
						}
					}
				};
				swingWorker.execute();
			}
		});

		deleteButton.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				controller.removeEntryAt(messagesTable.getSelectedRow());
			}
		});
	}

	public JPanel getPanel()
	{
		return panel;
	}

	//validate each of the three input fields
	private ValidationResult validate()
	{
		final ValidationResult validationResult = new ValidationResult();

		if (!googlePlusButton.isSelected() && !facebookButton.isSelected() && !twitterButton.isSelected() && !youtubeButton.isSelected()) {
			validationResult.addError(I18nSupport.message("validation.service"));
		}

		if (!ValidationUtils.hasBoundedLength(messageTextArea.getText(), 5, 140)) {
			validationResult.addError(I18nSupport.message("validation.message"));
		}

		if (messageTextArea.getText().contains("{video}") && !ValidationUtils.hasBoundedLength(messageTextArea.getText(), 5, 120)) { //NON-NLS
			validationResult.addError(I18nSupport.message("validation.message.video"));
		}

		return validationResult;
	}

	private void createUIComponents()
	{
		validationResultModel = new DefaultValidationResultModel();
		validationComponent = (JPanel) ValidationResultViewFactory.createReportIconAndTextPane(validationResultModel);
	}

	public void setData(final Message data)
	{
		messageTextArea.setText(data.message);
		facebookButton.setSelected(data.facebook);
		twitterButton.setSelected(data.twitter);
		googlePlusButton.setSelected(data.googleplus);
		youtubeButton.setSelected(data.youtube);
		publishComboBox.setSelectedIndex((data.uploadid == null) ? 0 : 1);
		uploadIDSpinner.setValue(data.uploadid);
	}

	public Message getData()
	{
		final Message data = new Message();
		data.message = messageTextArea.getText();
		data.facebook = facebookButton.isSelected();
		data.twitter = twitterButton.isSelected();
		data.youtube = youtubeButton.isSelected();
		data.googleplus = googlePlusButton.isSelected();
		data.uploadid = (Integer) uploadIDSpinner.getValue();

		return data;
	}
}
