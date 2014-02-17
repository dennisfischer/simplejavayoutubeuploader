/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.services;

import com.google.common.io.Files;
import de.chaosfisch.google.playlist.PlaylistModel;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtendedPlaceholders {
	private final ResourceBundle resourceBundle;
	private final Pattern        numberPattern;
	private final Pattern        playlistPattern;
	/**
	 * Custom user defined placeholders
	 */
	private final HashMap<String, String> map = new HashMap<>(10);
	/**
	 * The file for the {file} placeholder
	 */
	private File                file;
	/**
	 * The playlist for the {playlist} and {number} placeholders
	 */
	private List<PlaylistModel> playlists;

	@Inject
	public ExtendedPlaceholders(@Named("i18n-resources") final ResourceBundle resourceBundle) {
		this(null, null, resourceBundle);
	}

	/**
	 * Creates a new instance of Extendedplaceholders
	 *
	 * @param file      for {file}
	 * @param playlists for {playlist(i)} and {number(i)(j)}
	 */
	public ExtendedPlaceholders(final File file, final List<PlaylistModel> playlists, final ResourceBundle resourceBundle) {
		this.file = file;
		this.playlists = playlists;
		this.resourceBundle = resourceBundle;
		numberPattern = Pattern.compile(resourceBundle.getString("autotitle.numberPattern"));
		playlistPattern = Pattern.compile(resourceBundle.getString("autotitle.playlistPattern"));
	}

	/**
	 * Registers a new custom placeholder
	 *
	 * @param placeholder the placeholder
	 * @param replacement the replacement
	 */
	public void register(final String placeholder, final String replacement) {
		map.put(placeholder, replacement);
	}

	/**
	 * Replaces all placeholders
	 *
	 * @param input the string to be parsed
	 * @return the parsed string
	 */
	public String replace(String input) {
		if (null == input) {
			return "";
		}
		if (!playlists.isEmpty()) {
			Matcher matcher = numberPattern.matcher(input);

			StringBuffer sb = new StringBuffer(input.length() + 100);
			while (matcher.find()) {
				final int playlist = getPlaylist(matcher, sb);
				final int number = null == matcher.group(2) ? 0 : Integer.parseInt(matcher.group(2));
				final int zeros = null == matcher.group(3) ? 1 : Integer.parseInt(matcher.group(3));

				if (containsPlaylist(playlist)) {
					sb.append(zeroFill(playlists.get(playlist).getItemCount() + 1 + number, zeros));
				} else {
					appendMissingPlaylist(sb, playlist);
				}
			}
			matcher.appendTail(sb);
			input = sb.toString();

			matcher = playlistPattern.matcher(input);
			sb = new StringBuffer(input.length() + 100);
			while (matcher.find()) {
				final int playlist = getPlaylist(matcher, sb);

				if (containsPlaylist(playlist)) {
					sb.append(playlists.get(playlist).getTitle());
				} else {
					appendMissingPlaylist(sb, playlist);
				}
			}
			matcher.appendTail(sb);
			input = sb.toString();
		}

		if (null != file) {
			input = replaceFileTag(input, file);
		}

		for (final Map.Entry<String, String> vars : map.entrySet()) {
			input = input.replaceAll(Pattern.quote(vars.getKey()), vars.getValue());
		}

		return input;
	}

	/**
	 * Fills the number with X zeros
	 *
	 * @param number to be "filled"
	 * @param width  the number of characters
	 * @return the filled number string
	 */
	private String zeroFill(final long number, final int width) {
		return String.format(String.format("%%0%dd", width), number);
	}

	private boolean containsPlaylist(final int playlist) {
		return -1 != playlist && playlists.size() > playlist;
	}

	private void appendMissingPlaylist(final StringBuffer sb, final int playlist) {
		sb.append(String.format("{NO-PLAYLIST-%d}", playlist + 1));
	}

	private int getPlaylist(final Matcher matcher, final StringBuffer sb) {
		matcher.appendReplacement(sb, "");
		return null == matcher.group(1) ? 0 : Integer.parseInt(matcher.group(1)) - 1;
	}

	public String replaceFileTag(final String input, final File file) {
		final String fileName = file.getAbsolutePath();
		return input.replaceAll(resourceBundle.getString("autotitle.file"), Files.getNameWithoutExtension(fileName));
	}

	/**
	 * @return the file
	 */
	public final File getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	public final void setFile(final File file) {
		this.file = file;
	}

	/**
	 * @return the playlists
	 */
	public final List<PlaylistModel> getPlaylists() {
		return playlists;
	}

	/**
	 * @param playlists the playlists to set
	 */
	public final void setPlaylists(final List<PlaylistModel> playlists) {
		this.playlists = playlists;
	}
}
