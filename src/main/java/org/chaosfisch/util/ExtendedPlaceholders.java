/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.util;

import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Playlist;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtendedPlaceholders {
	/** The file for the {file} placeholder */
	private File file;

	/** Custom user defined placeholders */
	private final HashMap<String, String> map = new HashMap<>(10);

	/** The playlist for the {playlist} and {number} placeholders */
	private List<Playlist> playlists;

	/**
	 * Creates a new instance of Extendedplaceholders
	 *
	 * @param file
	 * 		for {file}
	 * @param playlists
	 * 		for {playlist(i)} and {number(i)(j)}
	 */
	public ExtendedPlaceholders(final File file, final List<Playlist> playlists) {
		this.file = file;
		this.playlists = playlists;
	}

	public ExtendedPlaceholders() {
	}

	/**
	 * Registers a new custom placeholder
	 *
	 * @param placeholder
	 * 		the placeholder
	 * @param replacement
	 * 		the replacement
	 */
	public void register(final String placeholder, final String replacement) {
		map.put(placeholder, replacement);
	}

	/**
	 * Replaces all placeholders
	 *
	 * @param input
	 * 		the string to be parsed
	 *
	 * @return the parsed string
	 */
	public String replace(String input) {
		if (null == input) {
			return "";
		}
		if (!playlists.isEmpty()) {
			Matcher matcher = RegexpUtils.getMatcher(input, TextUtil.getString("autotitle.numberPattern"));

			StringBuffer sb = new StringBuffer(input.length() + 100);
			while (matcher.find()) {
				matcher.appendReplacement(sb, "");
				final int playlist = null == matcher.group(1) ? 0 : Integer.parseInt(matcher.group(1)) - 1;
				final int number = null == matcher.group(2) ? 0 : Integer.parseInt(matcher.group(2));
				final int zeros = null == matcher.group(3) ? 1 : Integer.parseInt(matcher.group(3));

				if (-1 != playlist && playlists.size() > playlist) {
					sb.append(zeroFill(playlists.get(playlist).getNumber() + 1 + number, zeros));
				} else {
					sb.append("{NO-PLAYLIST-").append(playlist + 1).append('}');
				}
			}
			matcher.appendTail(sb);
			input = sb.toString();

			matcher = RegexpUtils.getMatcher(input, TextUtil.getString("autotitle.playlistPattern"));
			sb = new StringBuffer(input.length() + 100);
			while (matcher.find()) {
				matcher.appendReplacement(sb, "");
				final int playlist = null == matcher.group(1) ? 0 : Integer.parseInt(matcher.group(1)) - 1;

				if (-1 != playlist && playlists.size() > playlist) {
					sb.append(playlists.get(playlist).getTitle());
				} else {
					sb.append("{NO-PLAYLIST-").append(playlist + 1).append('}');
				}
			}
			matcher.appendTail(sb);
			input = sb.toString();
		}

		if (null != file)

		{

			final String fileName = file.getAbsolutePath();

			int index = fileName.lastIndexOf('.');
			if (-1 == index) {
				index = fileName.length();
			}
			input = input.replaceAll(TextUtil.getString("autotitle.file"), fileName.substring(fileName.lastIndexOf(File.separator) + 1, index));
		}

		for (final Map.Entry<String, String> vars : map.entrySet())

		{
			input = input.replaceAll(Pattern.quote(vars.getKey()), vars.getValue());
		}

		return input;
	}

	/**
	 * Fills the number with X zeros
	 *
	 * @param number
	 * 		to be "filled"
	 * @param width
	 * 		the number of characters
	 *
	 * @return the filled number string
	 */
	private String zeroFill(final int number, final int width) {
		return String.format(String.format("%%0%dd", width), number);
	}

	/** @return the file */
	public final File getFile() {
		return file;
	}

	/**
	 * @param file
	 * 		the file to set
	 */
	public final void setFile(final File file) {
		this.file = file;
	}

	/** @return the playlists */
	public final List<Playlist> getPlaylists() {
		return playlists;
	}

	/**
	 * @param playlists
	 * 		the playlists to set
	 */
	public final void setPlaylists(final List<Playlist> playlists) {
		this.playlists = playlists;
	}
}
