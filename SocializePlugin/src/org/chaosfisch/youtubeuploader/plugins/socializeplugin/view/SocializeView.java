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
		this.controller = messageController;
		this.settings = settingsService;
		this.injector = injector;
		this.mainFrame = mainFrame;
		this.initComponents();
		this.initListeners();
	}

	private void initComponents()
	{
		this.controller.setup();
		final SpinnerModel spinnerNumberModel = new SpinnerNumberModel(1, 1, 10000, 1);
		this.uploadIDSpinner.setModel(spinnerNumberModel);
		this.uploadIDSpinner.setEditor(new JSpinner.NumberEditor(this.uploadIDSpinner));

		this.messagesTable.setModel(this.controller.getMessageTableModel());
		this.messagesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		this.publishComboBox.setModel(new DefaultComboBoxModel(new String[]{I18nSupport.message("publishlist.uploadid"), I18nSupport.message("publishlist.uploadsfinished"), I18nSupport.message("publishlist.now")}));
	}

	private void initListeners()
	{
		this.publishComboBox.addItemListener(new ItemListener()
		{
			@Override public void itemStateChanged(final ItemEvent e)
			{
				if ((e.getStateChange() == ItemEvent.SELECTED) && (SocializeView.this.publishComboBox.getSelectedIndex() == 0)) {
					SocializeView.this.uploadIDSpinner.setEnabled(true);
				} else {
					SocializeView.this.uploadIDSpinner.setEnabled(false);
				}
			}
		});

		this.addButton.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				SocializeView.this.validationResultModel.setResult(SocializeView.this.validate());
				if (SocializeView.this.validationResultModel.hasErrors()) {
					return;
				}
				SocializeView.this.controller.addMessage(SocializeView.this.publishComboBox.getSelectedIndex(), SocializeView.this.getData());
			}
		});

		this.twitterButton.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				final ISocialProvider socialProvider = SocializeView.this.controller.getMessageService().get(Provider.TWITTER);
				final String settingsString = (String) SocializeView.this.settings.get("socialize.socialize.twitter", " ___ "); //NON-NLS
				if (SocializeView.this.twitterButton.isSelected()) {
					if (settingsString.contains("___")) {
						final String token = settingsString.substring(0, settingsString.indexOf("___"));
						final String secret = settingsString.substring(settingsString.indexOf("___") + 3, settingsString.length());
						socialProvider.setAccessToken(new Token(token, secret));
					}

					if (!socialProvider.hasValidAccessToken()) {
						socialProvider.setAccessToken(null);
						socialProvider.authenticate();
						if (!socialProvider.hasValidAccessToken()) {
							SocializeView.this.twitterButton.setSelected(false);
						} else {
							SocializeView.this.settings.set("socialize.socialize.twitter", String.format("%s___%s", socialProvider.getAccessToken().getToken(), socialProvider.getAccessToken().getSecret())); //NON-NLS
							SocializeView.this.settings.save();
						}
					}
				}
			}
		});
		this.facebookButton.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				final BetterSwingWorker swingWorker = new BetterSwingWorker()
				{
					private DisabledGlassPane glassPane;

					@Override protected void background()
					{
						final ISocialProvider socialProvider = SocializeView.this.controller.getMessageService().get(Provider.FACEBOOK);
						final String settingsString = (String) SocializeView.this.settings.get("socialize.socialize.facebook", " ___ "); //NON-NLS
						if (SocializeView.this.facebookButton.isSelected()) {
							if (settingsString.contains("___")) {
								final String token = settingsString.substring(0, settingsString.indexOf("___"));
								final String secret = settingsString.substring(settingsString.indexOf("___") + 3, settingsString.length());
								socialProvider.setAccessToken(new Token(token, secret));
							}

							if (!socialProvider.hasValidAccessToken()) {

								SwingUtilities.invokeLater(new Runnable()
								{
									@Override public void run()
									{
										//noinspection UnqualifiedFieldAccess
										glassPane = SocializeView.this.injector.getInstance(DisabledGlassPane.class);
										final JRootPane rootPane = SwingUtilities.getRootPane(SocializeView.this.mainFrame);
										//noinspection UnqualifiedFieldAccess
										rootPane.setGlassPane(glassPane);
										//noinspection UnqualifiedFieldAccess
										glassPane.activate(I18nSupport.message("label.facebook.waiting"));
									}
								});

								socialProvider.setAccessToken(null);
								socialProvider.authenticate();
								if (!socialProvider.hasValidAccessToken()) {
									SocializeView.this.facebookButton.setSelected(false);
								} else {
									SocializeView.this.settings.set("socialize.socialize.facebook", String.format("%s___%s", socialProvider.getAccessToken().getToken(), socialProvider.getAccessToken().getSecret())); //NON-NLS
									SocializeView.this.settings.save();
								}
							}
						}
					}

					@Override protected void onDone()
					{
						if (this.glassPane != null) {
							this.glassPane.deactivate();
						}
					}
				};
				swingWorker.execute();
			}
		});

		this.deleteButton.addActionListener(new ActionListener()
		{
			@Override public void actionPerformed(final ActionEvent e)
			{
				SocializeView.this.controller.removeEntryAt(SocializeView.this.messagesTable.getSelectedRow());
			}
		});
	}

	public JPanel getPanel()
	{
		return this.panel;
	}

	//validate each of the three input fields
	private ValidationResult validate()
	{
		final ValidationResult validationResult = new ValidationResult();

		if (!this.googlePlusButton.isSelected() && !this.facebookButton.isSelected() && !this.twitterButton.isSelected() && !this.youtubeButton.isSelected()) {
			validationResult.addError(I18nSupport.message("validation.service"));
		}

		if (!ValidationUtils.hasBoundedLength(this.messageTextArea.getText(), 5, 140)) {
			validationResult.addError(I18nSupport.message("validation.message"));
		}

		if (this.messageTextArea.getText().contains("{video}") && !ValidationUtils.hasBoundedLength(this.messageTextArea.getText(), 5, 120)) { //NON-NLS
			validationResult.addError(I18nSupport.message("validation.message.video"));
		}

		return validationResult;
	}

	private void createUIComponents()
	{
		this.validationResultModel = new DefaultValidationResultModel();
		this.validationComponent = (JPanel) ValidationResultViewFactory.createReportIconAndTextPane(this.validationResultModel);
	}

	public void setData(final Message data)
	{
		this.messageTextArea.setText(data.message);
		this.facebookButton.setSelected(data.facebook);
		this.twitterButton.setSelected(data.twitter);
		this.googlePlusButton.setSelected(data.googleplus);
		this.youtubeButton.setSelected(data.youtube);
		this.publishComboBox.setSelectedIndex((data.uploadid == null) ? 0 : 1);
		this.uploadIDSpinner.setValue(data.uploadid);
	}

	public Message getData()
	{
		final Message data = new Message();
		data.message = this.messageTextArea.getText();
		data.facebook = this.facebookButton.isSelected();
		data.twitter = this.twitterButton.isSelected();
		data.youtube = this.youtubeButton.isSelected();
		data.googleplus = this.googlePlusButton.isSelected();
		data.uploadid = (Integer) this.uploadIDSpinner.getValue();

		return data;
	}
}
