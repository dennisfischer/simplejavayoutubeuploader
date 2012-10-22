/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.controller;

import javax.inject.Inject;

import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.youtubeuploader.models.Message;
import org.chaosfisch.youtubeuploader.models.Queue;
import org.chaosfisch.youtubeuploader.services.uploader.Uploader;
import org.javalite.activejdbc.Model;

import com.google.inject.Injector;

public class MessageController
{

	@Inject private Injector	injector;

	public void addMessage(final int action, final Message data)
	{
		switch (action)
		{
			case 1:
				data.setInteger("uploadid", null);
			case 0:
				data.saveIt();
				break;
			case 2:
				publish(data);
				break;
		}
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_JOB_FINISHED)
	public void onUploadJobFinished(final String topic, final Queue queue)
	{
		for (final Model message : Model.find("uploadid = ?", queue.getLongId()))
		{
			publish(message.getString("message").replace("{video}", String.format("http://youtu.be/%s", queue.getString("videoid"))),
					message.getBoolean("facebook"), message.getBoolean("twitter"), message.getBoolean("googleplus"), message.getBoolean("youtube")); // NON-NLS
			message.delete();
		}
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_FINISHED)
	public void onUploadsFinished(final String topic, final Object o)
	{
		for (final Model message : Model.find("uploadid = null"))
		{
			publish((Message) message);
			message.delete();
		}
	}

	private void publish(final Message message)
	{
		publish(message.getString("message"), message.getBoolean("facebook"), message.getBoolean("twitter"), message.getBoolean("googleplus"),
				message.getBoolean("youtube"));
	}

	private void publish(final String message, final boolean facebook, final boolean twitter, final boolean googlePlus, final boolean youtube)
	{
	}

	private void validate()
	{
		// final String[] publishlist = new String[] {
		// I18nHelper.message("publishlist.uploadid"),
		// I18nHelper.message("publishlist.uploadsfinished"),
		// I18nHelper.message("publishlist.now") };
		//
		// if (!googlePlusButton.isSelected() && !facebookButton.isSelected() &&
		// !twitterButton.isSelected() && !youtubeButton.isSelected())
		// {
		// I18nHelper.message("validation.service");
		// }
		//
		// if (!ValidationUtils.hasBoundedLength(messageTextArea.getText(), 5,
		// 140))
		// {
		// I18nHelper.message("validation.message");
		// }
		//
		// if (messageTextArea.getText().contains("{video}") &&
		// !ValidationUtils.hasBoundedLength(messageTextArea.getText(), 5, 120))
		// {
		// I18nHelper.message("validation.message.video");
		// }

	}
}
