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
import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Playlist;
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
	private String formatString = "";
	private String playlistName = "";
	private int playlistNumber;
	private int number;
	private       String         fileName       = "";
	private final ResourceBundle resourceBundle = ResourceBundle.getBundle("org.chaosfisch.youtubeuploader.plugins.coreplugin.resources.coreplugin"); //NON-NLS

	public AutoTitleGeneratorImpl()
	{
		AnnotationProcessor.process(this);
	}

	public AutoTitleGeneratorImpl(final String formatString, final Playlist playlist, final int number)
	{
		this.formatString = formatString;
		this.number = number;
		fileName = null;
		setPlaylist(playlist);
		AnnotationProcessor.process(this);
	}

	public AutoTitleGeneratorImpl(final String formatString, final Playlist playlist, final int number, final String fileName)
	{
		this.formatString = formatString;
		this.number = number;
		this.fileName = fileName;
		setPlaylist(playlist);
	}

	@Override
	public String getFormatString()
	{
		return formatString;
	}

	@Override
	public void setFormatString(final String formatString)
	{
		this.formatString = formatString;
	}

	@Override
	public String getFileName()
	{
		return fileName;
	}

	@Override
	public void setFileName(final String fileName)
	{
		this.fileName = fileName;
	}

	@Override public void setPlaylist(final Playlist playlist)
	{
		playlistName = playlist.title;
		playlistNumber = playlist.number;
	}

	@Override public void setNumber(final int number)
	{
		this.number = number;
	}

	@Override
	public String gernerate()
	{
		String formated = formatString.replaceAll(resourceBundle.getString("autotitle.playlist"), playlistName);

		final Pattern p = Pattern.compile(resourceBundle.getString("autotitle.numberPattern"));
		final Matcher m = p.matcher(formated);

		if (m.find()) {
			formated = m.replaceAll(zeroFill(playlistNumber + 1 + number, Integer.parseInt(m.group(1))));
		} else {
			formated = formated.replaceAll(resourceBundle.getString("autotitle.numberDefault"), String.valueOf(playlistNumber + 1 + number));
		}

		//noinspection CallToStringEquals
		if ((fileName != null) && !fileName.equals("")) {
			//noinspection DuplicateStringLiteralInspection,MagicCharacter
			formated = formated.replaceAll(resourceBundle.getString("autotitle.file"), new String(fileName.substring(fileName.lastIndexOf(System.getProperty("file.separator")) + 1, //NON-NLS
																													 fileName.lastIndexOf('.'))));
		}
		return formated;
	}

	@SuppressWarnings("CallToStringEquals") @EventTopicSubscriber(topic = PlaylistService.PLAYLIST_ENTRY_UPDATED)
	public void onPlaylistUpdate(final String topic, final Playlist playlist)
	{
		if (playlist.title.equals(playlistName)) {
			setPlaylist(playlist);
			EventBus.publish(AutoTitleGeneratorImpl.AUTOTITLE_CHANGED, gernerate());
		}
	}

	String zeroFill(final int number, final int width)
	{
		return String.format(String.format("%%0%dd", width), number);//NON-NLS
	}
}
