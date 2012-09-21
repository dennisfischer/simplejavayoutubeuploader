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

package org.chaosfisch.youtubeuploader.controller;

import com.google.inject.Injector;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.youtubeuploader.models.GenericListModel;
import org.chaosfisch.youtubeuploader.models.Message;
import org.chaosfisch.youtubeuploader.models.MessageTableModel;
import org.chaosfisch.youtubeuploader.models.Queue;
import org.chaosfisch.youtubeuploader.services.socialize.MessageService;
import org.chaosfisch.youtubeuploader.services.socialize.Provider;
import org.chaosfisch.youtubeuploader.services.socialize.providers.ISocialProvider;
import org.chaosfisch.youtubeuploader.services.spi.QueueService;
import org.chaosfisch.youtubeuploader.services.spi.SettingsService;
import org.chaosfisch.youtubeuploader.services.uploader.Uploader;
import org.scribe.model.Token;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.table.TableModel;

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
	@Inject private QueueService      queueService;
	private final GenericListModel<Queue> uploadListModel = new GenericListModel<Queue>()
	{

		private static final long serialVersionUID = -2639047965085414029L;

		@EventTopicSubscriber(topic = QueueService.QUEUE_ADDED)
		public void onQueueAdded(final String topic, final Queue queue)
		{
			addElement(queue);
		}

		@EventTopicSubscriber(topic = QueueService.QUEUE_REMOVED)
		public void onQueueRemoved(final String topic, final Queue queue)
		{
			removeElement(queue);
		}

		@EventTopicSubscriber(topic = QueueService.QUEUE_UPDATED)
		public void onQueueUpdated(final String topic, final Queue queue)
		{
			final int index = getIndexOf(queue);
			if (index != -1) {
				replaceRow(index, queue);
			}
		}
	};

	public MessageController()
	{
		AnnotationProcessor.process(this);
	}

	public void setup()
	{
		for (final Message message : messageService.getAll()) {
			messageTableModel.addRow(message);
		}
	}

	public void addMessage(final int action, final Message data)
	{
		switch (action) {
			case 1:
				data.uploadid = null;
			case 0:
				messageService.create(data);
				break;
			case 2:
				publish(data);
				break;
		}
	}

	private void publish(final Message message)
	{
		publish(message.message, message.facebook, message.twitter, message.googleplus, message.youtube);
	}

	private void publish(final String message, final boolean facebook, final boolean twitter, final boolean googlePlus, final boolean youtube)
	{
		if (facebook) {
			final String settingsString = (String) settingsService.get("socialize.socialize.facebook", ""); //NON-NLS
			if (settingsString.contains("___")) {
				final String token = new String(settingsString.substring(0, settingsString.indexOf("___")));
				final String secret = new String(settingsString.substring(settingsString.indexOf("___") + 3, settingsString.length()));
				final ISocialProvider facebookSocialProvider = messageService.get(Provider.FACEBOOK);
				facebookSocialProvider.setAccessToken(new Token(token, secret));
				facebookSocialProvider.publish(message);
			}
		}
		if (twitter) {
			final String settingsString = (String) settingsService.get("socialize.socialize.twitter", ""); //NON-NLS
			if (settingsString.contains("___")) {
				final String token = new String(settingsString.substring(0, settingsString.indexOf("___")));
				final String secret = new String(settingsString.substring(settingsString.indexOf("___") + 3, settingsString.length()));
				final ISocialProvider twitterSocialProvider = messageService.get(Provider.TWITTER);
				twitterSocialProvider.setAccessToken(new Token(token, secret));
				twitterSocialProvider.publish(message);
			}
		}
	}

	public TableModel getMessageTableModel()
	{
		return messageTableModel;
	}

	public MessageService getMessageService()
	{
		return messageService;
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_JOB_FINISHED)
	public void onUploadJobFinished(final String topic, final Queue queue)
	{
		final Message findParameter = new Message();
		findParameter.uploadid = queue.getIdentity();
		for (final Message message : messageService.find(findParameter, true)) {
			publish(message.message.replace("{video}", String.format("http://youtu.be/%s", queue.videoId)), message.facebook, message.twitter, message.googleplus, message.youtube); //NON-NLS
		}
		messageService.clearByUploadID(queue.getIdentity());
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_FINISHED)
	public void onUploadsFinished(final String topic, final Object o)
	{
		for (final Message message : messageService.findWithoutQueueID()) {
			publish(message.message, message.facebook, message.twitter, message.googleplus, message.youtube);
		}

		messageService.clearByUploadID(null);
	}

	public void removeEntryAt(final int selectedRow)
	{
		if (messageTableModel.hasIndex(selectedRow)) {
			final Message message = messageTableModel.getRow(selectedRow);
			messageService.delete(message);
		}
	}

	public ComboBoxModel getUploadsModel()
	{
		for (final Queue queue : queueService.getQueued()) {
			uploadListModel.addElement(queue);
		}
		return uploadListModel;
	}
}
