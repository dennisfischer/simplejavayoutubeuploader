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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtendedPlaceholders {
	/** The file for the {file} placeholder */
	private File file;

	/** Custom user defined placeholders */
	private final HashMap<String, String> map = new HashMap<>(10);

	/** The number which modifies {number} placeholder */
	private int number;

	/** The playlist for the {playlist} and {number} placeholders */
	private Playlist playlist;

	/**
	 * Creates a new instance of Extendedplaceholders
	 *
	 * @param file
	 * 		for {file}
	 * @param playlist
	 * 		for {playlist} and {number}
	 * @param number
	 * 		for {number} modifications
	 */
	@SuppressWarnings("SameParameterValue")
	public ExtendedPlaceholders(final File file, final Playlist playlist, final int number) {
		this.file = file;
		this.playlist = playlist;
		this.number = number;
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
		if (input == null) {
			return "";
		}
		if (playlist != null) {
			input = input.replaceAll(TextUtil.getString("autotitle.playlist"), playlist.getTitle());

			final Pattern p = Pattern.compile(TextUtil.getString("autotitle.numberPattern"));
			final Matcher m = p.matcher(input);

			if (m.find()) {
				input = m.replaceAll(zeroFill(playlist.getNumber() + 1 + number, Integer.parseInt(m.group(1))));
				input = input.replaceAll(TextUtil.getString("autotitle.numberDefault"), String.valueOf(playlist.getNumber() + 1 + number));
			} else {
				input = input.replaceAll(TextUtil.getString("autotitle.numberDefault"), String.valueOf(playlist.getNumber() + 1 + number));
			}
		}

		if (file != null) {

			final String fileName = file.getAbsolutePath();

			int index = fileName.lastIndexOf('.');
			if (index == -1) {
				index = fileName.length();
			}
			input = input.replaceAll(TextUtil.getString("autotitle.file"), fileName.substring(fileName.lastIndexOf(File.separator) + 1, index));
		}

		for (final Map.Entry<String, String> vars : map.entrySet()) {
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

	/** @return the number */
	public final int getNumber() {
		return number;
	}

	/**
	 * @param number
	 * 		the number to set
	 */
	public final void setNumber(final int number) {
		this.number = number;
	}

	/** @return the playlist */
	public final Playlist getPlaylist() {
		return playlist;
	}

	/**
	 * @param playlist
	 * 		the playlist to set
	 */
	public final void setPlaylist(final Playlist playlist) {
		this.playlist = playlist;
	}
}
