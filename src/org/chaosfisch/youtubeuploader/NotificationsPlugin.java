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

package org.chaosfisch.youtubeuploader;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.util.Sound;
import org.chaosfisch.youtubeuploader.services.SettingsService;
import org.chaosfisch.youtubeuploader.services.uploader.Uploader;

import com.google.inject.Inject;

public class NotificationsPlugin
{

	@Inject
	private SettingsService	settingsService;

	public NotificationsPlugin()
	{
		AnnotationProcessor.process(this);
	}

	public void init()
	{
		settingsService.addFilechooser("notification.general.sound_failed", I18nHelper.message("settings.uploadFailedLabel"));
		settingsService.addFilechooser("notification.general.sound_finished", I18nHelper.message("settings.uploadSucceededLabel"));
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_STARTED)
	public void onUploadStarted(final String t, final Object o)
	{
		EventBus.publish(SystemTrayPlugin.MESSAGE, I18nHelper.message("message.uploader_started"));
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_JOB_FINISHED)
	public void onUploadFinished(final String t, final Object o)
	{
		EventBus.publish(SystemTrayPlugin.MESSAGE, I18nHelper.message("message.upload_successful"));
		final Sound sound = new Sound();
		sound.setSong((String) settingsService.get("notification.general.sound_finished", ""));
		sound.play();
	}

	public void onUploadFailed(final String t, final Object o)
	{
		EventBus.publish(SystemTrayPlugin.MESSAGE, I18nHelper.message("message.upload_failed"));
		final Sound sound = new Sound();
		sound.setSong((String) settingsService.get("notification.general.sound_failed", ""));
		sound.play();
	}
}
