/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.google.youtube.upload.metadata;

import com.google.common.base.Charsets;
import de.chaosfisch.util.RegexpUtils;

public final class TagParser {
	/** the separator between "block" tags */
	private static final char   BLOCK_DELIMITER = '"';
	private static final String STRING_TO_MATCH = ",";

	/** flag if block is openBrowser */
	private static boolean blockOpen;

	/** the separator between tags */
	private static final char DELIMITER = ' ';

	private TagParser() {
	}

	/**
	 * Checks validity of the given string
	 *
	 * @param input
	 * 		the string to be checked
	 *
	 * @return true if input is valid
	 */
	public static boolean isValid(final String input) {
		final String parsed = TagParser.parseAll(input);
		if (500 < parsed.getBytes(Charsets.UTF_8).length || parsed.contains("<") || parsed.contains(">")) {
			return false;
		}
		for (final String tag : RegexpUtils.getPattern(STRING_TO_MATCH).split(parsed)) {
			if (30 < tag.length() || 2 > tag.length() || 30 < tag.getBytes(Charsets.UTF_8).length) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns a parsed input string, invalid tags are removed
	 *
	 * @param input
	 * 		the string to be parsed
	 *
	 * @return the parsed string
	 */
	public static String parseAll(String input) {

		input = input.trim();

		final StringBuilder parsedOutput = new StringBuilder(500);
		for (int i = 0; i < input.length(); i++) {
			switch (input.charAt(i)) {
				case TagParser.BLOCK_DELIMITER:
					TagParser.blockOpen = !TagParser.blockOpen;
					parsedOutput.append('"');
					if (TagParser.blockOpen) {
						break;
					}
				case TagParser.DELIMITER:
					if (TagParser.blockOpen) {
						parsedOutput.append(input.charAt(i));
					} else if (parsedOutput.lastIndexOf(",") != parsedOutput.length() && i + 1 != input.length()) {
						parsedOutput.append(',');
					}
					break;
				default:
					parsedOutput.append(input.charAt(i));
					break;
			}
		}
		return TagParser.removeInvalid(parsedOutput.toString().trim());
	}

	/**
	 * Removes invalid tags from input
	 *
	 * @param input
	 * 		the string to be cleaned
	 *
	 * @return the cleaned string
	 */
	private static String removeInvalid(final String input) {
		final String[] tags = RegexpUtils.getPattern(STRING_TO_MATCH).split(input);
		final String[] tmpTags = new String[250];
		int i = 0;
		for (final String tag : tags) {
			if (!(30 < tag.length()) && !(2 > tag.length()) && !(30 < tag.getBytes(Charsets.UTF_8).length)) {
				tmpTags[i] = tag;
				i++;
			}
		}
		final StringBuilder stringBuilder = new StringBuilder(30);
		if (0 < tmpTags.length) {
			stringBuilder.append(tmpTags[0]);
			for (int j = 1; j < tmpTags.length; j++) {
				if (null == tmpTags[j]) {
					break;
				}
				stringBuilder.append(',');
				stringBuilder.append(tmpTags[j]);
			}
		}
		return stringBuilder.toString();
	}
}
