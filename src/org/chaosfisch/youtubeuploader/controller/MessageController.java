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
import javax.swing.ComboBoxModel;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.youtubeuploader.models.Message;
import org.chaosfisch.youtubeuploader.models.Queue;
import org.chaosfisch.youtubeuploader.services.socialize.FacebookSocialProvider;
import org.chaosfisch.youtubeuploader.services.socialize.TwitterSocialProvider;
import org.chaosfisch.youtubeuploader.services.uploader.Uploader;
import org.javalite.activejdbc.Model;

import com.google.inject.Injector;

/**
 * Created with IntelliJ IDEA. User: Dennis Date: 14.04.12 Time: 21:36 To change
 * this template use File | Settings | File Templates.
 */
public class MessageController
{

	@Inject private Injector	injector;

	public MessageController()
	{
		AnnotationProcessor.process(this);
	}

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

	public ComboBoxModel<String> getUploadsModel()
	{
		return null;
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_JOB_FINISHED)
	public void onUploadJobFinished(final String topic, final Queue queue)
	{
		for (final Model message : Message.find("uploadid = ?", queue.getLongId()))
		{
			publish(message.getString("message").replace("{video}", String.format("http://youtu.be/%s", queue.getString("videoid"))),
					message.getBoolean("facebook"), message.getBoolean("twitter"), message.getBoolean("googleplus"), message.getBoolean("youtube")); // NON-NLS
			message.delete();
		}
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_FINISHED)
	public void onUploadsFinished(final String topic, final Object o)
	{
		for (final Model message : Message.find("uploadid = null"))
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
		if (facebook)
		{
			final String settingsString = settingsDao.get("socialize.socialize.facebook"); // NON-NLS
			if (settingsString.contains("___"))
			{
				final String token = new String(settingsString.substring(0, settingsString.indexOf("___")));
				final String secret = new String(settingsString.substring(settingsString.indexOf("___") + 3, settingsString.length()));
				final FacebookSocialProvider facebookSocialProvider = new FacebookSocialProvider();
				facebookSocialProvider.setAccessToken(new Token(token, secret));
				facebookSocialProvider.publish(message);
			}
		}
		if (twitter)
		{
			final String settingsString = settingsDao.get("socialize.socialize.twitter"); // NON-NLS
			if (settingsString.contains("___"))
			{
				final String token = new String(settingsString.substring(0, settingsString.indexOf("___")));
				final String secret = new String(settingsString.substring(settingsString.indexOf("___") + 3, settingsString.length()));
				final TwitterSocialProvider twitterSocialProvider = new TwitterSocialProvider();
				twitterSocialProvider.setAccessToken(new Token(token, secret));
				twitterSocialProvider.publish(message);
			}
		}
	}

	public void removeEntryAt(final int selectedRow)
	{
	}

	public void setup()
	{
	}
}