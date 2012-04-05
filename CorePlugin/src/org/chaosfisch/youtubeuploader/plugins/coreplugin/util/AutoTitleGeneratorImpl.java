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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.util;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.entities.PlaylistEntry;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.PlaylistService;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.spi.AutoTitleGenerator;

import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 10.01.12
 * Time: 17:49
 * To change this template use File | Settings | File Templates.
 */

public class AutoTitleGeneratorImpl implements AutoTitleGenerator
{
	private              String         formatString      = "";
	private              String         playlistName      = "";
	private              int            playlistNumber    = 0;
	private              int            number            = 0;
	private              String         fileName          = "";
	private final        ResourceBundle resourceBundle    = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.plugins.coreplugin.resources.uploadView"); //NON-NLS
	private static final String         AUTOTITLE_CHANGED = "autoTitleChanged"; //NON-NLS

	public AutoTitleGeneratorImpl()
	{
		AnnotationProcessor.process(this);
	}

	public AutoTitleGeneratorImpl(final String formatString, final PlaylistEntry playlistEntry, final int number)
	{
		this.formatString = formatString;
		this.number = number;
		this.fileName = null;
		this.setPlaylist(playlistEntry);
		AnnotationProcessor.process(this);
	}

	public AutoTitleGeneratorImpl(final String formatString, final PlaylistEntry playlistEntry, final int number, final String fileName)
	{
		this.formatString = formatString;
		this.number = number;
		this.fileName = fileName;
		this.setPlaylist(playlistEntry);
	}

	@Override
	public String getFormatString()
	{
		return this.formatString;
	}

	@Override
	public void setFormatString(final String formatString)
	{
		this.formatString = formatString;
	}

	@Override
	public String getFileName()
	{
		return this.fileName;
	}

	@Override
	public void setFileName(final String fileName)
	{
		this.fileName = fileName;
	}

	@Override public void setPlaylist(final PlaylistEntry playlist)
	{
		this.playlistName = playlist.getName();
		this.playlistNumber = playlist.getNumber();
	}

	@Override public void setNumber(final int number)
	{
		this.number = number;
	}

	@Override
	public String gernerate()
	{
		String formated = this.formatString.replaceAll(this.resourceBundle.getString("autotitle.playlist"), this.playlistName);

		final Pattern p = Pattern.compile(this.resourceBundle.getString("autotitle.numberPattern"));
		final Matcher m = p.matcher(formated);

		if (m.find()) {
			formated = m.replaceAll(this.zeroFill(this.playlistNumber + 1 + this.number, Integer.parseInt(m.group(1))));
		} else {
			formated = formated.replaceAll(this.resourceBundle.getString("autotitle.numberDefault"), String.valueOf(this.playlistNumber + 1 + this.number));
		}

		//noinspection CallToStringEquals
		if (this.fileName != null && !this.fileName.equals("")) {
			//noinspection DuplicateStringLiteralInspection,MagicCharacter
			formated = formated.replaceAll(this.resourceBundle.getString("autotitle.file"), this.fileName.substring(this.fileName.lastIndexOf(System.getProperty("file.separator")) + 1, //NON-NLS
					this.fileName.lastIndexOf('.')));
		}
		return formated;
	}

	@SuppressWarnings("CallToStringEquals") @EventTopicSubscriber(topic = PlaylistService.PLAYLIST_ENTRY_UPDATED)
	public void onPlaylistUpdate(final String topic, final PlaylistEntry playlistEntry)
	{
		if (playlistEntry.getName().equals(this.playlistName)) {
			this.setPlaylist(playlistEntry);
			EventBus.publish(AUTOTITLE_CHANGED, this.gernerate());
		}
	}

	String zeroFill(final int number, final int width)
	{
		return String.format("%0" + width + "d", number);//NON-NLS
	}
}
