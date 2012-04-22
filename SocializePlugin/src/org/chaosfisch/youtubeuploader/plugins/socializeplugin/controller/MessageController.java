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

package org.chaosfisch.youtubeuploader.plugins.socializeplugin.controller;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.entities.QueueEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.Uploader;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.models.MessageTableModel;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.models.entities.MessageEntry;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.services.MessageService;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.services.providers.FacebookSocialProvider;
import org.chaosfisch.youtubeuploader.plugins.socializeplugin.services.providers.TwitterSocialProvider;
import org.chaosfisch.youtubeuploader.services.settingsservice.spi.SettingsService;
import org.scribe.model.Token;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 14.04.12
 * Time: 21:36
 * To change this template use File | Settings | File Templates.
 */
public class MessageController
{

	@Inject private MessageTableModel messageTableModel;
	@Inject private MessageService    messageService;
	@Inject private Injector          injector;
	@Inject private SettingsService   settingsService;

	public MessageController()
	{
		AnnotationProcessor.process(this);
	}

	public void setup()
	{
		this.messageTableModel.addMessageEntryList(this.messageService.getMessageEntries());
	}

	public void addMessage(final String messageTextAreaText, final int publishComboBoxSelectedItem, final int uploadIDSpinnerValue, final boolean facebookButtonSellected,
	                       final boolean twitterButtonSelected, final boolean googlePlusButtonSelected, final boolean youtubeButtonSelected)
	{
		switch (publishComboBoxSelectedItem) {
			case 0:
				final MessageEntry messageEntry_1 = new MessageEntry();
				messageEntry_1.setMessage(messageTextAreaText);
				messageEntry_1.setFacebook(facebookButtonSellected);
				messageEntry_1.setTwitter(twitterButtonSelected);
				messageEntry_1.setGooglePlus(googlePlusButtonSelected);
				messageEntry_1.setYoutube(youtubeButtonSelected);
				messageEntry_1.setUploadID(uploadIDSpinnerValue);
				this.messageService.createMessageEntry(messageEntry_1);
				break;
			case 1:
				final MessageEntry messageEntry_2 = new MessageEntry();
				messageEntry_2.setMessage(messageTextAreaText);
				messageEntry_2.setFacebook(facebookButtonSellected);
				messageEntry_2.setTwitter(twitterButtonSelected);
				messageEntry_2.setGooglePlus(googlePlusButtonSelected);
				messageEntry_2.setYoutube(youtubeButtonSelected);
				this.messageService.createMessageEntry(messageEntry_2);
				break;
			case 2:
				this.publish(messageTextAreaText, facebookButtonSellected, twitterButtonSelected, googlePlusButtonSelected, youtubeButtonSelected);
				break;
		}
	}

	private void publish(final String message, final boolean facebook, final boolean twitter, final boolean googlePlus, final boolean youtube)
	{
		if (facebook) {
			final String settingsString = (String) this.settingsService.get("socialize.socialize.facebook", ""); //NON-NLS
			if (settingsString.contains("___")) {
				final String token = settingsString.substring(0, settingsString.indexOf("___"));
				final String secret = settingsString.substring(settingsString.indexOf("___") + 3, settingsString.length());
				final FacebookSocialProvider facebookSocialProvider = (FacebookSocialProvider) this.messageService.get(MessageService.Provider.FACEBOOK);
				facebookSocialProvider.setAccessToken(new Token(token, secret));
				facebookSocialProvider.publish(message);
			}
		}
		if (twitter) {
			final String settingsString = (String) this.settingsService.get("socialize.socialize.twitter", ""); //NON-NLS
			if (settingsString.contains("___")) {
				final String token = settingsString.substring(0, settingsString.indexOf("___"));
				final String secret = settingsString.substring(settingsString.indexOf("___") + 3, settingsString.length());
				final TwitterSocialProvider twitterSocialProvider = (TwitterSocialProvider) this.messageService.get(MessageService.Provider.TWITTER);
				twitterSocialProvider.setAccessToken(new Token(token, secret));
				twitterSocialProvider.publish(message);
			}
		}
	}

	public MessageTableModel getMessageTableModel()
	{
		return this.messageTableModel;
	}

	public MessageService getMessageService()
	{
		return this.messageService;
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_JOB_FINISHED)
	public void onUploadJobFinished(final String topic, final QueueEntry queueEntry)
	{
		for (final MessageEntry messageEntry : this.messageService.getMessageEntriesByQueueID(queueEntry.getIdentity())) {
			this.publish(messageEntry.getMessage().replace("{video}", "http://youtu.be/" + queueEntry.getVideoId()), messageEntry.isFacebook(), messageEntry.isTwitter(), messageEntry.isGooglePlus(),
			             messageEntry.isYoutube());
		}
		this.messageService.clearWithQueueID(queueEntry.getIdentity());
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_FINISHED)
	public void onUploadsFinished(final String topic, final Object o)
	{
		for (final MessageEntry messageEntry : this.messageService.getMessageEntriesWithoutQueueID()) {
			this.publish(messageEntry.getMessage(), messageEntry.isFacebook(), messageEntry.isTwitter(), messageEntry.isGooglePlus(), messageEntry.isYoutube());
		}

		this.messageService.clearWithoutQueueID();
	}

	public void removeEntryAt(final int selectedRow)
	{
		if (this.messageTableModel.hasMessageEntryAt(selectedRow)) {
			final MessageEntry messageEntry = this.messageTableModel.getMessageEntryAt(selectedRow);
			this.messageService.removeMessageEntry(messageEntry);
		}
	}
}
