/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.youtubeuploader.services.uploader.Uploader;

import com.google.inject.Inject;

public class NotificationsPlugin
{

	@Inject private SettingsDao	settingsDao;

	public void onUploadFailed(final String t, final Object o)
	{
		EventBus.publish(SystemTrayPlugin.MESSAGE, I18nHelper.message("message.upload_failed"));
		settingsDao.get("notification.general.sound_failed");
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_JOB_FINISHED)
	public void onUploadFinished(final String t, final Object o)
	{
		EventBus.publish(SystemTrayPlugin.MESSAGE, I18nHelper.message("message.upload_successful"));
		settingsDao.get("notification.general.sound_finished");
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_STARTED)
	public void onUploadStarted(final String t, final Object o)
	{
		EventBus.publish(SystemTrayPlugin.MESSAGE, I18nHelper.message("message.uploader_started"));
	}
}