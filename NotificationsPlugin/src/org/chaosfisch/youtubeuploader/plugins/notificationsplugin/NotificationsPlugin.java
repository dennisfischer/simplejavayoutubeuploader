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

package org.chaosfisch.youtubeuploader.plugins.notificationsplugin;

import com.google.inject.Inject;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.plugin.Pluggable;
import org.chaosfisch.util.Sound;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.uploader.Uploader;
import org.chaosfisch.youtubeuploader.plugins.systemtrayplugin.SystemTrayPlugin;
import org.chaosfisch.youtubeuploader.services.settingsservice.spi.SettingsService;

import java.util.ResourceBundle;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 12.03.12
 * Time: 08:02
 * To change this template use File | Settings | File Templates.
 */
public class NotificationsPlugin implements Pluggable
{
	private static final String[] DEPENDENCIES = {"org.chaosfisch.youtubeuploader.plugins.settingsplugin.SettingsPlugin", "org.chaosfisch.youtubeuploader.plugins.coreplugin.CorePlugin"};
	@Inject private SettingsService settingsService;
	private final ResourceBundle resourceBundle = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.plugins.notificationsplugin.resources.notificationsplugin"); //NON-NLS

	public NotificationsPlugin()
	{
		AnnotationProcessor.process(this);
	}

	@Override public String[] getDependencies()
	{
		return NotificationsPlugin.DEPENDENCIES.clone();
	}

	@Override public void init()
	{
		settingsService.addFilechooser("notification.general.sound_failed", resourceBundle.getString("settings.uploadFailedLabel")); //NON-NLS
		settingsService.addFilechooser("notification.general.sound_finished", resourceBundle.getString("settings.uploadSucceededLabel")); //NON-NLS
	}

	@Override public void onStart()
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override public void onEnd()
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_STARTED)
	public void onUploadStarted(final String t, final Object o)
	{
		EventBus.publish(SystemTrayPlugin.MESSAGE, resourceBundle.getString("message.uploader_started"));
	}

	@EventTopicSubscriber(topic = Uploader.UPLOAD_JOB_FINISHED)
	public void onUploadFinished(final String t, final Object o)
	{
		EventBus.publish(SystemTrayPlugin.MESSAGE, resourceBundle.getString("message.upload_successful"));
		final Sound sound = new Sound();
		sound.setSong((String) settingsService.get("notification.general.sound_finished", "")); //NON-NLS
		sound.play();
	}

	public void onUploadFailed(final String t, final Object o)
	{
		EventBus.publish(SystemTrayPlugin.MESSAGE, resourceBundle.getString("message.upload_failed"));
		final Sound sound = new Sound();
		sound.setSong((String) settingsService.get("notification.general.sound_failed", "")); //NON-NLS
		sound.play();
	}

	@Override public String getName()
	{
		return "NotificationsPlugin"; //NON-NLS
	}

	@Override public String getAuthor()
	{
		return "CHAOSFISCH"; //NON-NLS
	}
}
